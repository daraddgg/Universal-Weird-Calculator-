package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

object GeminiApiHelper {
    private const val SYSTEM_PROMPT = 
        "You are the witty, brilliant AI mind of the 'Universal Weird Calculator' Android app. " +
        "Your goal is to parse unit or comparative conversion concerns (e.g., 'How many cats weigh 500 kg?', " +
        "'how many movies fit in 1 TB?', 'distance of 5 km in Eiffel Towers'), do precise calculations, " +
        "and present standard conversions alongside highly educational, funny, bizarre real-world body/animal/landmark scales. " +
        "Always structure your response with: " +
        "1. A direct, clear mathematical answer as the title/headline (e.g. '500 kg is exactly 111.1 Domestic Cats!'). " +
        "2. A couple of supplementary bizarre scales (e.g. 'That is also equal to 2,500 apples, or 0.1 Savanna Elephants!'). " +
        "3. A playful, educational comment or fun fact under a '💡 Weird Fact:' heading. " +
        "Keep total response concise, structured, and easy to read in a mobile card. Be witty and fun!"

    suspend fun askGemini(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API KEY MISSING: Please register your Google Gemini API Key in the AI Studio Secrets Panel to enable real-time AI conversions!"
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = SYSTEM_PROMPT)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val textResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            textResult ?: "The AI parsed your query but couldn't formulate a direct comparison. Please try another phrase!"
        } catch (e: Exception) {
            "API Connection Error: ${e.message}"
        }
    }
}
