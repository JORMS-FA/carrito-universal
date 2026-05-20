package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MoshiGenerateContentRequest(
    @Json(name = "contents") val contents: List<MoshiContent>,
    @Json(name = "generationConfig") val generationConfig: MoshiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: MoshiContent? = null
)

@JsonClass(generateAdapter = true)
data class MoshiContent(
    @Json(name = "parts") val parts: List<MoshiPart>
)

@JsonClass(generateAdapter = true)
data class MoshiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class MoshiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class MoshiGenerateContentResponse(
    @Json(name = "candidates") val candidates: List<MoshiCandidate>?
)

@JsonClass(generateAdapter = true)
data class MoshiCandidate(
    @Json(name = "content") val content: MoshiContent?
)

/**
 * Data structure extracted from the product URL via Gemini.
 */
@JsonClass(generateAdapter = true)
data class ProductExtractionResult(
    @Json(name = "title") val title: String,
    @Json(name = "brand") val brand: String,
    @Json(name = "price") val price: Double,
    @Json(name = "currency") val currency: String,
    @Json(name = "category") val category: String,
    @Json(name = "description") val description: String,
    @Json(name = "sourceStore") val sourceStore: String,
    @Json(name = "imageUrl") val imageUrl: String,
    @Json(name = "rating") val rating: Float,
    @Json(name = "summaryMd") val summaryMd: String,
    
    // Comparison metrics
    @Json(name = "quality") val quality: Int, // 1-5
    @Json(name = "pros") val pros: String, // Comma separated pros
    @Json(name = "contras") val contras: String, // Comma separated cons
    @Json(name = "score") val score: Int // 1-100 score
)
