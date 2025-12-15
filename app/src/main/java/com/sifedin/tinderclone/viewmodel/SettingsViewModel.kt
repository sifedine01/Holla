package com.sifedin.tinderclone.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sifedin.tinderclone.data.model.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var currentUserProfile by mutableStateOf<User?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    var nameInput by mutableStateOf("")
    var birthdayInput by mutableStateOf("")
    var genderInput by mutableStateOf<String?>(null)

    // Track current user ID to detect when user changes
    private var currentUserId: String? = null

    init {
        Log.d("SettingsVM", "ViewModel created - init block running")
        loadUserProfile()
    }

    // 🚨 NEW: Public method to refresh user data when screen is opened
    fun refreshUserProfile() {
        val newUserId = auth.currentUser?.uid
        Log.d("SettingsVM", "refreshUserProfile() called - Current: $currentUserId, New: $newUserId")

        // If user changed or data needs refresh, reload
        if (newUserId != currentUserId || currentUserProfile == null) {
            Log.d("SettingsVM", "User changed or no data - reloading profile")
            clearUserData()
            loadUserProfile()
        } else {
            Log.d("SettingsVM", "Same user - keeping existing data")
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.e("SettingsVM", "No authenticated user")
            errorMessage = "User not logged in"
            clearUserData()
            return
        }

        Log.d("SettingsVM", "Loading profile for user: $userId")
        currentUserId = userId
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(userId).get().await()

                if (doc.exists()) {
                    val user = doc.toObject(User::class.java)
                    currentUserProfile = user

                    nameInput = user?.name ?: ""
                    birthdayInput = user?.birthday ?: ""
                    genderInput = user?.gender

                    Log.d("SettingsVM", "✓ Profile loaded successfully: ${user?.name}")
                } else {
                    Log.e("SettingsVM", "User document does not exist in Firestore")
                    errorMessage = "User profile not found"
                    clearUserData()
                }

                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load profile: ${e.message}"
                isLoading = false
                Log.e("SettingsVM", "Error loading profile", e)
                clearUserData()
            }
        }
    }

    fun updateProfile(onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            errorMessage = "User not logged in"
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        val updatedData = mapOf(
            "name" to nameInput,
            "birthday" to birthdayInput,
            "gender" to genderInput
        )

        Log.d("SettingsVM", "Updating profile for user: $userId")

        firestore.collection("users").document(userId)
            .update(updatedData)
            .addOnSuccessListener {
                isLoading = false
                successMessage = "Profile updated successfully!"
                Log.d("SettingsVM", "✓ Profile updated successfully")
                loadUserProfile()
                onSuccess()
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = "Update failed: ${e.message}"
                Log.e("SettingsVM", "Failed to update profile", e)
            }
    }

    fun logout(onSuccess: () -> Unit) {
        Log.d("SettingsVM", "Logging out user: $currentUserId")
        clearUserData()
        auth.signOut()
        Log.d("SettingsVM", "✓ User logged out and data cleared")
        onSuccess()
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            errorMessage = "Not logged in"
            return
        }

        Log.d("SettingsVM", "Deleting account for user: $userId")

        firestore.collection("users").document(userId).delete()
            .addOnSuccessListener {
                Log.d("SettingsVM", "✓ Firestore document deleted")
                auth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        Log.d("SettingsVM", "✓ Firebase Auth user deleted")
                        clearUserData()
                        onSuccess()
                    }
                    ?.addOnFailureListener { e ->
                        errorMessage = "Failed to delete user: ${e.message}"
                        Log.e("SettingsVM", "Failed to delete Firebase Auth user", e)
                    }
            }
            .addOnFailureListener { e ->
                errorMessage = "Failed to delete profile data: ${e.message}"
                Log.e("SettingsVM", "Failed to delete Firestore document", e)
            }
    }

    // 🚨 NEW: Method to completely clear all user data
    private fun clearUserData() {
        Log.d("SettingsVM", "Clearing all user data")
        currentUserProfile = null
        nameInput = ""
        birthdayInput = ""
        genderInput = null
        errorMessage = null
        successMessage = null
        currentUserId = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("SettingsVM", "ViewModel being destroyed")
    }
}