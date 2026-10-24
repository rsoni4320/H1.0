package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient(
    private val getApiKey: () -> String,
    private val getModel: () -> String,
    private val getAssistantName: () -> String,
    private val getLanguage: () -> String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateResponse(
        userPrompt: String,
        history: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val configuredKey = getApiKey().ifBlank {
            try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Throwable) {
                ""
            }
        }

        if (configuredKey.isBlank() || configuredKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("API_KEY_MISSING: कृपया Settings > AI Configuration Center में अपनी Gemini API Key दर्ज करें।")
            )
        }

        val model = getModel().ifBlank { "gemini-2.5-flash" }
        val assistantName = getAssistantName().ifBlank { "Heena AI" }
        val lang = getLanguage()

        val systemPrompt = """
            You are $assistantName, a friendly, intelligent, polite, and expressive female anime-style AI assistant on an Android phone.
            Primary language: Hindi and Hinglish. You also speak English naturally.
            Always be warm, cute, respectful, and helpful.
            
            Action Detection Protocol:
            When the user asks to launch an app, play music, set a timer, make a call, or search, you MUST provide a friendly spoken reply in Hindi/Hinglish AND append an action command tag at the very end of your response in this exact format:
            [[ACTION: <ACTION_TYPE> | <PARAM>]]
            
            Supported ACTION_TYPEs:
            - OPEN_APP | com.google.android.youtube (or youtube, chrome, maps, camera, gallery, whatsapp, settings, dialer, calculator)
            - SEARCH_YOUTUBE | <search query>
            - SEARCH_WEB | <search query>
            - OPEN_MAPS | <location query>
            - MAKE_CALL | <phone number or name>
            - SEND_MESSAGE | <message draft>
            - SET_TIMER | <seconds, e.g. 300 for 5 min>
            - SHOW_BRIEFING | today
            
            Example:
            User: "YouTube kholo aur cricket highlights search karo"
            Response: "ज़रूर! मैं आपके लिए YouTube खोल रही हूँ और क्रिकेट हाइलाइट्स सर्च कर रही हूँ। [[ACTION: SEARCH_YOUTUBE | cricket highlights]]"
        """.trimIndent()

        try {
            val rootJson = JSONObject()

            // System instructions
            val systemObj = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                }
                put("parts", parts)
            }
            rootJson.put("systemInstruction", systemObj)

            // Contents array
            val contentsArray = JSONArray()

            // Add previous context (last 6 turns for prompt hygiene)
            val recentHistory = history.takeLast(6)
            for ((role, text) in recentHistory) {
                val roleName = if (role.equals("user", ignoreCase = true)) "user" else "model"
                val contentObj = JSONObject().apply {
                    put("role", roleName)
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", text))
                    }
                    put("parts", parts)
                }
                contentsArray.put(contentObj)
            }

            // Current message
            val currentMsg = JSONObject().apply {
                put("role", "user")
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", userPrompt))
                }
                put("parts", parts)
            }
            contentsArray.put(currentMsg)

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
                put("topK", 40)
            }
            rootJson.put("generationConfig", genConfig)

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$configuredKey"
            val requestBody = rootJson.toString().toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errObj = JSONObject(bodyString).optJSONObject("error")
                        errObj?.optString("message") ?: "HTTP ${response.code}"
                    } catch (e: Exception) {
                        "HTTP ${response.code}: $bodyString"
                    }
                    return@withContext Result.failure(Exception("Gemini API Error: $errorMsg"))
                }

                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val replyText = parts.getJSONObject(0).optString("text", "")
                        return@withContext Result.success(replyText)
                    }
                }
                Result.failure(Exception("Empty response from AI model"))
            }
        } catch (e: Exception) {
            Log.e("GeminiClient", "API call failed", e)
            Result.failure(e)
        }
    }

    suspend fun testConnection(apiKey: String, model: String): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKey.ifBlank {
            try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Throwable) {
                ""
            }
        }
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("API Key is missing or default placeholder"))
        }

        val testModel = model.ifBlank { "gemini-2.5-flash" }
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$testModel:generateContent?key=$key"

        val pingJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "Say 'Connected successfully'"))
                    })
                })
            }
            put("contents", contents)
        }

        try {
            val request = Request.Builder()
                .url(endpoint)
                .post(pingJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success("Connection test successful! Model: $testModel")
                } else {
                    Result.failure(Exception("Failed (HTTP ${response.code}): $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
