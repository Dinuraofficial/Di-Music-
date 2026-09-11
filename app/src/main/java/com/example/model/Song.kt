package com.example.model

import androidx.annotation.Keep

@Keep
data class Song(
    val videoId: String = "",
    val title: String,
    val artist: String,
    val thumbnailUrl: String = "",
    val durationText: String? = null,
    val artUrl: String = thumbnailUrl
) {
    val hdArtUrl: String
        get() = getHdImageUrl(artUrl.ifEmpty { thumbnailUrl }, videoId)
}

fun getHdImageUrl(rawUrl: String, videoId: String = ""): String {
    if (rawUrl.isEmpty()) {
        return if (videoId.isNotEmpty()) "https://i.ytimg.com/vi/$videoId/maxresdefault.jpg" else ""
    }

    var transformed = rawUrl

    // 1. YouTube Music dynamic sizing parameters
    if (transformed.contains("googleusercontent.com") || transformed.contains("ggpht.com") || transformed.contains("ytimg.com")) {
        // Upgrade low resolution dimension tokens to full 1080p
        // Matches =w... or =s... at the end of the URL and replaces it
        if (transformed.contains("=")) {
            val base = transformed.substringBeforeLast("=")
            transformed = "$base=w1080-h1080-l90-rj"
        }
    }

    // 2. YouTube standard thumbnail formats
    if (transformed.contains("/hqdefault.jpg")) {
        transformed = transformed.replace("/hqdefault.jpg", "/maxresdefault.jpg")
    } else if (transformed.contains("/mqdefault.jpg")) {
        transformed = transformed.replace("/mqdefault.jpg", "/maxresdefault.jpg")
    } else if (transformed.contains("/default.jpg")) {
        transformed = transformed.replace("/default.jpg", "/maxresdefault.jpg")
    }

    // 3. Unsplash URLs
    if (transformed.contains("images.unsplash.com")) {
        transformed = if (transformed.contains("?")) {
            transformed.replace(Regex("""[?&]w=\d+"""), "?w=1200").replace(Regex("""[?&]q=\d+"""), "&q=90")
        } else {
            "$transformed?w=1200&q=90"
        }
    }

    return transformed
}

@Keep
data class Album(
    val id: String = "",
    val title: String,
    val artist: String,
    val artUrl: String = "",
    val year: String? = null,
    val songCount: Int = 0
)

@Keep
data class Artist(
    val id: String = "",
    val name: String,
    val imageUrl: String = "",
    val subscribers: String? = null,
    val monthlyListeners: String? = null
)

