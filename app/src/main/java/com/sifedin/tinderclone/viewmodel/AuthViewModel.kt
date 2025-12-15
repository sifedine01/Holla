package com.sifedin.tinderclone.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sifedin.tinderclone.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val CLOUDINARY_CLOUD_NAME = "dqrzpu6xw"
    private val CLOUDINARY_UPLOAD_PRESET = "hola_dating_app_preset"

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var name by mutableStateOf("")
    var gender by mutableStateOf<String?>(null)
    var birthday by mutableStateOf<String?>(null)
    var interestedIn by mutableStateOf<String?>(null)

    var photoUris by mutableStateOf<List<Uri>>(emptyList())

    fun registerWithEmail(onProfileExists: () -> Unit, onProfileMissing: () -> Unit) {
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match."
            return
        }
        if (email.isBlank() || password.length < 6) {
            errorMessage = "Email/Password must be at least 6 characters."
            return
        }

        isLoading = true
        errorMessage = null

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkProfileCompletion(onProfileExists, onProfileMissing)
                } else {
                    isLoading = false
                    errorMessage = task.exception?.message ?: "Registration failed"
                }
            }
    }

    fun signInWithEmail(onProfileExists: () -> Unit, onProfileMissing: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Enter email and password."
            return
        }
        isLoading = true
        errorMessage = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkProfileCompletion(onProfileExists, onProfileMissing)
                } else {
                    isLoading = false
                    errorMessage = task.exception?.message ?: "Login failed"
                }
            }
    }

    private fun checkProfileCompletion(onProfileExists: () -> Unit, onProfileMissing: () -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            isLoading = false
            errorMessage = "User not authenticated"
            return
        }

        viewModelScope.launch {
            try {
                val documentSnapshot = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()

                isLoading = false

                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null && user.name.isNotBlank() && user.photos.isNotEmpty()) {
                        onProfileExists()
                    } else {
                        onProfileMissing()
                    }
                } else {
                    onProfileMissing()
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Failed to check profile: ${e.message}"
                Log.e("AuthViewModel", "Profile check error", e)
            }
        }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val user = auth.currentUser
        if (user == null || name.isBlank() || gender.isNullOrEmpty() || photoUris.isEmpty() || birthday.isNullOrEmpty() || interestedIn.isNullOrEmpty()) {
            errorMessage = "Profile data incomplete or no photos selected"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val context = storage.app.applicationContext
                val photoUrls = mutableListOf<String>()

                for (uri in photoUris) {
                    val url = uploadPhotoToCloudinary(context, uri)
                    photoUrls.add(url)
                }

                withContext(Dispatchers.Main) {
                    saveProfileDocument(user.uid, photoUrls, onSuccess)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    errorMessage = "Upload failed: ${e.message}"
                    Log.e("AuthViewModel", "Cloudinary Upload Error", e)
                }
            }
        }
    }

    private suspend fun uploadPhotoToCloudinary(context: android.content.Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw Exception("Failed to read image from URI")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart(
                    "file",
                    tempFile.name,
                    tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    throw Exception("Cloudinary upload failed: ${response.code}. Details: $errorBody")
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty response from Cloudinary")
                val json = JSONObject(responseBody)

                return@withContext json.getString("secure_url")
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    private fun saveProfileDocument(
        uid: String,
        photoUrls: List<String>,
        onSuccess: () -> Unit
    ) {
        val profileData = hashMapOf(
            "uid" to uid,
            "name" to name,
            "gender" to gender,
            "birthday" to birthday,
            "interestedin" to interestedIn,
            "phoneNumber" to (auth.currentUser?.phoneNumber ?: ""),
            "photos" to photoUrls,
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(uid)
            .set(profileData)
            .addOnSuccessListener {
                isLoading = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.message ?: "Failed to save profile"
                Log.e("AuthViewModel", "Firestore save error", e)
            }
    }

    fun signOut() {
        auth.signOut()
        // Clear all user data
        email = ""
        password = ""
        confirmPassword = ""
        name = ""
        gender = null
        birthday = null
        interestedIn = null
        photoUris = emptyList()
        errorMessage = null
        isLoading = false
    }

    fun clearUserData() {
        // Clear all form data when switching users
        email = ""
        password = ""
        confirmPassword = ""
        name = ""
        gender = null
        birthday = null
        interestedIn = null
        photoUris = emptyList()
        errorMessage = null
        isLoading = false
    }
}