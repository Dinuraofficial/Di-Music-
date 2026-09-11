package com.example.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.model.Song
import com.example.network.YouTubeAuthManager
import com.example.network.YouTubeLoginRequiredException
import com.example.network.YouTubeStreamResolver
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val playbackState: Int = Player.STATE_IDLE,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isResolvingStream: Boolean = false,
    val errorMessage: String? = null,
    val isLoginRequired: Boolean = false,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = -1
)

class PlayerController(
    private val context: Context,
    private val streamResolver: YouTubeStreamResolver = YouTubeStreamResolver.create(context)
) {
    companion object {
        private const val TAG = "MusicDebug"

        @Volatile
        private var instance: PlayerController? = null

        fun getInstance(context: Context): PlayerController {
            return instance ?: synchronized(this) {
                instance ?: PlayerController(context.applicationContext).also { instance = it }
            }
        }
    }

    private val searchClient = com.example.network.YouTubeSearchClient()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlayerService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future

        future.addListener({
            try {
                val mediaController = future.get()
                controller = mediaController
                attachPlayerListener(mediaController)
                syncState(mediaController)
                Log.d(TAG, "MediaController connected successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect MediaController", e)
            }
        }, MoreExecutors.directExecutor())
    }

    private fun attachPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val duration = if (playbackState == Player.STATE_READY) player.duration.coerceAtLeast(0L) else _uiState.value.durationMs
                _uiState.update {
                    it.copy(
                        playbackState = playbackState,
                        durationMs = duration
                    )
                }
                if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Player encountered error: ${error.message}", error)
                _uiState.update {
                    it.copy(
                        isResolvingStream = false,
                        isPlaying = false,
                        errorMessage = "Playback error: ${error.message}"
                    )
                }
            }
        })
    }

    private fun syncState(player: Player) {
        _uiState.update {
            it.copy(
                isPlaying = player.isPlaying,
                playbackState = player.playbackState,
                durationMs = player.duration.coerceAtLeast(0L),
                currentPositionMs = player.currentPosition.coerceAtLeast(0L)
            )
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                controller?.let { c ->
                    if (c.isPlaying) {
                        _uiState.update {
                            it.copy(
                                currentPositionMs = c.currentPosition.coerceAtLeast(0L),
                                durationMs = c.duration.coerceAtLeast(0L)
                            )
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun playSong(song: Song, newPlaylist: List<Song>? = null) {
        Log.d(TAG, "playSong called for videoId=${song.videoId} ('${song.title}')")

        if (newPlaylist != null) {
            val idx = newPlaylist.indexOfFirst { it.videoId == song.videoId }
            _uiState.update {
                it.copy(
                    playlist = newPlaylist,
                    currentIndex = if (idx >= 0) idx else 0
                )
            }
        } else if (_uiState.value.playlist.isEmpty() || _uiState.value.playlist.size == 1) {
            // Auto-fetch Up Next for single song playback to create an endless radio
            scope.launch {
                val upNextResult = searchClient.getUpNext(song.videoId)
                if (upNextResult.isSuccess) {
                    val upNextSongs = upNextResult.getOrNull()
                    if (!upNextSongs.isNullOrEmpty()) {
                        val fullPlaylist = mutableListOf(song)
                        val filteredNext = upNextSongs.filter { it.videoId != song.videoId }
                        fullPlaylist.addAll(filteredNext)
                        _uiState.update {
                            it.copy(
                                playlist = fullPlaylist,
                                currentIndex = 0
                            )
                        }
                    }
                }
            }
        }

        _uiState.update {
            it.copy(
                currentSong = song,
                isResolvingStream = true,
                errorMessage = null,
                isLoginRequired = false
            )
        }

        scope.launch {
            val resolveResult = streamResolver.resolveStreamUrl(song.videoId)
            if (resolveResult.isSuccess) {
                val streamUrl = resolveResult.getOrThrow()
                playStreamUrl(song, streamUrl)
            } else {
                val exception = resolveResult.exceptionOrNull()
                val isLogin = exception is YouTubeLoginRequiredException
                val err = exception?.message ?: "Unknown resolution error"
                Log.e(TAG, "Stream resolution failed (isLogin=$isLogin): $err")
                _uiState.update {
                    it.copy(
                        isResolvingStream = false,
                        isLoginRequired = isLogin,
                        errorMessage = if (isLogin) "YouTube requires account sign-in for this track." else "Could not resolve stream: $err"
                    )
                }
            }
        }
    }

    fun retryCurrentTrack() {
        val current = _uiState.value.currentSong ?: return
        playSong(current)
    }

    fun dismissLoginPrompt() {
        _uiState.update {
            it.copy(isLoginRequired = false)
        }
    }

    private fun playStreamUrl(song: Song, streamUrl: String) {
        val ctrl = controller
        if (ctrl == null) {
            _uiState.update {
                it.copy(
                    isResolvingStream = false,
                    errorMessage = "Player service not ready. Retrying..."
                )
            }
            return
        }

        try {
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setArtworkUri(Uri.parse(song.thumbnailUrl))
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaId(song.videoId)
                .setUri(streamUrl)
                .setMediaMetadata(metadata)
                .build()

            ctrl.setMediaItem(mediaItem)
            ctrl.prepare()
            ctrl.play()

            _uiState.update {
                it.copy(
                    currentSong = song,
                    isResolvingStream = false,
                    errorMessage = null
                )
            }
            Log.d(TAG, "Playback started for: ${song.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed starting playback", e)
            _uiState.update {
                it.copy(
                    isResolvingStream = false,
                    errorMessage = "Failed starting playback: ${e.message}"
                )
            }
        }
    }

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) {
            ctrl.pause()
        } else {
            ctrl.play()
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _uiState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun playNext() {
        val state = _uiState.value
        val list = state.playlist
        if (list.isNotEmpty()) {
            val nextIndex = (state.currentIndex + 1) % list.size
            _uiState.update { it.copy(currentIndex = nextIndex) }
            playSong(list[nextIndex])
        }
    }

    fun playPrevious() {
        val state = _uiState.value
        val list = state.playlist
        if (list.isNotEmpty()) {
            val prevIndex = if (state.currentIndex - 1 < 0) list.size - 1 else state.currentIndex - 1
            _uiState.update { it.copy(currentIndex = prevIndex) }
            playSong(list[prevIndex])
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun release() {
        stopProgressTracker()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
    }
}
