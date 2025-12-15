package com.sifedin.tinderclone.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.sifedin.tinderclone.data.model.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SwipeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var potentialMatches by mutableStateOf<List<User>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var onMatchFound: ((matchedUser: User, matchId: String) -> Unit)? = null

    init {
        loadPotentialMatches()
    }

    private fun loadPotentialMatches() {
        val currentUserId = auth.currentUser?.uid ?: return
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val currentUserDoc = firestore.collection("users").document(currentUserId).get().await()
                val currentUser = currentUserDoc.toObject(User::class.java)

                if (currentUser == null) {
                    errorMessage = "Could not load current user profile."
                    isLoading = false
                    return@launch
                }

                val query = firestore.collection("users")
                    .whereEqualTo("gender", currentUser.interestedin)
                    .get()
                    .await()

                val allUsers = query.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }.filter { it.uid != currentUserId }

                val swipedUsers = firestore.collection("swipes")
                    .whereEqualTo("swiperId", currentUserId)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.getString("targetId") }
                    .toSet()

                val matchedUsers = firestore.collection("matches")
                    .whereArrayContains("users", currentUserId)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        val users = doc.get("users") as? List<String>
                        users?.firstOrNull { it != currentUserId }
                    }
                    .toSet()

                val excludedUserIds = swipedUsers + matchedUsers
                potentialMatches = allUsers.filter { user ->
                    user.uid !in excludedUserIds
                }

                Log.d("SwipeVM", "Loaded ${potentialMatches.size} potential matches")
                isLoading = false

            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to fetch matches"
                isLoading = false
                Log.e("SwipeViewModel", "Error fetching matches", e)
            }
        }
    }

    fun recordSwipe(targetUserId: String, isLiked: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val swipeData = hashMapOf(
                    "swiperId" to currentUserId,
                    "targetId" to targetUserId,
                    "type" to if (isLiked) "like" else "pass",
                    "timestamp" to FieldValue.serverTimestamp()
                )

                firestore.collection("swipes").add(swipeData).await()
                potentialMatches = potentialMatches.filter { it.uid != targetUserId }

                if (isLiked) {
                    checkForMatch(currentUserId, targetUserId)
                }

                Log.d("SwipeVM", "Swipe recorded: ${if (isLiked) "LIKE" else "PASS"} on $targetUserId")

            } catch (e: Exception) {
                errorMessage = "Swipe failed: ${e.message}"
                Log.e("SwipeVM", "Error recording swipe", e)
            }
        }
    }

    private fun checkForMatch(currentUserId: String, targetUserId: String) {
        viewModelScope.launch {
            try {
                val reciprocalLike = firestore.collection("swipes")
                    .whereEqualTo("swiperId", targetUserId)
                    .whereEqualTo("targetId", currentUserId)
                    .whereEqualTo("type", "like")
                    .get()
                    .await()

                if (!reciprocalLike.isEmpty) {
                    Log.d("SwipeVM", "Reciprocal like found! Creating match...")

                    val matchedUserDoc = firestore.collection("users")
                        .document(targetUserId)
                        .get()
                        .await()

                    val matchedUser = matchedUserDoc.toObject(User::class.java)

                    if (matchedUser != null) {
                        createMatch(currentUserId, matchedUser)
                    } else {
                        Log.e("SwipeVM", "Could not load matched user data")
                    }
                } else {
                    Log.d("SwipeVM", "No reciprocal like yet")
                }

            } catch (e: Exception) {
                Log.e("SwipeVM", "Error checking for match", e)
            }
        }
    }

    private suspend fun createMatch(user1Id: String, matchedUser: User) {
        try {
            val user2Id = matchedUser.uid

            val existingMatch = firestore.collection("matches")
                .whereArrayContains("users", user1Id)
                .get()
                .await()
                .documents
                .firstOrNull { doc ->
                    val users = doc.get("users") as? List<String>
                    users?.contains(user2Id) == true
                }

            if (existingMatch != null) {
                Log.d("SwipeVM", "Match already exists: ${existingMatch.id}")
                onMatchFound?.invoke(matchedUser, existingMatch.id)
                return
            }

            val matchData = hashMapOf(
                "users" to listOf(user1Id, user2Id).sorted(),
                "createdAt" to FieldValue.serverTimestamp(),
                "lastMessage" to null,
                "lastMessageTimestamp" to null,
                "lastMessageSenderId" to null,
                "seenBy" to emptyList<String>()
            )

            val docRef = firestore.collection("matches").add(matchData).await()
            Log.d("SwipeVM", "New match created: ${docRef.id}")

            onMatchFound?.invoke(matchedUser, docRef.id)

        } catch (e: Exception) {
            errorMessage = "Match creation failed: ${e.message}"
            Log.e("SwipeVM", "Error creating match", e)
        }
    }

    fun refreshMatches() {
        loadPotentialMatches()
    }
}