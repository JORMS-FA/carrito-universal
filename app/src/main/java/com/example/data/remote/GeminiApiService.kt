package com.example.data.remote

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import okhttp3.Request

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Body request: MoshiGenerateContentRequest
    ): MoshiGenerateContentResponse
}

/**
 * Mutable holder for the Gemini API key, used by the OkHttp interceptor
 * to inject the key as an X-Goog-Api-Key header at request time.
 */
object GeminiApiKeyProvider {
    @Volatile
    var apiKey: String = ""
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val newRequest = if (GeminiApiKeyProvider.apiKey.isNotBlank()) {
                original.newBuilder()
                    .header("X-Goog-Api-Key", GeminiApiKeyProvider.apiKey)
                    .build()
            } else {
                original
            }
            chain.proceed(newRequest)
        }
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val geminiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    fun supabaseAuthService(baseUrl: String): SupabaseAuthService {
        return Retrofit.Builder()
            .baseUrl(baseUrl.trimEnd('/') + "/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseAuthService::class.java)
    }

    fun fetchUrl(url: String): String {
        val allowedDomains = listOf(
            "amazon.com", "www.amazon.com", "amazon.es", "amazon.co.uk", "amazon.de",
            "mercadolibre.com", "www.mercadolibre.com", "mercadolibre.com.ar",
            "mercadolibre.com.mx", "mercadolibre.com.co", "mercadolibre.cl",
            "aliexpress.com", "www.aliexpress.com", "es.aliexpress.com",
            "temu.com", "www.temu.com",
            "ebay.com", "www.ebay.com", "ebay.es",
            "nike.com", "www.nike.com",
            "unsplash.com", "images.unsplash.com"
        )
        val uri = try { java.net.URI(url) } catch (e: Exception) { return "" }
        val host = uri.host?.lowercase() ?: return ""
        val isAllowed = allowedDomains.any { host == it || host.endsWith(".$it") }
        if (!isAllowed) {
            Log.w("RetrofitClient", "URL bloqueada por seguridad: dominio no permitido: $host")
            return ""
        }
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 CarritoUniversal/1.0")
            .build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return ""
            return response.body?.string().orEmpty()
        }
    }

    val moshiParser: Moshi by lazy { moshi }
}
