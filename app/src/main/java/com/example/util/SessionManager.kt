package com.example.util

import android.content.Context
import android.content.SharedPreferences

data class UserSession(
    val email: String,
    val displayName: String,
    val photoUrl: String
)

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shopwise_prefs", Context.MODE_PRIVATE)

    fun getThemeMode(): String {
        return prefs.getString("KEY_THEME_MODE", "system") ?: "system"
    }

    fun getThemeColor(): String {
        return prefs.getString("KEY_THEME_COLOR", "blue") ?: "blue"
    }

    fun setTheme(mode: String, color: String) {
        prefs.edit().apply {
            putString("KEY_THEME_MODE", mode)
            putString("KEY_THEME_COLOR", color)
            apply()
        }
    }

    fun login(email: String, name: String, photo: String = "") {
        prefs.edit().apply {
            putString("KEY_USER_EMAIL", email)
            putString("KEY_USER_NAME", name)
            putString("KEY_USER_PHOTO", photo.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=150" })
            putBoolean("KEY_IS_LOGGED", true)
            apply()
        }
    }

    fun logout() {
        prefs.edit().apply {
            remove("KEY_USER_EMAIL")
            remove("KEY_USER_NAME")
            remove("KEY_USER_PHOTO")
            putBoolean("KEY_IS_LOGGED", false)
            apply()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean("KEY_IS_LOGGED", false)
    }

    fun getUserSession(): UserSession? {
        if (!isUserLoggedIn()) return null
        return UserSession(
            email = prefs.getString("KEY_USER_EMAIL", "test@test.com") ?: "test@test.com",
            displayName = prefs.getString("KEY_USER_NAME", "Usuario de Google") ?: "Usuario de Google",
            photoUrl = prefs.getString("KEY_USER_PHOTO", "") ?: ""
        )
    }
}
