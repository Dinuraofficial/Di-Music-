package com.example.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class YouTubeLoginRequiredException(message: String = "YouTube requires account sign-in to play this track.") : Exception(message)

class YouTubeStreamResolver(
    private val authManager: YouTubeAuthManager? = null,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "MusicDebug"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private const val YOUTUBE_PLAYER_URL = "https://www.youtube.com/youtubei/v1/player?prettyPrint=false"
        private const val GOOGLEAPIS_PLAYER_URL = "https://youtubei.googleapis.com/youtubei/v1/player?prettyPrint=false"
        private const val VISITOR_DATA_URL = "https://www.youtube.com/sw.js_data"

        @Volatile
        private var cachedVisitorData: String? = null
        @Volatile
        private var visitorDataFetchTime: Long = 0L

        fun create(context: Context): YouTubeStreamResolver {
            return YouTubeStreamResolver(
                authManager = YouTubeAuthManager.getInstance(context)
            )
        }
    }

    suspend fun resolveStreamUrl(videoId: String): Result<String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Resolving stream for videoId=$videoId (HasAuth=${authManager?.hasAuth()})")

        // Ensure we have a valid visitorData token
        val visitorData = getOrFetchVisitorData()

        // Attempt resolution with configured clients
        val directResult = tryDirectPlayerApi(videoId, visitorData)
        if (directResult.isSuccess) {
            val url = directResult.getOrThrow()
            Log.d(TAG, "Resolved stream URL successfully: ${url.take(80)}...")
            return@withContext Result.success(url)
        }

        val err = directResult.exceptionOrNull()
        if (err is YouTubeLoginRequiredException) {
            Log.w(TAG, "Login required for videoId '$videoId': ${err.message}")
            return@withContext Result.failure(err)
        }

        val finalError = "Failed to resolve stream for videoId '$videoId': ${err?.message}"
        Log.e(TAG, finalError)
        Result.failure(Exception(finalError))
    }

    private fun getOrFetchVisitorData(): String {
        val now = System.currentTimeMillis()
        val cached = cachedVisitorData
        if (cached != null && (now - visitorDataFetchTime) < 3_600_000L) {
            return cached
        }

        return try {
            val req = Request.Builder()
                .url(VISITOR_DATA_URL)
                .header("User-Agent", "com.google.android.youtube/20.10.38 (Linux; U; ANDROID 11) gzip")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(req).execute()
            if (response.isSuccessful) {
                var body = response.body?.string() ?: ""
                if (body.startsWith(")]}'")) {
                    body = body.substring(4)
                }
                val arr = JSONArray(body)
                val token = arr.getJSONArray(0)
                    .getJSONArray(2)
                    .getJSONArray(0)
                    .getJSONArray(0)
                    .getString(13)
                if (!token.isNullOrEmpty()) {
                    cachedVisitorData = token
                    visitorDataFetchTime = now
                    Log.d(TAG, "Fetched fresh visitorData: ${token.take(20)}...")
                    return token
                }
            }
            ""
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch visitorData: ${e.message}")
            ""
        }
    }

    private data class ClientProfile(
        val clientName: String,
        val clientVersion: String,
        val deviceMake: String? = null,
        val deviceModel: String? = null,
        val osName: String? = null,
        val osVersion: String? = null,
        val androidSdkVersion: Int? = null,
        val userAgent: String,
        val playerEndpoint: String = YOUTUBE_PLAYER_URL
    )

    private fun tryDirectPlayerApi(videoId: String, visitorData: String): Result<String> {
        val cpn = UUID.randomUUID().toString().replace("-", "").take(16)
        val cookies = authManager?.getCookies()
        val authHeader = authManager?.getAuthorizationHeader()

        val clients = listOf(
            // Profile 1: VISIONOS (Apple Reality OS - high success rate for music & standard video)
            ClientProfile(
                clientName = "VISIONOS",
                clientVersion = "1.04",
                deviceMake = "Apple",
                deviceModel = "RealityDevice17,1",
                osName = "visionOS",
                osVersion = "26.5.23O471",
                userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15"
            ),
            // Profile 2: ANDROID_MUSIC (Official YouTube Music client)
            ClientProfile(
                clientName = "ANDROID_MUSIC",
                clientVersion = "6.42.52",
                osName = "Android",
                osVersion = "14",
                androidSdkVersion = 34,
                userAgent = "com.google.android.apps.youtube.music/6.42.52 (Linux; U; Android 14; en_US) gzip"
            ),
            // Profile 3: IOS (Official iOS YouTube client)
            ClientProfile(
                clientName = "IOS",
                clientVersion = "19.29.1",
                deviceMake = "Apple",
                deviceModel = "iPhone16,2",
                osName = "iOS",
                osVersion = "17.5.1.21F90",
                userAgent = "com.google.ios.youtube/19.29.1 (iPhone16,2; U; CPU iOS 17_5_1 like Mac OS X; en_US)"
            ),
            // Profile 4: WEB_REMIX (YouTube Music Web)
            ClientProfile(
                clientName = "WEB_REMIX",
                clientVersion = "1.20240901.01.00",
                userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
            ),
            // Profile 5: TVHTML5_SIMPLY_EMBEDDED_PLAYER (Fallback TV / Embed)
            ClientProfile(
                clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
                clientVersion = "2.0",
                userAgent = "Mozilla/5.0 (Web0S; SmartTV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.79 Safari/537.36 WebAppManager",
                playerEndpoint = GOOGLEAPIS_PLAYER_URL
            )
        )

        var loginRequiredEncountered = false
        var lastErrorMessage = "No stream found"

        for (profile in clients) {
            try {
                val clientObj = JSONObject().apply {
                    put("clientName", profile.clientName)
                    put("clientVersion", profile.clientVersion)
                    put("hl", "en")
                    put("gl", "US")
                    put("utcOffsetMinutes", 0)

                    if (!visitorData.isNullOrEmpty()) {
                        put("visitorData", visitorData)
                    }
                    profile.deviceMake?.let { put("deviceMake", it) }
                    profile.deviceModel?.let { put("deviceModel", it) }
                    profile.osName?.let { put("osName", it) }
                    profile.osVersion?.let { put("osVersion", it) }
                    profile.androidSdkVersion?.let { put("androidSdkVersion", it) }
                }

                val bodyJson = JSONObject().apply {
                    put("context", JSONObject().apply {
                        put("client", clientObj)
                    })
                    put("videoId", videoId)
                    put("cpn", cpn)
                    put("contentCheckOk", true)
                    put("racyCheckOk", true)
                }

                val requestBuilder = Request.Builder()
                    .url(profile.playerEndpoint)
                    .post(bodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", profile.userAgent)
                    .header("X-Goog-Api-Format-Version", "2")

                // Add authentication headers if cookies are available
                if (!cookies.isNullOrBlank()) {
                    requestBuilder.header("Cookie", cookies)
                    requestBuilder.header("Origin", "https://www.youtube.com")
                    requestBuilder.header("Referer", "https://www.youtube.com/")
                }
                if (!authHeader.isNullOrBlank()) {
                    requestBuilder.header("Authorization", authHeader)
                }

                val response = client.newCall(requestBuilder.build()).execute()
                if (!response.isSuccessful) {
                    Log.w(TAG, "Client ${profile.clientName} returned HTTP ${response.code}")
                    continue
                }

                val responseStr = response.body?.string() ?: continue
                val root = JSONObject(responseStr)

                val playabilityStatus = root.optJSONObject("playabilityStatus")
                val status = playabilityStatus?.optString("status", "UNKNOWN") ?: "UNKNOWN"

                if (status == "LOGIN_REQUIRED") {
                    val reason = playabilityStatus?.optString("reason", "Sign in required")
                    Log.w(TAG, "Client ${profile.clientName} status=LOGIN_REQUIRED ($reason)")
                    loginRequiredEncountered = true
                    lastErrorMessage = reason ?: "Sign in required"
                    continue
                }

                if (status != "OK") {
                    val reason = playabilityStatus?.optString("reason", status) ?: status
                    Log.w(TAG, "Client ${profile.clientName} status=$status ($reason)")
                    lastErrorMessage = reason
                    continue
                }

                val streamingData = root.optJSONObject("streamingData") ?: continue
                val adaptiveFormats = streamingData.optJSONArray("adaptiveFormats")

                var bestUrl: String? = null
                var highestBitrate = -1

                if (adaptiveFormats != null) {
                    for (i in 0 until adaptiveFormats.length()) {
                        val format = adaptiveFormats.getJSONObject(i)
                        val mimeType = format.optString("mimeType", "")
                        val url = format.optString("url", "")
                        val bitrate = format.optInt("bitrate", 0)

                        if (url.isNotEmpty() && mimeType.startsWith("audio/")) {
                            if (bitrate > highestBitrate) {
                                highestBitrate = bitrate
                                bestUrl = url
                            }
                        }
                    }
                }

                if (bestUrl != null) {
                    Log.d(TAG, "Direct stream resolved using client ${profile.clientName} (bitrate=$highestBitrate)")
                    return Result.success(bestUrl)
                }

                // Fallback to HLS manifest if present
                val hlsUrl = streamingData.optString("hlsManifestUrl", "")
                if (hlsUrl.isNotEmpty()) {
                    Log.d(TAG, "Direct HLS manifest resolved using client ${profile.clientName}")
                    return Result.success(hlsUrl)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Client ${profile.clientName} failed with exception: ${e.message}")
            }
        }

        if (loginRequiredEncountered && authManager?.hasAuth() != true) {
            return Result.failure(YouTubeLoginRequiredException(lastErrorMessage))
        }

        return Result.failure(Exception(lastErrorMessage))
    }
}
