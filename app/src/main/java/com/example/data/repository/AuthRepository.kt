package com.example.data.repository

import com.example.BuildConfig
import com.example.data.remote.RetrofitClient
import com.example.data.remote.SupabaseAuthResponse
import com.example.data.remote.SupabasePasswordRequest
import com.example.data.remote.SupabaseSignUpRequest
import com.example.util.SessionManager
import com.squareup.moshi.Types
import retrofit2.HttpException

class AuthRepository(private val sessionManager: SessionManager) {
    private val supabaseUrl = BuildConfig.SUPABASE_URL
    private val supabaseKey = BuildConfig.SUPABASE_ANON_KEY

    suspend fun signIn(email: String, password: String) {
        ensureConfigured()
        val response = authService().signInWithPassword(
            apiKey = supabaseKey,
            request = SupabasePasswordRequest(email.trim(), password)
        )
        persistSession(response, email.trim())
    }

    suspend fun signUp(email: String, password: String, displayName: String) {
        ensureConfigured()
        val cleanEmail = email.trim()
        val response = authService().signUp(
            apiKey = supabaseKey,
            request = SupabaseSignUpRequest(
                email = cleanEmail,
                password = password,
                data = mapOf("display_name" to displayName.trim().ifEmpty { cleanEmail.substringBefore("@") })
            )
        )
        persistSession(response, cleanEmail)
    }

    suspend fun signInWithGoogle(idToken: String) {
        ensureConfigured()
        val response = authService().signInWithIdToken(
            apiKey = supabaseKey,
            body = mapOf(
                "provider" to "google",
                "id_token" to idToken
            )
        )
        val user = response.user ?: error("Supabase no devolvio un usuario valido con Google.")
        val displayName = (user.userMetadata?.get("full_name") as? String)
            ?: (user.userMetadata?.get("name") as? String)
            ?: user.email?.substringBefore("@") ?: "Usuario"
        val photoUrl = (user.userMetadata?.get("avatar_url") as? String)
            ?: (user.userMetadata?.get("picture") as? String) ?: ""
        sessionManager.login(
            id = user.id,
            email = user.email ?: "",
            name = displayName,
            photo = photoUrl,
            accessToken = response.accessToken.orEmpty(),
            refreshToken = response.refreshToken.orEmpty()
        )
    }

    fun continueAsGuest() {
        sessionManager.continueAsGuest()
    }

    private fun authService() = RetrofitClient.supabaseAuthService(supabaseUrl)

    private fun ensureConfigured() {
        if (supabaseUrl.isBlank() || supabaseKey.isBlank() || supabaseUrl.contains("your-project-ref")) {
            error("Configura SUPABASE_URL y SUPABASE_ANON_KEY en config/.env para activar el login.")
        }
    }

    private fun persistSession(response: SupabaseAuthResponse, fallbackEmail: String) {
        val user = response.user ?: error("Supabase no devolvio un usuario valido.")
        val displayName = user.userMetadata?.get("display_name") as? String
        sessionManager.login(
            id = user.id,
            email = user.email ?: fallbackEmail,
            name = displayName?.takeIf { it.isNotBlank() } ?: fallbackEmail.substringBefore("@"),
            accessToken = response.accessToken.orEmpty(),
            refreshToken = response.refreshToken.orEmpty()
        )
    }

    companion object {
        fun readableError(throwable: Throwable): String {
            if (throwable is HttpException) {
                val raw = throwable.response()?.errorBody()?.string().orEmpty()
                val adapterType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
                val adapter = RetrofitClient.moshiParser.adapter<Map<String, Any?>>(adapterType)
                val decoded = runCatching { adapter.fromJson(raw) }.getOrNull()
                val message = decoded?.get("message") ?: decoded?.get("error_description") ?: decoded?.get("msg")
                return message?.toString() ?: "Supabase respondio con error ${throwable.code()}."
            }
            return throwable.localizedMessage ?: "No se pudo completar la autenticacion."
        }
    }
}
