package com.sifedin.tinderclone.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.sifedin.tinderclone.data.model.Match
import com.sifedin.tinderclone.data.model.MatchWithUser
import com.sifedin.tinderclone.data.model.Message
import com.sifedin.tinderclone.data.model.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class ChatViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Matches list
    var matches by mutableStateOf<List<MatchWithUser>>(emptyList())
    var matchesLoading by mutableStateOf(false)
    private var matchesListener: ListenerRegistration? = null

    // Current chat
    var messages by mutableStateOf<List<Message>>(emptyList())
    var chatPartner by mutableStateOf<User?>(null)
    var currentMatchId by mutableStateOf<String?>(null)
    private var messagesListener: ListenerRegistration? = null

    init {
        loadMatches()
    }

    fun loadMatches() {
        val userId = auth.currentUser?.uid ?: return
        matchesLoading = true

        matchesListener?.remove()
        matchesListener = firestore.collection("matches")
            .whereArrayContains("users", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatVM", "Error loading matches", error)
                    matchesLoading = false
                    return@addSnapshotListener
                }

                viewModelScope.launch {
                    val uniqueMatches = mutableMapOf<String, MatchWithUser>()

                    snapshot?.documents?.forEach { doc ->
                        try {
                            val matchId = doc.id
                            val users = doc.get("users") as? List<String> ?: emptyList()
                            val lastMessage = doc.getString("lastMessage")
                            val lastMessageTimestamp = doc.getDate("lastMessageTimestamp")
                            val lastMessageSenderId = doc.getString("lastMessageSenderId")
                            val seenBy = doc.get("seenBy") as? List<String> ?: emptyList()
                            val createdAt = doc.getDate("createdAt")

                            val match = Match(
                                id = matchId,
                                users = users,
                                lastMessage = lastMessage,
                                lastMessageTimestamp = lastMessageTimestamp,
                                lastMessageSenderId = lastMessageSenderId,
                                seenBy = seenBy,
                                createdAt = createdAt
                            )

                            if (users.size == 2) {
                                val partnerId = users.firstOrNull { it != userId }
                                if (partnerId != null) {
                                    if (!uniqueMatches.containsKey(partnerId)) {
                                        val userDoc = firestore.collection("users")
                                            .document(partnerId).get().await()
                                        val user = userDoc.toObject(User::class.java)
                                        if (user != null) {
                                            uniqueMatches[partnerId] = MatchWithUser(match, user)
                                        }
                                    } else {
                                        val existing = uniqueMatches[partnerId]!!
                                        val existingTime = existing.match.lastMessageTimestamp?.time ?: 0L
                                        val newTime = match.lastMessageTimestamp?.time ?: 0L
                                        if (newTime > existingTime) {
                                            uniqueMatches[partnerId] = MatchWithUser(match, existing.user)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ChatVM", "Error processing match", e)
                        }
                    }

                    matches = uniqueMatches.values.sortedByDescending { matchWithUser ->
                        matchWithUser.match.lastMessageTimestamp?.time ?: 0L
                    }
                    matchesLoading = false
                }
            }
    }

    fun openChat(matchId: String) {
        val userId = auth.currentUser?.uid ?: return
        currentMatchId = matchId

        // Mark messages as seen when opening chat
        markMessagesAsSeen(matchId, userId)

        messagesListener?.remove()

        viewModelScope.launch {
            try {
                val matchDoc = firestore.collection("matches").document(matchId).get().await()
                val users = matchDoc.get("users") as? List<String>
                val partnerId = users?.firstOrNull { it != userId }

                if (partnerId != null) {
                    val userDoc = firestore.collection("users").document(partnerId).get().await()
                    chatPartner = userDoc.toObject(User::class.java)
                }
            } catch (e: Exception) {
                Log.e("ChatVM", "Error loading chat partner", e)
            }
        }

        messagesListener = firestore.collection("matches")
            .document(matchId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatVM", "Error loading messages", error)
                    return@addSnapshotListener
                }

                messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()
            }
    }

    private fun markMessagesAsSeen(matchId: String, userId: String) {
        firestore.collection("matches")
            .document(matchId)
            .update("seenBy", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                Log.d("ChatVM", "✓ Messages marked as seen")
            }
            .addOnFailureListener { e ->
                Log.e("ChatVM", "✗ Failed to mark messages as seen", e)
            }
    }

    fun sendMessage(text: String) {
        val userId = auth.currentUser?.uid ?: return
        val matchId = currentMatchId ?: return
        if (text.isBlank()) return

        val trimmedText = text.trim()
        val now = Date()
        val message = Message(
            senderId = userId,
            text = trimmedText,
            timestamp = now
        )

        firestore.collection("matches")
            .document(matchId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                // Update match document with last message info
                val updateData = hashMapOf<String, Any>(
                    "lastMessage" to trimmedText,
                    "lastMessageTimestamp" to now,
                    "lastMessageSenderId" to userId,
                    "seenBy" to listOf(userId) // Only sender has seen it initially
                )

                firestore.collection("matches")
                    .document(matchId)
                    .update(updateData)
                    .addOnSuccessListener {
                        Log.d("ChatVM", "✓ Match document updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatVM", "✗ Failed to update match document", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ChatVM", "✗ Failed to add message to subcollection", e)
            }
    }

    fun closeChat() {
        messagesListener?.remove()
        messagesListener = null
        currentMatchId = null
        messages = emptyList()
        chatPartner = null
    }

    override fun onCleared() {
        super.onCleared()
        matchesListener?.remove()
        messagesListener?.remove()
    }
}