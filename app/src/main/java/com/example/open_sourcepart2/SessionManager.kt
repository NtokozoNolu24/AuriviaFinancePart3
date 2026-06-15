package com.example.open_sourcepart2

import android.content.Context
import android.content.SharedPreferences
import com.example.open_sourcepart2.database.entities.User

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun createLoginSession(user: User) {
        val editor = prefs.edit()
        editor.putInt("user_id", user.id)
        editor.putString("user_name", user.name)
        editor.putString("user_email", user.email)
        editor.putBoolean("is_logged_in", true)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    fun getUserName(): String? {
        return prefs.getString("user_name", null)
    }

    fun getUserEmail(): String? {
        return prefs.getString("user_email", null)
    }

    fun getUserDetails(): User? {
        if (!isLoggedIn()) return null
        return User(
            id = getUserId(),
            name = getUserName() ?: "",
            email = getUserEmail() ?: "",
            age = 0, // Age not stored in session, default to 0
            password = "" // Password not stored in session
        )
    }

    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}