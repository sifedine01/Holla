package com.sifedin.tinderclone.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import java.util.Calendar

data class User(
    val uid: String = "",
    val name: String = "",
    val gender: String = "",
    val birthday: String = "",
    val interestedin: String = "",
    val phoneNumber: String = "",
    val photos: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null
) {
    fun getAge(): Int {
        if (birthday.isEmpty()) return 25

        try {
            val parts = birthday.split("/")
            if (parts.size != 3) return 25

            val day = parts[0].toIntOrNull() ?: return 25
            val month = parts[1].toIntOrNull() ?: return 25
            val year = parts[2].toIntOrNull() ?: return 25

            val birthCalendar = Calendar.getInstance().apply {
                set(year, month - 1, day)
            }

            val today = Calendar.getInstance()
            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }

            return if (age > 0) age else 25
        } catch (e: Exception) {
            return 25
        }
    }
}