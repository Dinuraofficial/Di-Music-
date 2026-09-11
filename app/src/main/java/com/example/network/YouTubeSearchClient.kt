package com.example.network

import android.util.Log
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class YouTubeSearchClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "MusicDebug"
        private const val SEARCH_URL = "https://music.youtube.com/youtubei/v1/search?prettyPrint=false"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    suspend fun searchSongs(query: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val requestBodyJson = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB_REMIX")
                        put("clientVersion", "1.20240101.01.00")
                        put("hl", "en")
                        put("gl", "US")
                    })
                })
                put("query", query)
                put("params", "Eg-KAQwIARAAGAAgACgAMABqChAEEAMQCRAFEAo%3D") // Filter for tracks/songs
            }

            val request = Request.Builder()
                .url(SEARCH_URL)
                .post(requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Referer", "https://music.youtube.com/")
                .header("Origin", "https://music.youtube.com")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val err = "Search request failed with HTTP ${response.code}"
                Log.e(TAG, err)
                return@withContext Result.failure(Exception(err))
            }

            val bodyString = response.body?.string() ?: ""
            val songs = parseSearchResponse(bodyString)
            Log.d(TAG, "Search '$query' found ${songs.size} tracks")
            Result.success(songs)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing search for '$query'", e)
            Result.failure(e)
        }
    }

    suspend fun getHistory(authManager: com.example.network.YouTubeAuthManager? = null): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val requestBodyJson = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB_REMIX")
                        put("clientVersion", "1.20240101.01.00")
                        put("hl", "en")
                        put("gl", "US")
                    })
                })
                put("browseId", "FEmusic_history")
            }
            val requestBuilder = Request.Builder()
                .url("https://music.youtube.com/youtubei/v1/browse?prettyPrint=false")
                .post(requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Referer", "https://music.youtube.com/")
                .header("Origin", "https://music.youtube.com")

            authManager?.getCookies()?.let { cookies ->
                requestBuilder.header("Cookie", cookies)
            }
            authManager?.getAuthorizationHeader("https://music.youtube.com")?.let { auth ->
                requestBuilder.header("Authorization", auth)
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Browse history failed with HTTP ${response.code}"))
            }

            val bodyString = response.body?.string() ?: ""
            val songs = parseSearchResponse(bodyString)
            Log.d(TAG, "History found ${songs.size} tracks")
            Result.success(songs)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing getHistory", e)
            Result.failure(e)
        }
    }

    suspend fun getUpNext(videoId: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val requestBodyJson = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB_REMIX")
                        put("clientVersion", "1.20240101.01.00")
                        put("hl", "en")
                        put("gl", "US")
                    })
                })
                put("videoId", videoId)
            }
            val request = Request.Builder()
                .url("https://music.youtube.com/youtubei/v1/next?prettyPrint=false")
                .post(requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Referer", "https://music.youtube.com/")
                .header("Origin", "https://music.youtube.com")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Next request failed with HTTP ${response.code}"))
            }

            val bodyString = response.body?.string() ?: ""
            val songs = parseSearchResponse(bodyString)
            Log.d(TAG, "Up Next for '$videoId' found ${songs.size} tracks")
            Result.success(songs)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing getUpNext for '$videoId'", e)
            Result.failure(e)
        }
    }

    private fun parseSearchResponse(jsonString: String): List<Song> {
        val songs = mutableListOf<Song>()
        val seenVideoIds = mutableSetOf<String>()

        try {
            val root = JSONObject(jsonString)
            findResponsiveItems(root, songs, seenVideoIds)
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing search response", e)
        }
        return songs
    }

    private fun findResponsiveItems(
        obj: Any,
        outList: MutableList<Song>,
        seenIds: MutableSet<String>
    ) {
        when (obj) {
            is JSONObject -> {
                if (obj.has("musicResponsiveListItemRenderer")) {
                    val item = obj.getJSONObject("musicResponsiveListItemRenderer")
                    extractSongFromRenderer(item)?.let { song ->
                        if (seenIds.add(song.videoId)) {
                            outList.add(song)
                        }
                    }
                } else if (obj.has("playlistPanelVideoRenderer")) {
                    val item = obj.getJSONObject("playlistPanelVideoRenderer")
                    extractSongFromPlaylistPanel(item)?.let { song ->
                        if (seenIds.add(song.videoId)) {
                            outList.add(song)
                        }
                    }
                } else {
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        findResponsiveItems(obj.get(key), outList, seenIds)
                    }
                }
            }
            is JSONArray -> {
                for (i in 0 until obj.length()) {
                    val item = obj.get(i)
                    findResponsiveItems(item, outList, seenIds)
                }
            }
        }
    }

    private fun extractSongFromPlaylistPanel(renderer: JSONObject): Song? {
        val videoId = renderer.optString("videoId", null) ?: return null
        
        // Extract Title
        var title = "Unknown Title"
        val titleRuns = renderer.optJSONObject("title")?.optJSONArray("runs")
        if (titleRuns != null && titleRuns.length() > 0) {
            title = titleRuns.optJSONObject(0)?.optString("text", title) ?: title
        }
        
        // Extract Artist and Duration
        var artist = "Unknown Artist"
        var durationText: String? = null
        val longBylineTextRuns = renderer.optJSONObject("longBylineText")?.optJSONArray("runs")
        if (longBylineTextRuns != null && longBylineTextRuns.length() > 0) {
            artist = longBylineTextRuns.optJSONObject(0)?.optString("text", artist) ?: artist
        }
        val lengthText = renderer.optJSONObject("lengthText")?.optJSONArray("runs")
        if (lengthText != null && lengthText.length() > 0) {
            durationText = lengthText.optJSONObject(0)?.optString("text", "")
        }

        // Extract Thumbnail URL
        var thumbUrl = ""
        val thumbObj = renderer.optJSONObject("thumbnail")
            ?.optJSONArray("thumbnails")
        if (thumbObj != null && thumbObj.length() > 0) {
            val lastThumb = thumbObj.optJSONObject(thumbObj.length() - 1)
            thumbUrl = lastThumb?.optString("url", "") ?: ""
        }
        if (thumbUrl.isEmpty()) {
            thumbUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
        }

        return Song(
            videoId = videoId,
            title = title,
            artist = artist,
            thumbnailUrl = thumbUrl,
            durationText = durationText
        )
    }

    private fun extractSongFromRenderer(renderer: JSONObject): Song? {
        var videoId: String? = null

        // Method 1: playlistItemData
        if (renderer.has("playlistItemData")) {
            videoId = renderer.getJSONObject("playlistItemData").optString("videoId", null)
        }

        // Method 2: overlay -> play button
        if (videoId.isNullOrEmpty() && renderer.has("overlay")) {
            val overlay = renderer.optJSONObject("overlay")
            val playBtn = overlay?.optJSONObject("musicItemThumbnailOverlayRenderer")
                ?.optJSONObject("content")
                ?.optJSONObject("musicPlayButtonRenderer")
            videoId = playBtn?.optJSONObject("playNavigationEndpoint")
                ?.optJSONObject("watchEndpoint")
                ?.optString("videoId", null)
        }

        // Method 3: flexColumns navigationEndpoint
        val flexColumns = renderer.optJSONArray("flexColumns") ?: return null
        if (videoId.isNullOrEmpty() && flexColumns.length() > 0) {
            val col0 = flexColumns.optJSONObject(0)
            val runs = col0?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")
                ?.optJSONArray("runs")
            val firstRun = runs?.optJSONObject(0)
            videoId = firstRun?.optJSONObject("navigationEndpoint")
                ?.optJSONObject("watchEndpoint")
                ?.optString("videoId", null)
        }

        if (videoId.isNullOrEmpty()) return null

        // Extract Title
        var title = "Unknown Title"
        if (flexColumns.length() > 0) {
            val col0 = flexColumns.optJSONObject(0)
            val runs = col0?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")
                ?.optJSONArray("runs")
            if (runs != null && runs.length() > 0) {
                title = runs.optJSONObject(0)?.optString("text", title) ?: title
            }
        }

        // Extract Artist and Duration
        var artist = "Unknown Artist"
        var durationText: String? = null
        if (flexColumns.length() > 1) {
            val col1 = flexColumns.optJSONObject(1)
            val runs = col1?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")
                ?.optJSONArray("runs")
            if (runs != null && runs.length() > 0) {
                val artistRun = runs.optJSONObject(0)
                artist = artistRun?.optString("text", artist) ?: artist

                // Look for duration (often the last run)
                for (r in (runs.length() - 1) downTo 0) {
                    val t = runs.optJSONObject(r)?.optString("text", "") ?: ""
                    if (t.matches(Regex("""\d{1,2}:\d{2}"""))) {
                        durationText = t
                        break
                    }
                }
            }
        }

        // Extract Thumbnail URL
        var thumbUrl = ""
        val thumbObj = renderer.optJSONObject("thumbnail")
            ?.optJSONObject("musicThumbnailRenderer")
            ?.optJSONObject("thumbnail")
            ?.optJSONArray("thumbnails")
        if (thumbObj != null && thumbObj.length() > 0) {
            val lastThumb = thumbObj.optJSONObject(thumbObj.length() - 1)
            thumbUrl = lastThumb?.optString("url", "") ?: ""
        }

        if (thumbUrl.isEmpty()) {
            thumbUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
        }

        return Song(
            videoId = videoId,
            title = title,
            artist = artist,
            thumbnailUrl = thumbUrl,
            durationText = durationText
        )
    }
}
