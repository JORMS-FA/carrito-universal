package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

data class UserSession(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String,
    val isGuest: Boolean
)

class SessionManager(context: Context) {
    private val appContext = context.applicationContext
    private val legacyPrefs: SharedPreferences = appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
    private val settingsPrefs: SharedPreferences = appContext.getSharedPreferences(SETTINGS_PREFS_NAME, Context.MODE_PRIVATE)
    private val securePrefs: SharedPreferences by lazy { createSecurePrefs(appContext) }

    init {
        migrateLegacySessionData()
    }

    fun getThemeMode(): String {
        return settingsPrefs.getString(KEY_THEME_MODE, DEFAULT_THEME_MODE) ?: DEFAULT_THEME_MODE
    }

    fun getThemeColor(): String {
        return settingsPrefs.getString(KEY_THEME_COLOR, DEFAULT_THEME_COLOR) ?: DEFAULT_THEME_COLOR
    }

    fun setTheme(mode: String, color: String) {
        settingsPrefs.edit().apply {
            putString(KEY_THEME_MODE, mode)
            putString(KEY_THEME_COLOR, color)
            apply()
        }
    }

    fun getGeminiApiKey(): String {
        return securePrefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
    }

    fun setGeminiApiKey(apiKey: String) {
        securePrefs.edit().apply {
            putString(KEY_GEMINI_API_KEY, apiKey.trim())
            apply()
        }
    }

    fun getLanguage(): String {
        return settingsPrefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun setLanguage(language: String) {
        settingsPrefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun login(
        id: String,
        email: String,
        name: String,
        photo: String = "",
        accessToken: String = "",
        refreshToken: String = ""
    ) {
        securePrefs.edit().apply {
            putString(KEY_USER_ID, id)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_PHOTO, photo.ifEmpty { DEFAULT_USER_PHOTO })
            putString(KEY_SUPABASE_ACCESS_TOKEN, accessToken)
            putString(KEY_SUPABASE_REFRESH_TOKEN, refreshToken)
            putBoolean(KEY_IS_GUEST, false)
            putBoolean(KEY_IS_LOGGED, true)
            remove(KEY_GUEST_ID)
            apply()
        }
    }

    fun continueAsGuest() {
        val guestId = securePrefs.getString(KEY_GUEST_ID, null) ?: "guest-${UUID.randomUUID()}"
        securePrefs.edit().apply {
            putString(KEY_GUEST_ID, guestId)
            putString(KEY_USER_ID, guestId)
            putString(KEY_USER_EMAIL, GUEST_EMAIL)
            putString(KEY_USER_NAME, GUEST_NAME)
            putString(KEY_USER_PHOTO, "")
            remove(KEY_SUPABASE_ACCESS_TOKEN)
            remove(KEY_SUPABASE_REFRESH_TOKEN)
            putBoolean(KEY_IS_GUEST, true)
            putBoolean(KEY_IS_LOGGED, true)
            apply()
        }
    }

    fun logout() {
        securePrefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_USER_PHOTO)
            remove(KEY_SUPABASE_ACCESS_TOKEN)
            remove(KEY_SUPABASE_REFRESH_TOKEN)
            remove(KEY_GUEST_ID)
            putBoolean(KEY_IS_GUEST, false)
            putBoolean(KEY_IS_LOGGED, false)
            apply()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return securePrefs.getBoolean(KEY_IS_LOGGED, false)
    }

    fun getUserSession(): UserSession? {
        if (!isUserLoggedIn()) return null
        return UserSession(
            id = securePrefs.getString(KEY_USER_ID, GUEST_ID_FALLBACK) ?: GUEST_ID_FALLBACK,
            email = securePrefs.getString(KEY_USER_EMAIL, DEFAULT_SESSION_EMAIL) ?: DEFAULT_SESSION_EMAIL,
            displayName = securePrefs.getString(KEY_USER_NAME, DEFAULT_SESSION_NAME) ?: DEFAULT_SESSION_NAME,
            photoUrl = securePrefs.getString(KEY_USER_PHOTO, "") ?: "",
            isGuest = securePrefs.getBoolean(KEY_IS_GUEST, false)
        )
    }

    private fun migrateLegacySessionData() {
        if (legacyPrefs.all.isEmpty()) return

        val secureEditor = securePrefs.edit()
        val settingsEditor = settingsPrefs.edit()
        var shouldClearLegacy = false

        if (legacyPrefs.contains(KEY_THEME_MODE) && !settingsPrefs.contains(KEY_THEME_MODE)) {
            settingsEditor.putString(KEY_THEME_MODE, legacyPrefs.getString(KEY_THEME_MODE, DEFAULT_THEME_MODE))
            shouldClearLegacy = true
        }
        if (legacyPrefs.contains(KEY_THEME_COLOR) && !settingsPrefs.contains(KEY_THEME_COLOR)) {
            settingsEditor.putString(KEY_THEME_COLOR, legacyPrefs.getString(KEY_THEME_COLOR, DEFAULT_THEME_COLOR))
            shouldClearLegacy = true
        }
        if (legacyPrefs.contains(KEY_LANGUAGE) && !settingsPrefs.contains(KEY_LANGUAGE)) {
            settingsEditor.putString(KEY_LANGUAGE, legacyPrefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE))
            shouldClearLegacy = true
        }

        if (legacyPrefs.contains(KEY_GEMINI_API_KEY) && !securePrefs.contains(KEY_GEMINI_API_KEY)) {
            secureEditor.putString(KEY_GEMINI_API_KEY, legacyPrefs.getString(KEY_GEMINI_API_KEY, ""))
            shouldClearLegacy = true
        }
        if (legacyPrefs.contains(KEY_USER_ID) && !securePrefs.contains(KEY_USER_ID)) {
            secureEditor.putString(KEY_USER_ID, legacyPrefs.getString(KEY_USER_ID, GUEST_ID_FALLBACK))
            shouldClearLegacy = true
        }
        if (legacyPrefs.contains(KEY_USER_EMAIL) && !securePrefs.contains(KEY_USER_EMAIL)) {
            secureEditor.putString(KEY_USER_EMAIL, legacyPrefs.getString(KEY_USER_EMAIL, DEFAULT_SESSION_EMAIL))
            shouldClearLegacy = true
        }
        if (legacyPrefs.contains(KEY_USER_NAME) && !securePrefs.contains(KEY_USER_NAME)) {
            secureEditor.putString(KEY_USER_NAME, legacyPrefs.getString(KEY_USER_NAME, DEFAULT_SESSION_NAME))
            shouldClearLegacy = true
        }
        if (legacyPrefs.contains(KEY_USER_PHOTO) && !securePrefs.contains(KEY_USER_PHOTO)) {
            secureEditor.putString(KEY_USER_PHOTO, legacyPrefs.getString(KEY_USER_PHOTO, ""))
            shouldClearLegacy = true
        }
        if (legacyPrefs.contains(KEY_SUPABASE_ACCESS_TOKEN) && !securePrefs.contains(KEY_SUPABASE_ACCESS_TOKEN)) {
            secureEditor.putString(KEY_SUPABASE_ACCESS_TOKEN, legacyPrefs.getString(KEY_SUPABASE_ACCESS_TOKEN, ""))
            shouldClearLegacy = true
        }
        if (legacyPrefs.contains(KEY_SUPABASE_REFRESH_TOKEN) && !securePrefs.contains(KEY_SUPABASE_REFRESH_TOKEN)) {
            secureEditor.putString(KEY_SUPABASE_REFRESH_TOKEN, legacyPrefs.getString(KEY_SUPABASE_REFRESH_TOKEN, ""))
            shouldClearLegacy = true
        }
        if (legacyPrefs.contains(KEY_GUEST_ID) && !securePrefs.contains(KEY_GUEST_ID)) {
            secureEditor.putString(KEY_GUEST_ID, legacyPrefs.getString(KEY_GUEST_ID, null))
            shouldClearLegacy = true
        }
        if (legacyPrefs.contains(KEY_IS_GUEST) && !securePrefs.contains(KEY_IS_GUEST)) {
            secureEditor.putBoolean(KEY_IS_GUEST, legacyPrefs.getBoolean(KEY_IS_GUEST, false))
            shouldClearLegacy = true
        }
        if (legacyPrefs.contains(KEY_IS_LOGGED) && !securePrefs.contains(KEY_IS_LOGGED)) {
            secureEditor.putBoolean(KEY_IS_LOGGED, legacyPrefs.getBoolean(KEY_IS_LOGGED, false))
            shouldClearLegacy = true
        }

        settingsEditor.apply()
        secureEditor.apply()
        if (shouldClearLegacy) {
            legacyPrefs.edit().clear().apply()
        }
    }

    private fun createSecurePrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val LEGACY_PREFS_NAME = "shopwise_prefs"
        private const val SETTINGS_PREFS_NAME = "carrito_universal_settings"
        private const val SECURE_PREFS_NAME = "carrito_universal_secure_prefs"

        private const val KEY_THEME_MODE = "KEY_THEME_MODE"
        private const val KEY_THEME_COLOR = "KEY_THEME_COLOR"
        private const val KEY_LANGUAGE = "KEY_LANGUAGE"
        private const val KEY_GEMINI_API_KEY = "KEY_GEMINI_API_KEY"
        private const val KEY_USER_ID = "KEY_USER_ID"
        private const val KEY_USER_EMAIL = "KEY_USER_EMAIL"
        private const val KEY_USER_NAME = "KEY_USER_NAME"
        private const val KEY_USER_PHOTO = "KEY_USER_PHOTO"
        private const val KEY_SUPABASE_ACCESS_TOKEN = "KEY_SUPABASE_ACCESS_TOKEN"
        private const val KEY_SUPABASE_REFRESH_TOKEN = "KEY_SUPABASE_REFRESH_TOKEN"
        private const val KEY_GUEST_ID = "KEY_GUEST_ID"
        private const val KEY_IS_GUEST = "KEY_IS_GUEST"
        private const val KEY_IS_LOGGED = "KEY_IS_LOGGED"

        private const val DEFAULT_THEME_MODE = "system"
        private const val DEFAULT_THEME_COLOR = "blue"
        private const val DEFAULT_LANGUAGE = "es"
        private const val DEFAULT_SESSION_EMAIL = "test@test.com"
        private const val DEFAULT_SESSION_NAME = "Usuario"
        private const val DEFAULT_USER_PHOTO = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=150"
        private const val GUEST_EMAIL = "invitado@local"
        private const val GUEST_NAME = "Invitado"
        private const val GUEST_ID_FALLBACK = "guest-local"
    }
}
