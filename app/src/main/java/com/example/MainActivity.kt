package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.network.YouTubeAuthManager
import com.example.player.PlayerController
import com.example.ui.GoogleLoginScreen
import com.example.ui.MainLayout
import com.example.ui.SearchViewModel
import com.example.ui.theme.DiMusicTheme

class MainActivity : ComponentActivity() {

    private lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        playerController = PlayerController.getInstance(applicationContext)

        setContent {
            DiMusicTheme {
                RequestNotificationPermissionIfNeeded()

                MainContent(
                    playerController = playerController,
                    onMinimizeApp = {
                        moveTaskToBack(true)
                    }
                )
            }
        }
    }


    @Composable
    private fun RequestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val context = this
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* granted state acknowledged */ }

            LaunchedEffect(Unit) {
                if (!permissionGranted) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

@Composable
fun MainContent(
    playerController: PlayerController,
    onMinimizeApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authManager = remember { YouTubeAuthManager.getInstance(context) }
    val isLoggedIn by authManager.isLoggedIn.collectAsState()
    val searchViewModel: SearchViewModel = viewModel()
    val playerState by playerController.uiState.collectAsState()
    
    var quickPicks by remember { mutableStateOf<List<com.example.model.Song>>(com.example.ui.SampleData.quickPicks) }

    if (!isLoggedIn) {
        GoogleLoginScreen(
            authManager = authManager,
            onLoginSuccess = {
                // Unlocked upon Google authentication
            },
            modifier = modifier
        )
        return
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val searchClient = com.example.network.YouTubeSearchClient()
            val historyResult = searchClient.getHistory(authManager)
            if (historyResult.isSuccess) {
                val history = historyResult.getOrNull()
                if (!history.isNullOrEmpty()) {
                    quickPicks = history
                }
            }
        }
    }

    MainLayout(
        modifier = modifier,
        searchViewModel = searchViewModel,
        playerState = playerState,
        songs = quickPicks,
        onSongClick = { song, playlist ->
            playerController.playSong(song, playlist)
        },
        onTogglePlayPause = {
            playerController.togglePlayPause()
        },
        onPlayNext = {
            playerController.playNext()
        },
        onPlayPrevious = {
            playerController.playPrevious()
        },
        onSeekTo = { pos ->
            playerController.seekTo(pos)
        }
    )
}

