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

class LikesViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = auth.currentUser?.uid

    var usersWhoLikedMe by mutableStateOf<List<User>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadUsersWhoLikedMe()
    }

    fun loadUsersWhoLikedMe() {
        if (currentUserId == null) {
            Log.e("LikesVM", "No current user")
            errorMessage = "Please log in to view likes"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                Log.d("LikesVM", "Fetching users who liked: $currentUserId")

                val likedMeSwipes = firestore.collection("swipes")
                    .whereEqualTo("targetId", currentUserId)
                    .whereEqualTo("type", "like")
                    .get()
                    .await()

                val swiperIds = likedMeSwipes.documents.mapNotNull {
                    it.getString("swiperId")
                }

                Log.d("LikesVM", "Found ${swiperIds.size} users who liked me")

                if (swiperIds.isEmpty()) {
                    usersWhoLikedMe = emptyList()
                    isLoading = false
                    return@launch
                }

                val matchedUserIds = firestore.collection("matches")
                    .whereArrayContains("users", currentUserId)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        val users = doc.get("users") as? List<String>
                        users?.firstOrNull { it != currentUserId }
                    }
                    .toSet()

                Log.d("LikesVM", "Already matched with: $matchedUserIds")

                val unmatchedSwiperIds = swiperIds.filter { it !in matchedUserIds }

                if (unmatchedSwiperIds.isEmpty()) {
                    usersWhoLikedMe = emptyList()
                    isLoading = false
                    return@launch
                }

                val usersList = mutableListOf<User>()
                unmatchedSwiperIds.chunked(10).forEach { batch ->
                    val usersSnapshot = firestore.collection("users")
                        .whereIn("uid", batch)
                        .get()
                        .await()

                    val batchUsers = usersSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(User::class.java)
                    }
                    usersList.addAll(batchUsers)
                }

                usersWhoLikedMe = usersList
                Log.d("LikesVM", "Loaded ${usersList.size} users who liked me")
                isLoading = false

            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to fetch users who liked you"
                isLoading = false
                Log.e("LikesVM", "Error fetching liked users", e)
            }
        }
    }

    fun createMatch(partnerId: String, onSuccess: (String) -> Unit) {
        if (currentUserId == null) {
            Log.e("LikesVM", "No current user")
            errorMessage = "Please log in to create matches"
            return
        }

        viewModelScope.launch {
            try {
                Log.d("LikesVM", "Creating match between $currentUserId and $partnerId")

                val existingMatch = firestore.collection("matches")
                    .whereArrayContains("users", currentUserId)
                    .get()
                    .await()
                    .documents
                    .firstOrNull { doc ->
                        val users = doc.get("users") as? List<String>
                        users?.contains(partnerId) == true
                    }

                if (existingMatch != null) {
                    Log.d("LikesVM", "Match already exists: ${existingMatch.id}")
                    usersWhoLikedMe = usersWhoLikedMe.filter { it.uid != partnerId }
                    onSuccess(existingMatch.id)
                    return@launch
                }

                val swipeData = hashMapOf(
                    "swiperId" to currentUserId,
                    "targetId" to partnerId,
                    "type" to "like",
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("swipes").add(swipeData).await()
                Log.d("LikesVM", "Like back swipe recorded")

                val matchData = hashMapOf(
                    "users" to listOf(currentUserId, partnerId).sorted(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "lastMessage" to null,
                    "lastMessageTimestamp" to null,
                    "lastMessageSenderId" to null,
                    "seenBy" to emptyList<String>()
                )

                val matchRef = firestore.collection("matches").add(matchData).await()
                Log.d("LikesVM", "Match created: ${matchRef.id}")

                usersWhoLikedMe = usersWhoLikedMe.filter { it.uid != partnerId }
                onSuccess(matchRef.id)

            } catch (e: Exception) {
                errorMessage = "Failed to create match: ${e.message}"
                Log.e("LikesVM", "Error creating match", e)
            }
        }
    }

    fun refresh() {
        loadUsersWhoLikedMe()
    }
}