package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class StreamInfo(
    val name: String,
    val url: String
)

data class ExtractedStreamResult(
    val streamUrls: List<StreamInfo>,
    val source: String,
    val status: String
)

object GeminiStreamService {
    private const val TAG = "GeminiStreamService"
    private const val GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Tries to fetch the HTML content of the Ze Sport website in the background
     */
    private suspend fun fetchZeSportHtml(targetUrl: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(targetUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .build()
        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string() ?: ""
                } else {
                    Log.w(TAG, "Failed to fetch HTML from $targetUrl: Code ${response.code}")
                    ""
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching HTML from $targetUrl", e)
            ""
        }
    }

    /**
     * Regex fallback parser in case Gemini is unavailable or rate limited.
     * Looks for common patterns such as iFrame sources, .m3u8 files, or streaming urls.
     */
    private fun parseStreamsFromHtml(html: String): List<StreamInfo> {
        val streams = mutableListOf<StreamInfo>()
        if (html.isEmpty()) return streams

        // Find .m3u8 source URLs
        val m3u8Matcher = Pattern.compile("https?://[^\"'\\s]+\\.m3u8[^\"'\\s]*").matcher(html)
        var serverIndex = 1
        while (m3u8Matcher.find() && streams.size < 3) {
            val url = m3u8Matcher.group()
            if (streams.none { it.url == url }) {
                streams.add(StreamInfo("Regex Server $serverIndex", url))
                serverIndex++
            }
        }

        // Find iframe player sources
        val iframeMatcher = Pattern.compile("<iframe[^>]+src=\"([^\"]+)\"").matcher(html)
        while (iframeMatcher.find() && streams.size < 3) {
            val url = iframeMatcher.group(1) ?: continue
            if (streams.none { it.url == url }) {
                streams.add(StreamInfo("Regex Player $serverIndex", url))
                serverIndex++
            }
        }

        return streams
    }

    /**
     * Resolves the latest stream URLs for a channel or match.
     * It fetches the main page or channel page on ze-sport.com, passes it to the Gemini API with instructions,
     * and gets the structured URLs back.
     */
    suspend fun resolveStreamUrls(
        id: String,
        title: String,
        subtitle: String,
        type: String // "Channel" or "Match"
    ): ExtractedStreamResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val cleanTitle = title.replace(" – World Cup", "").trim()
        val targetUrl = "https://ze-sport.com"

        Log.d(TAG, "Resolving stream for $type: $cleanTitle (ID: $id)")

        // 1. Fetch raw HTML from site
        val rawHtml = fetchZeSportHtml(targetUrl)
        val localRegexStreams = parseStreamsFromHtml(rawHtml)

        // Trim HTML to avoid exceeding token limit (get first 25,000 characters which usually contain streams/match list)
        val snippet = if (rawHtml.length > 25000) rawHtml.substring(0, 25000) else rawHtml

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is missing or is the default placeholder. Using local regex and fallback streams.")
            return@withContext buildFallbackResult(localRegexStreams, targetUrl, "Fallback (No API Key)")
        }

        // 2. Query Gemini to intelligently extract/find working stream URLs for this channel/match
        val prompt = """
            You are a smart sports stream extractor for our 'Ze Sport' app. We have fetched the following HTML snippet directly from the original site 'https://ze-sport.com':
            
            === HTML SNIPPET ===
            $snippet
            === END HTML SNIPPET ===
            
            We are looking for stream HLS (.m3u8), iframe players, or player URLs for:
            Type: $type
            Title: $title
            Subtitle: $subtitle
            App ID: $id

            Tasks:
            1. Analyze the HTML snippet to see if there is an active stream link, iframe, or .m3u8 source that corresponds to "$cleanTitle" or "$subtitle".
            2. If found, extract these URLs.
            3. If not found in the HTML snippet, use your up-to-date web intelligence or knowledge of ze-sport.com streams to provide the actual streaming/mirror player URLs usually used by ze-sport.com or related sports streamers for this kind of channel/match.
            4. If no exact real-time stream is available (e.g. because the match hasn't started or is offline), provide working alternate public sports-related HLS streams (e.g. Mux HLS test streams, public HLS/m3u8 sport channels, or generic high-quality live feeds) so that our player has content to stream.

            Please output your answer strictly as a JSON object with this exact structure:
            {
              "streamUrls": [
                { "name": "Server 1 (Direct)", "url": "M3U8_OR_PLAYER_URL" },
                { "name": "Server 2 (Mirror)", "url": "M3U8_OR_PLAYER_URL_2" }
              ],
              "source": "https://ze-sport.com/stream-source-page",
              "status": "Success"
            }

            Rules:
            - Return ONLY valid JSON.
            - Do NOT wrap your response in markdown code blocks like ```json ... ```. Just return the raw JSON string starting with { and ending with }.
            - Do not include any notes, explanations or text.
        """.trimIndent()

        try {
            val requestBodyJson = buildGeminiRequestBody(prompt)
            val request = Request.Builder()
                .url(GEMINI_URL + "?key=" + apiKey)
                .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    Log.d(TAG, "Gemini raw response: $responseBody")
                    val parsedResult = parseGeminiResponse(responseBody)
                    if (parsedResult != null && parsedResult.streamUrls.isNotEmpty()) {
                        return@withContext parsedResult
                    }
                } else {
                    Log.e(TAG, "Gemini API failed with response code ${response.code}: ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving stream URL via Gemini API", e)
        }

        // Fallback to local regex streams or standard backup URLs
        return@withContext buildFallbackResult(localRegexStreams, targetUrl, "Regex/Fallback Mode")
    }

    private fun buildFallbackResult(
        regexStreams: List<StreamInfo>,
        targetUrl: String,
        status: String
    ): ExtractedStreamResult {
        val finalStreams = mutableListOf<StreamInfo>()
        finalStreams.addAll(regexStreams)

        // Add standard high-quality working public live streams as fallbacks
        if (finalStreams.none { it.url.contains("mux.dev") }) {
            finalStreams.add(StreamInfo("Server 1 (HLS Live)", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"))
        }
        if (finalStreams.size < 2) {
            finalStreams.add(StreamInfo("Server 2 (HLS Backup)", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"))
        }
        if (finalStreams.size < 3) {
            finalStreams.add(StreamInfo("Server 3 (HD Playback)", "https://playertest.longtailvideo.com/adaptive/bipbop/bipbop.m3u8"))
        }

        return ExtractedStreamResult(
            streamUrls = finalStreams.take(3),
            source = targetUrl,
            status = status
        )
    }

    private fun buildGeminiRequestBody(prompt: String): String {
        val requestMap = mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf("text" to prompt)
                    )
                )
            ),
            "generationConfig" to mapOf(
                "responseMimeType" to "application/json"
            )
        )
        return moshi.adapter(Map::class.java).toJson(requestMap)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseGeminiResponse(responseJson: String): ExtractedStreamResult? {
        return try {
            val responseMap = moshi.adapter(Map::class.java).fromJson(responseJson) ?: return null
            val candidates = responseMap["candidates"] as? List<Map<String, Any>> ?: return null
            val firstCandidate = candidates.firstOrNull() ?: return null
            val content = firstCandidate["content"] as? Map<String, Any> ?: return null
            val parts = content["parts"] as? List<Map<String, Any>> ?: return null
            val firstPart = parts.firstOrNull() ?: return null
            val text = firstPart["text"] as? String ?: return null

            // Clean JSON string in case the model ignored rules and wrapped in markdown anyway
            val cleanedText = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonResultMap = moshi.adapter(Map::class.java).fromJson(cleanedText) ?: return null
            val streamUrlsRaw = jsonResultMap["streamUrls"] as? List<Map<String, String>> ?: emptyList()
            val streamUrls = streamUrlsRaw.map {
                StreamInfo(
                    name = it["name"] ?: "Server",
                    url = it["url"] ?: ""
                )
            }.filter { it.url.isNotEmpty() }

            ExtractedStreamResult(
                streamUrls = streamUrls,
                source = jsonResultMap["source"] as? String ?: "https://ze-sport.com",
                status = jsonResultMap["status"] as? String ?: "Success"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response: $responseJson", e)
            null
        }
    }
}
