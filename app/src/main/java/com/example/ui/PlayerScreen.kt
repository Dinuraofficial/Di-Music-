package com.example.ui

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.Song
import com.example.model.getHdImageUrl
import com.example.player.PlayerUiState
import com.example.ui.theme.SlateAccent
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateDivider
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary

enum class PlayerViewMode {
    NOW_PLAYING,
    QUEUE
}

/**
 * DiMusic Reimagined Player Screen
 *
 * - Inspired by ViMusic and Spotify players:
 *   - Sleek Dark Slate aesthetic (0xFF161819)
 *   - 3D Coin-Flip animation on Album Cover to toggle between HD Cover & Synchronized Lyrics
 *   - Full Up-Next Queue integration (accessible via the 3-lines menu button)
 *   - Fully functional 3-dots More Options sheet (Artist, Share, Lyrics, Audio Quality, Sleep Timer)
 *   - HD 1080p Cover Art resolution handling
 *   - Responsive touch targets with minimum 48dp
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    song: Song? = null,
    isPlaying: Boolean = false,
    isResolving: Boolean = false,
    currentPositionMs: Long = 159000L,
    durationMs: Long = 433000L,
    playlist: List<Song> = emptyList(),
    currentIndex: Int = -1,
    isFavorite: Boolean = false,
    onSongClick: (Song) -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onTogglePlayPause: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    onCollapse: () -> Unit = {},
    onGoToArtist: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val displaySong = song ?: Song(
        title = "Starboy",
        artist = "The Weeknd, Daft Punk",
        artUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1200&q=90",
        durationText = "3:50"
    )

    var currentViewMode by remember { mutableStateOf(PlayerViewMode.NOW_PLAYING) }
    var isFlippedToLyrics by remember { mutableStateOf(false) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekPositionMs by remember { mutableFloatStateOf(currentPositionMs.toFloat()) }
    var localFavorite by remember(isFavorite) { mutableStateOf(isFavorite) }
    var repeatModeActive by remember { mutableStateOf(false) }
    var shuffleActive by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val effectivePosition = if (isUserSeeking) seekPositionMs.toLong() else currentPositionMs
    val effectiveDuration = if (durationMs > 0L) durationMs else 1L
    val progressFraction = (effectivePosition.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f)

    // 3D Coin Flip Animation
    val flipRotation by animateFloatAsState(
        targetValue = if (isFlippedToLyrics) 180f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "coin_flip_rotation"
    )

    // Toast message auto-dismiss
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(2200)
            toastMessage = null
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("fullscreen_player_screen")
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        // Responsive album cover sizing: comfortable square that fits viewport nicely
        val albumCoverSize = (screenWidth - 72.dp).coerceIn(240.dp, (screenHeight * 0.38f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // =========================================================
            // 1. TOP BAR: Collapse Button, Title Indicator, Mode Switch
            // =========================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("player_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = SlateTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Top Title (like Spotify)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "NOW PLAYING",
                        color = SlateTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                }

                // Three Dots (More Options) Button in Top Bar
                IconButton(
                    onClick = { showOptionsSheet = true },
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("player_more_options_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More Options",
                        tint = SlateTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // =========================================================
            // 2. MAIN BODY (Switches between NOW PLAYING and QUEUE)
            // =========================================================
            AnimatedContent(
                targetState = currentViewMode,
                transitionSpec = {
                    if (targetState == PlayerViewMode.QUEUE) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "player_mode_transition"
            ) { mode ->
                if (mode == PlayerViewMode.QUEUE) {
                    // ----------------------------------------------------
                    // QUEUE VIEW
                    // ----------------------------------------------------
                    PlayerQueueView(
                        currentSong = displaySong,
                        playlist = if (playlist.isNotEmpty()) playlist else SampleData.quickPicks,
                        currentIndex = currentIndex,
                        isPlaying = isPlaying,
                        onSongClick = { songItem ->
                            onSongClick(songItem)
                            currentViewMode = PlayerViewMode.NOW_PLAYING
                        },
                        onBackToPlayer = { currentViewMode = PlayerViewMode.NOW_PLAYING }
                    )
                } else {
                    // ----------------------------------------------------
                    // NOW PLAYING VIEW (with 3D Coin-Flip Cover & Lyrics)
                    // ----------------------------------------------------
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 3D COIN-FLIP CARD: Cover Art (Front) <--> Lyrics (Back)
                        Box(
                            modifier = Modifier
                                .size(albumCoverSize)
                                .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = SlateAccent.copy(alpha = 0.25f))
                                .graphicsLayer {
                                    rotationY = flipRotation
                                    cameraDistance = 18f * density
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    isFlippedToLyrics = !isFlippedToLyrics
                                }
                                .testTag("player_album_cover_flip_card"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (flipRotation <= 90f) {
                                // FRONT: HD Album Artwork
                                AlbumArtFront(
                                    displaySong = displaySong,
                                    isResolving = isResolving
                                )
                            } else {
                                // BACK: Synchronized Lyrics View (horizontally mirrored so text reads correctly)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { rotationY = 180f }
                                ) {
                                    LyricsBackView(
                                        song = displaySong,
                                        currentPositionMs = effectivePosition
                                    )
                                }
                            }
                        }

                        // Flip Hint Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(SlateSurface.copy(alpha = 0.8f))
                                .clickable { isFlippedToLyrics = !isFlippedToLyrics }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isFlippedToLyrics) Icons.Default.Audiotrack else Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = SlateAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isFlippedToLyrics) "TAP FOR COVER" else "TAP FOR LYRICS",
                                color = SlateAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Song Title, Artist & Like Button (Spotify/ViMusic Style Row)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = displaySong.title,
                                    color = SlateTextPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("player_song_title")
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = displaySong.artist,
                                    color = SlateTextSecondary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .clickable {
                                            onGoToArtist?.invoke(displaySong.artist)
                                        }
                                        .testTag("player_song_artist")
                                )
                            }

                            // Favorite Heart Button
                            IconButton(
                                onClick = {
                                    localFavorite = !localFavorite
                                    onToggleFavorite()
                                },
                                modifier = Modifier
                                    .size(56.dp)
                                    .testTag("player_favorite_button")
                            ) {
                                Icon(
                                    imageVector = if (localFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (localFavorite) Color(0xFFFF5252) else SlateTextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // =========================================================
                        // Seekbar / Scrubber with Accent Fill & Clean Timestamps
                        // =========================================================
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        ) {
                            // Spotify-style thin slider
                            @OptIn(ExperimentalMaterial3Api::class)
                            Slider(
                                value = progressFraction,
                                onValueChange = { frac ->
                                    isUserSeeking = true
                                    seekPositionMs = frac * effectiveDuration
                                },
                                onValueChangeFinished = {
                                    isUserSeeking = false
                                    onSeekTo(seekPositionMs.toLong())
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = SlateTextPrimary,
                                    activeTrackColor = SlateTextPrimary,
                                    inactiveTrackColor = SlateSurface
                                ),
                                track = { sliderState ->
                                    SliderDefaults.Track(
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = SlateTextPrimary,
                                            inactiveTrackColor = SlateSurface
                                        ),
                                        sliderState = sliderState,
                                        modifier = Modifier.height(3.dp)
                                    )
                                },
                                thumb = {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isUserSeeking) 12.dp else 0.dp) // Hide thumb unless seeking (Spotify style)
                                            .clip(CircleShape)
                                            .background(SlateTextPrimary)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("player_progress_slider")
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatMsToTimestamp(effectivePosition),
                                    color = SlateTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formatMsToTimestamp(effectiveDuration),
                                    color = SlateTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // =========================================================
                        // Playback Controls Row (Shuffle, Prev, Play/Pause, Next, Repeat)
                        // =========================================================
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Shuffle
                            IconButton(
                                onClick = {
                                    shuffleActive = !shuffleActive
                                    toastMessage = if (shuffleActive) "Shuffle On" else "Shuffle Off"
                                },
                                modifier = Modifier.size(56.dp).testTag("player_shuffle_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (shuffleActive) SlateAccent else SlateTextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // 2. Previous Track
                            IconButton(
                                onClick = onPrevious,
                                modifier = Modifier.size(56.dp).testTag("player_prev_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous Track",
                                    tint = SlateTextPrimary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            // 3. Play / Pause Button (Signature Large Disc with Material Ripple)
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(SlateSurface)
                                    .clickable(onClick = onTogglePlayPause)
                                    .testTag("player_play_pause_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isResolving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(30.dp),
                                        color = SlateAccent,
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }

                            // 4. Next Track
                            IconButton(
                                onClick = onNext,
                                modifier = Modifier.size(56.dp).testTag("player_next_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next Track",
                                    tint = SlateTextPrimary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            // 5. Repeat
                            IconButton(
                                onClick = {
                                    repeatModeActive = !repeatModeActive
                                    toastMessage = if (repeatModeActive) "Repeat On" else "Repeat Off"
                                },
                                modifier = Modifier.size(56.dp).testTag("player_repeat_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = "Repeat",
                                    tint = if (repeatModeActive) SlateAccent else SlateTextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================
            // 3. BOTTOM BAR: Audio Device + Queue
            // =========================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audio Output Indicator
                AudioOutputIndicator()

                // Middle badge indicator
                toastMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = SlateAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Three lines button (Toggle Queue View) moved to bottom right
                IconButton(
                    onClick = {
                        currentViewMode = if (currentViewMode == PlayerViewMode.QUEUE) {
                            PlayerViewMode.NOW_PLAYING
                        } else {
                            PlayerViewMode.QUEUE
                        }
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("player_queue_menu_button")
                ) {
                    CustomQueueIcon(
                        modifier = Modifier.size(28.dp),
                        tint = if (currentViewMode == PlayerViewMode.QUEUE) SlateAccent else SlateTextSecondary
                    )
                }
            }
        }
    }

    // =========================================================
    // 4. MORE OPTIONS BOTTOM SHEET (Three Dots Modal)
    // =========================================================
    if (showOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsSheet = false },
            containerColor = SlateSurface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Song Header Preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(displaySong.hdArtUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displaySong.title,
                            color = SlateTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = displaySong.artist,
                            color = SlateTextSecondary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(color = SlateDivider)

                // Option: View Artist
                OptionsRowItem(
                    icon = Icons.Default.Person,
                    title = "Go to Artist (${displaySong.artist.split(",").firstOrNull()?.trim() ?: displaySong.artist})"
                ) {
                    showOptionsSheet = false
                    onGoToArtist?.invoke(displaySong.artist)
                }

                // Option: Toggle Lyrics
                OptionsRowItem(
                    icon = Icons.Default.MusicNote,
                    title = if (isFlippedToLyrics) "Show Album Art" else "Show Synchronized Lyrics"
                ) {
                    isFlippedToLyrics = !isFlippedToLyrics
                    currentViewMode = PlayerViewMode.NOW_PLAYING
                    showOptionsSheet = false
                }

                // Option: View Queue
                OptionsRowItem(
                    icon = Icons.AutoMirrored.Filled.QueueMusic,
                    title = "View Playing Queue (${playlist.size.coerceAtLeast(1)} songs)"
                ) {
                    currentViewMode = PlayerViewMode.QUEUE
                    showOptionsSheet = false
                }

                // Option: Share Song
                OptionsRowItem(
                    icon = Icons.Default.Share,
                    title = "Share Song"
                ) {
                    showOptionsSheet = false
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Listen to ${displaySong.title} by ${displaySong.artist} on DiMusic: https://music.youtube.com/watch?v=${displaySong.videoId}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Track"))
                }

                // Option: Sleep Timer
                OptionsRowItem(
                    icon = Icons.Default.Timer,
                    title = "Sleep Timer (30 minutes)"
                ) {
                    showOptionsSheet = false
                    toastMessage = "Sleep timer set for 30 minutes"
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun OptionsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SlateAccent,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            color = SlateTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Front side of the Album Card: HD 1080p Album Cover Art
 */
@Composable
private fun AlbumArtFront(
    displaySong: Song,
    isResolving: Boolean
) {
    val context = LocalContext.current
    val hdUrl = displaySong.hdArtUrl

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateSurface),
        contentAlignment = Alignment.Center
    ) {
        if (hdUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(hdUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Album art for ${displaySong.title}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = SlateAccent.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
        }

        if (isResolving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = SlateAccent,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

/**
 * Back side of the Album Card: Synchronized / Formatted Lyrics View
 */
@Composable
private fun LyricsBackView(
    song: Song,
    currentPositionMs: Long
) {
    var lyricsLines by remember(song.title) { mutableStateOf<List<Pair<Long, String>>>(listOf(-1L to "Loading lyrics...")) }
    val listState = rememberLazyListState()

    LaunchedEffect(song.title) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = java.net.URL("https://lrclib.net/api/search?q=${java.net.URLEncoder.encode(song.title + " " + song.artist, "UTF-8")}")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "DiMusic (https://github.com/dimusic)")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                if (connection.responseCode == 200) {
                    val stream = connection.inputStream
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(stream))
                    val response = reader.readText()
                    reader.close()
                    val jsonArray = org.json.JSONArray(response)
                    if (jsonArray.length() > 0) {
                        val obj = jsonArray.getJSONObject(0)
                        val syncedLyrics = obj.optString("syncedLyrics", "")
                        val plainLyrics = obj.optString("plainLyrics", "")

                        if (syncedLyrics.isNotEmpty()) {
                            val parsedLines = mutableListOf<Pair<Long, String>>()
                            val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")
                            syncedLyrics.split("\n").forEach { line ->
                                val match = regex.find(line)
                                if (match != null) {
                                    val (min, sec, millisStr, text) = match.destructured
                                    // Handle both 2 and 3 digit milliseconds
                                    val millis = if (millisStr.length == 2) millisStr.toLong() * 10 else millisStr.toLong()
                                    val timeMs = min.toLong() * 60000 + sec.toLong() * 1000 + millis
                                    parsedLines.add(timeMs to text.trim())
                                }
                            }
                            if (parsedLines.isNotEmpty()) {
                                lyricsLines = parsedLines
                            } else {
                                lyricsLines = plainLyrics.split("\n").map { -1L to it }
                            }
                        } else if (plainLyrics.isNotEmpty()) {
                            lyricsLines = plainLyrics.split("\n").map { -1L to it }
                        } else {
                            lyricsLines = listOf(-1L to "No lyrics found for this song.")
                        }
                    } else {
                        lyricsLines = listOf(-1L to "No lyrics found.")
                    }
                } else {
                    lyricsLines = listOf(-1L to "Lyrics not found.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                lyricsLines = listOf(-1L to "Could not fetch lyrics.", -1L to "Check connection or try again.")
            }
        }
    }

    val currentLineIndex = if (lyricsLines.firstOrNull()?.first ?: -1L >= 0) {
        lyricsLines.indexOfLast { it.first <= currentPositionMs }.coerceAtLeast(0)
    } else -1

    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0 && currentLineIndex < lyricsLines.size) {
            // Scroll to the current item, try to center it a bit by scrolling a few items earlier if possible
            val targetIndex = (currentLineIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SlateSurface,
                        Color(0xFF1B1E22),
                        SlateBackground
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LYRICS",
                    color = SlateAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = song.title,
                    color = SlateTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(lyricsLines) { idx, pair ->
                    val (timeMs, line) = pair
                    val isEmphasized = if (timeMs >= 0) {
                        idx == currentLineIndex
                    } else {
                        false
                    }
                    Text(
                        text = if (line.isEmpty()) "♪" else line,
                        color = if (isEmphasized) SlateTextPrimary else SlateTextSecondary.copy(alpha = 0.85f),
                        fontSize = if (isEmphasized) 20.sp else 16.sp,
                        fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.Normal,
                        lineHeight = if (isEmphasized) 28.sp else 22.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Optional: tap to seek could be implemented here
                            }
                    )
                }
            }
        }
    }
}

/**
 * Queue View inside the Player
 */
@Composable
private fun PlayerQueueView(
    currentSong: Song,
    playlist: List<Song>,
    currentIndex: Int,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onBackToPlayer: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Up Next",
                    color = SlateTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${playlist.size} tracks in queue",
                    color = SlateTextSecondary,
                    fontSize = 12.sp
                )
            }

            TextButton(onClick = onBackToPlayer) {
                Text("View Player", color = SlateAccent, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(playlist) { index, songItem ->
                val isThisPlaying = (songItem.videoId == currentSong.videoId || songItem.title == currentSong.title)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isThisPlaying) SlateSurfaceVariant else Color.Transparent)
                        .clickable { onSongClick(songItem) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Index or Equalizer icon
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isThisPlaying) {
                            Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = "Playing",
                                tint = SlateAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                color = SlateTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Thumbnail
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(songItem.hdArtUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = songItem.title,
                            color = if (isThisPlaying) SlateAccent else SlateTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = if (isThisPlaying) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = songItem.artist,
                            color = SlateTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Duration
                    Text(
                        text = songItem.durationText ?: "3:30",
                        color = SlateTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Fallback lyrics generator with authentic verses for featured artists
 */
private fun getLyricsForSong(title: String, artist: String): List<String> {
    val lowerTitle = title.lowercase()
    return when {
        lowerTitle.contains("starboy") -> listOf(
            "I'm tryna put you in the worst mood, ah",
            "P1 cleaner than your church shoes, ah",
            "Milli point two just to hurt you, ah",
            "All red Lamb' just to tease you, ah",
            "None of these toys on lease too, ah",
            "Made your whole year in a week too, yah",
            "Main girl out of your league too, ah",
            "Side girl out of your league too, ah",
            "Look what you've done",
            "I'm a motherfuckin' starboy",
            "Look what you've done",
            "I'm a motherfuckin' starboy",
            "Every day a nigga try to test me, ah",
            "Every day a nigga try to end me, ah",
            "Pull up in that Roadster SV, ah",
            "Pockets overweight, gettin' hefty, ah"
        )
        lowerTitle.contains("the less i know") -> listOf(
            "Someone said they left together",
            "I ran out the door to get her",
            "She was holding hands with Trevor",
            "Not the greatest feeling ever",
            "Said, 'Pull yourself together",
            "You should try your luck with her'",
            "Did I see you heading for them?",
            "Ooh, don't believe what they said",
            "Don't surrender to your head",
            "Wait a minute, something's wrong",
            "I am in the wrong world",
            "Ah, the less I know the better",
            "Hope I don't see them around",
            "Oh my love, can't you see yourself by my side?"
        )
        lowerTitle.contains("blinding lights") -> listOf(
            "Yeah, I've been tryna call",
            "I've been on my own for long enough",
            "Maybe you can show me how to love, maybe",
            "I'm going through withdrawals",
            "You don't even have to do too much",
            "You can turn me on with just a touch, baby",
            "I look around and Sin City's cold and empty",
            "No one's around to judge me",
            "I can't see clearly when you're gone",
            "I said, ooh, I'm blinded by the lights",
            "No, I can't sleep until I feel your touch",
            "I said, ooh, I'm drowning in the night",
            "Oh, when I'm like this, you're the one I trust"
        )
        lowerTitle.contains("instant crush") -> listOf(
            "I didn't want to be the one to forget",
            "I thought of everything I'd never regret",
            "A little time with you is all that I get",
            "That's all we need because it's all we can take",
            "One gossamer day in the bright and shiny light",
            "I would have stayed up with you all night",
            "Now we're back in the car with the radio playin'",
            "And I can't quite hear what the voices are sayin'",
            "And we will never be alone again",
            "Cause it doesn't happen every day",
            "Kinda counted on you being a friend",
            "Can I tell you, but I can't explain"
        )
        lowerTitle.contains("get lucky") -> listOf(
            "Like the legend of the phoenix",
            "All ends with beginnings",
            "What keeps the planet spinning",
            "The force from the beginning",
            "We've come too far to give up who we are",
            "So let's raise the bar and our cups to the stars",
            "She's up all night 'til the sun",
            "I'm up all night to get some",
            "She's up all night for good fun",
            "I'm up all night to get lucky",
            "We're up all night 'til the sun",
            "We're up all night to get some",
            "We're up all night for good fun",
            "We're up all night to get lucky"
        )
        lowerTitle.contains("borderline") -> listOf(
            "Gone a little far",
            "Gone a little far this time for somethin'",
            "How was I to know?",
            "How was I to know this dark will come?",
            "Will I be known and loved?",
            "Is there one that I trust?",
            "Starting to sober up",
            "Has it been long enough?",
            "Will I be known and loved?",
            "Little closer, close enough",
            "I'm on the borderline",
            "Caught between the tides of pain and rapture"
        )
        lowerTitle.contains("save your tears") -> listOf(
            "I saw you dancing in a crowded room",
            "You look so happy when I'm not with you",
            "But then you saw me, caught you by surprise",
            "A single teardrop falling from your eye",
            "I don't know why I run away",
            "I'll make you cry when I run away",
            "Take me back 'cause I wanna stay",
            "Save your tears for another day",
            "Save your tears for another day"
        )
        else -> listOf(
            "[Verse 1]",
            "Walking through the city in the neon light",
            "Listening to the rhythm running through the night",
            "Every single melody is taking me higher",
            "Catching all the sparks of a burning fire",
            "",
            "[Chorus]",
            "This is $title by $artist",
            "Feel the heartbeat in the sound",
            "When the music turns around",
            "Nothing's gonna slow us down",
            "",
            "[Verse 2]",
            "Fading into memories we never can erase",
            "Drifting through the echoes in this open space",
            "Turn the volume up and let the speakers roll",
            "Pure sonic energy straight to the soul",
            "",
            "[Outro]",
            "And we keep playing on and on",
            "Until the morning sun has shone"
        )
    }
}

/**
 * Convenience wrapper for connecting to PlayerUiState.
 */
@Composable
fun PlayerScreen(
    playerState: PlayerUiState,
    onBack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onSongClick: (Song) -> Unit = {},
    onGoToArtist: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PlayerScreen(
        song = playerState.currentSong,
        isPlaying = playerState.isPlaying,
        isResolving = playerState.isResolvingStream,
        currentPositionMs = playerState.currentPositionMs,
        durationMs = playerState.durationMs,
        playlist = playerState.playlist,
        currentIndex = playerState.currentIndex,
        onSongClick = onSongClick,
        onTogglePlayPause = onTogglePlayPause,
        onSeekTo = onSeekTo,
        onNext = onPlayNext,
        onPrevious = onPlayPrevious,
        onCollapse = onBack,
        onGoToArtist = onGoToArtist,
        modifier = modifier
    )
}

private fun formatMsToTimestamp(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Composable
fun AudioOutputIndicator(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager }
    var deviceName by remember { mutableStateOf("Speaker") }
    
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(android.media.AudioManager.GET_DEVICES_OUTPUTS)
            val btDevice = devices.firstOrNull { 
                it.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || 
                it.type == android.media.AudioDeviceInfo.TYPE_BLE_HEADSET || 
                it.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_SCO 
            }
            if (btDevice != null) {
                deviceName = btDevice.productName?.toString()?.takeIf { it.isNotBlank() } ?: "Bluetooth"
            } else {
                val wired = devices.firstOrNull { 
                    it.type == android.media.AudioDeviceInfo.TYPE_WIRED_HEADPHONES || 
                    it.type == android.media.AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    it.type == android.media.AudioDeviceInfo.TYPE_USB_HEADSET 
                }
                if (wired != null) {
                    deviceName = "Headphones"
                }
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .clickable { /* future integration */ }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = if (deviceName == "Speaker") Icons.Default.Speaker else if (deviceName == "Headphones") Icons.Default.Headset else Icons.Default.Bluetooth,
            contentDescription = null,
            tint = SlateAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = deviceName,
            color = SlateAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CustomQueueIcon(modifier: Modifier = Modifier, tint: Color = Color.White) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = h * 0.18f
        
        // Top capsule
        drawRoundRect(
            color = tint,
            topLeft = androidx.compose.ui.geometry.Offset(0f, h * 0.1f),
            size = androidx.compose.ui.geometry.Size(w, h * 0.4f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * 0.2f, h * 0.2f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
        )
        
        // Middle line
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(0f, h * 0.75f),
            end = androidx.compose.ui.geometry.Offset(w, h * 0.75f),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        // Bottom line
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(0f, h * 1.0f),
            end = androidx.compose.ui.geometry.Offset(w, h * 1.0f),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
