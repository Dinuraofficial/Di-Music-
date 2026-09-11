package com.example.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class YouTubeAuthManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(hasValidCookies())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    companion object {
        private const val TAG = "MusicDebug"
        private const val PREFS_NAME = "yt_auth_preferences"
        private const val KEY_COOKIES = "yt_cookies"

        @Volatile
        private var instance: YouTubeAuthManager? = null

        fun getInstance(context: Context): YouTubeAuthManager {
            return instance ?: synchronized(this) {
                instance ?: YouTubeAuthManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getCookies(): String? {
        val cookies = prefs.getString(KEY_COOKIES, null)
        return if (!cookies.isNullOrBlank()) cookies else null
    }

    fun saveCookies(cookies: String) {
        val trimmed = cookies.trim()
        prefs.edit().putString(KEY_COOKIES, trimmed).apply()
        val valid = hasValidCookies(trimmed)
        _isLoggedIn.value = valid
        Log.d(TAG, "YouTube cookies updated. Has auth: $valid")
    }

    fun clearAuth() {
        prefs.edit().remove(KEY_COOKIES).apply()
        _isLoggedIn.value = false
        Log.d(TAG, "YouTube cookies cleared")
    }

    fun hasAuth(): Boolean {
        return _isLoggedIn.value
    }

    private fun hasValidCookies(cookies: String? = getCookies()): Boolean {
        if (cookies.isNullOrBlank()) return false
        return cookies.contains("SAPISID") ||
                cookies.contains("__Secure-3PAPISID") ||
                cookies.contains("LOGIN_INFO") ||
                cookies.contains("SID")
    }

    /**
     * Calculates the SAPISIDHASH required by YouTube's authenticated InnerTube requests.
     * Formula: sha1("${timestamp} ${sapisid} ${origin}")
     */
    fun getAuthorizationHeader(origin: String = "https://www.youtube.com"): String? {
        val cookies = getCookies() ?: return null
        val sapisid = extractCookie(cookies, "SAPISID")
            ?: extractCookie(cookies, "__Secure-3PAPISID")
            ?: extractCookie(cookies, "__Secure-1PAPISID")
            ?: return null

        val timestamp = System.currentTimeMillis() / 1000
        val payload = "$timestamp $sapisid $origin"
        val hash = sha1(payload)
        return "SAPISIDHASH ${timestamp}_$hash"
    }

    private fun extractCookie(cookieString: String, name: String): String? {
        val pattern = "(?:^|;\\s*)$name=([^;]*)".toRegex()
        val match = pattern.find(cookieString)
        return match?.groupValues?.getOrNull(1)
    }

    private fun sha1(input: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
