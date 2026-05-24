package com.example.util

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

data class UserSession(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String,
    val isGuest: Boolean
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

    fun getGeminiApiKey(): String {
        return prefs.getString("KEY_GEMINI_API_KEY", "") ?: ""
    }

    fun setGeminiApiKey(apiKey: String) {
        prefs.edit().apply {
            putString("KEY_GEMINI_API_KEY", apiKey.trim())
            apply()
        }
    }

    fun getLanguage(): String {
        return prefs.getString("KEY_LANGUAGE", "es") ?: "es"
    }

    fun setLanguage(language: String) {
        prefs.edit().putString("KEY_LANGUAGE", language).apply()
    }

    fun login(id: String, email: String, name: String, photo: String = "", accessToken: String = "") {
        prefs.edit().apply {
            putString("KEY_USER_ID", id)
            putString("KEY_USER_EMAIL", email)
            putString("KEY_USER_NAME", name)
            putString("KEY_USER_PHOTO", photo.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=150" })
            putString("KEY_SUPABASE_ACCESS_TOKEN", accessToken)
            putBoolean("KEY_IS_GUEST", false)
            putBoolean("KEY_IS_LOGGED", true)
            apply()
        }
    }

    fun continueAsGuest() {
        val guestId = prefs.getString("KEY_GUEST_ID", null) ?: "guest-${UUID.randomUUID()}"
        prefs.edit().apply {
            putString("KEY_GUEST_ID", guestId)
            putString("KEY_USER_ID", guestId)
            putString("KEY_USER_EMAIL", "invitado@local")
            putString("KEY_USER_NAME", "Invitado")
            putString("KEY_USER_PHOTO", "")
            remove("KEY_SUPABASE_ACCESS_TOKEN")
            putBoolean("KEY_IS_GUEST", true)
            putBoolean("KEY_IS_LOGGED", true)
            apply()
        }
    }

    fun logout() {
        prefs.edit().apply {
            remove("KEY_USER_ID")
            remove("KEY_USER_EMAIL")
            remove("KEY_USER_NAME")
            remove("KEY_USER_PHOTO")
            remove("KEY_SUPABASE_ACCESS_TOKEN")
            putBoolean("KEY_IS_GUEST", false)
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
            id = prefs.getString("KEY_USER_ID", "guest-local") ?: "guest-local",
            email = prefs.getString("KEY_USER_EMAIL", "test@test.com") ?: "test@test.com",
            displayName = prefs.getString("KEY_USER_NAME", "Usuario") ?: "Usuario",
            photoUrl = prefs.getString("KEY_USER_PHOTO", "") ?: "",
            isGuest = prefs.getBoolean("KEY_IS_GUEST", false)
        )
    }
}
