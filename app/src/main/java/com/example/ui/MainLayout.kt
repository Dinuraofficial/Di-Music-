package com.example.ui
import androidx.compose.material.icons.filled.Tune
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Song
import com.example.player.PlayerUiState
import com.example.ui.components.AlbumGridItem
import com.example.ui.components.RotatedNavItem
import com.example.ui.components.SongListItem
import com.example.ui.components.SquircleFab
import com.example.ui.theme.SlateAccent
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateDivider
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary

enum class NavTab(val title: String) {
    QUICK_PICKS("Quick picks"),
    SONGS("Songs"),
    LIBRARY("Your Library"),
    ARTISTS("Artists"),
    ALBUMS("Albums")
}

data class Playlist(
    val id: String,
    val title: String,
    val songCount: Int,
    val coverUrl: String
)

val samplePlaylists = listOf(
    Playlist("pl_1", "Favorites", 28, "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300"),
    Playlist("pl_2", "Late Night Drive", 19, "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300"),
    Playlist("pl_3", "Deep Focus & Study", 42, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300"),
    Playlist("pl_4", "Electronic Classics", 35, "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=300")
)

/**
 * ViMusic Main Screen Layout
 *
 * Implements the signature Left Side Navigation Pattern:
 * - Left Column (Navigation Rail):
 *     - At top: Equalizer icon
 *     - Vertical text tabs rotated -90 degrees ("Quick picks", "Songs", "Playlists", "Artists", "Albums")
 *     - Active tab has SlateAccent; inactive tabs are gray
 * - Right Column (Main Content): Takes up weight(1f)
 * - Bottom Right: Squircle FAB (search)
 * - Persistent Bottom Bar: Floating Mini-Player
 */
@Composable
fun MainLayout(
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel = viewModel(),
    playerState: PlayerUiState = PlayerUiState(
        currentSong = SampleData.quickPicks.first(),
        isPlaying = false,
        currentPositionMs = 159000L,
        durationMs = 433000L
    ),
    songs: List<Song> = SampleData.quickPicks,
    albums: List<Album> = SampleData.albums,
    artists: List<Artist> = SampleData.artists,
    playlists: List<Playlist> = samplePlaylists,
    onSongClick: (Song, List<Song>?) -> Unit = { _, _ -> },
    onTogglePlayPause: () -> Unit = {},
    onPlayNext: () -> Unit = {},
    onPlayPrevious: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(NavTab.QUICK_PICKS) }
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }
    var isPlayerFullscreen by remember { mutableStateOf(false) }
    var isSearchOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    // Intercept back button for nested screens
    BackHandler(enabled = isPlayerFullscreen || selectedArtist != null || isSearchOpen || isSettingsOpen) {
        when {
            isSettingsOpen -> isSettingsOpen = false
            isPlayerFullscreen -> isPlayerFullscreen = false
            selectedArtist != null -> selectedArtist = null
            isSearchOpen -> isSearchOpen = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SlateBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main App Frame: Row with Left Navigation Rail + Right Main Content
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // ==========================================
                // 1. LEFT COLUMN: NAVIGATION RAIL
                // ==========================================
                Column(
                    modifier = Modifier
                        .width(52.dp)
                        .fillMaxHeight()
                        .background(SlateBackground)
                        .padding(top = 32.dp, bottom = if (playerState.currentSong != null) 80.dp else 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    // Menu icon at the very top of rail for Settings
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clickable { isSettingsOpen = true }
                            .testTag("nav_menu_icon"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = SlateTextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Text tabs rotated -90 degrees
                    NavTab.entries.forEach { tab ->
                        RotatedNavItem(
                            title = tab.title,
                            selected = selectedTab == tab && selectedArtist == null,
                            onClick = {
                                selectedArtist = null
                                selectedTab = tab
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Subtle vertical divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(SlateDivider.copy(alpha = 0.5f))
                )

                // ==========================================
                // 2. RIGHT COLUMN: MAIN CONTENT (weight 1f)
                // ==========================================
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (selectedTab) {
                        NavTab.QUICK_PICKS -> {
                            QuickPicksScreen(
                                songs = songs,
                                albums = albums,
                                artists = artists,
                                currentSong = playerState.currentSong,
                                onSongClick = { song -> onSongClick(song, null) },
                                onAlbumClick = { /* album click */ },
                                onArtistClick = { artist -> selectedArtist = artist }
                            )
                        }

                        NavTab.SONGS -> {
                            SongsTabContent(
                                songs = songs,
                                currentSong = playerState.currentSong,
                                onSongClick = { song -> onSongClick(song, null) }
                            )
                        }

                        NavTab.LIBRARY -> {
                            PlaylistsTabContent(
                                playlists = playlists,
                                onPlaylistClick = { pl ->
                                    if (songs.isNotEmpty()) onSongClick(songs.first(), null)
                                }
                            )
                        }

                        NavTab.ARTISTS -> {
                            ArtistsTabContent(
                                artists = artists,
                                onArtistClick = { artist -> selectedArtist = artist }
                            )
                        }

                        NavTab.ALBUMS -> {
                            AlbumsTabContent(
                                albums = albums,
                                onAlbumClick = { album ->
                                    if (songs.isNotEmpty()) onSongClick(songs.first(), null)
                                }
                            )
                        }
                    }

                    // Floating Action Button (FAB) in bottom right of main content (above mini-player)
                    SquircleFab(
                        onClick = {
                            isSearchOpen = true
                            onSearchClick()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = if (playerState.currentSong != null) 72.dp else 24.dp)
                    )
                }
            }

            // ==========================================
            // 3. PERSISTENT FLOATING MINI-PLAYER
            // ==========================================
            if (playerState.currentSong != null && !isPlayerFullscreen) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    MiniPlayerBar(
                        playerState = playerState,
                        onTogglePlayPause = onTogglePlayPause,
                        onPlayNext = onPlayNext,
                        onOpenFullPlayer = { isPlayerFullscreen = true }
                    )
                }
            }

            // ==========================================
            // 4. SEARCH MODAL OVERLAY (Actively searches YouTube)
            // ==========================================
            AnimatedVisibility(
                visible = isSearchOpen,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                SearchOverlay(
                    searchViewModel = searchViewModel,
                    onClose = { isSearchOpen = false },
                    onSongClick = { song, resultList ->
                        onSongClick(song, resultList)
                        isSearchOpen = false
                    }
                )
            }

            // ==========================================
            // 5. ARTIST SCREEN OVERLAY
            // ==========================================
            AnimatedVisibility(
                visible = selectedArtist != null && !isPlayerFullscreen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                selectedArtist?.let { artist ->
                    ArtistScreen(
                        artist = artist,
                        songs = songs.filter { it.artist.contains(artist.name, ignoreCase = true) }
                            .ifEmpty { songs.take(5) },
                        onBack = { selectedArtist = null },
                        onSongClick = { song -> onSongClick(song, null) }
                    )
                }
            }

            // ==========================================
            // 6. FULLSCREEN PLAYER SCREEN OVERLAY
            // ==========================================
            AnimatedVisibility(
                visible = isPlayerFullscreen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                PlayerScreen(
                    song = playerState.currentSong,
                    isPlaying = playerState.isPlaying,
                    isResolving = playerState.isResolvingStream,
                    currentPositionMs = playerState.currentPositionMs,
                    durationMs = playerState.durationMs,
                    onTogglePlayPause = onTogglePlayPause,
                    onSeekTo = onSeekTo,
                    onNext = onPlayNext,
                    onPrevious = onPlayPrevious,
                    onCollapse = { isPlayerFullscreen = false }
                )
            }

            // ==========================================
            // 7. SETTINGS SCREEN OVERLAY
            // ==========================================
            AnimatedVisibility(
                visible = isSettingsOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                SettingsScreen(
                    onClose = { isSettingsOpen = false }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Secondary Tab Contents for ViMusic Minimal Aesthetic
// -------------------------------------------------------------

@Composable
private fun SongsTabContent(
    songs: List<Song>,
    currentSong: Song?,
    onSongClick: (Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp, start = 8.dp, end = 8.dp)
    ) {
        item {
            Text(
                text = "Songs",
                color = SlateTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
        }
        items(songs) { song ->
            SongListItem(
                song = song,
                isPlaying = currentSong?.title == song.title,
                onClick = { onSongClick(song) }
            )
        }
    }
}

@Composable
private fun PlaylistsTabContent(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp, start = 12.dp, end = 12.dp)
    ) {
        item {
            Text(
                text = "Playlists",
                color = SlateTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        items(playlists) { pl ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onPlaylistClick(pl) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SlateSurface),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = pl.coverUrl,
                        contentDescription = pl.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pl.title,
                        color = SlateTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${pl.songCount} songs",
                        color = SlateTextSecondary,
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = null,
                    tint = SlateTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ArtistsTabContent(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(artists) { artist ->
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onArtistClick(artist) }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = artist.imageUrl,
                    contentDescription = artist.name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SlateSurface),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = artist.name,
                    color = SlateTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!artist.subscribers.isNullOrEmpty()) {
                    Text(
                        text = "${artist.subscribers} fans",
                        color = SlateTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumsTabContent(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(albums) { album ->
            AlbumGridItem(
                album = album,
                onClick = { onAlbumClick(album) }
            )
        }
    }
}

@Composable
private fun SearchOverlay(
    searchViewModel: SearchViewModel,
    onClose: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit
) {
    val query by searchViewModel.query.collectAsState()
    val searchUiState by searchViewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_overlay"),
        color = SlateBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Top search bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("close_search_overlay")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close Search",
                        tint = SlateTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { searchViewModel.onQueryChange(it) },
                    placeholder = {
                        Text(
                            "Search songs, artists on YouTube...",
                            color = SlateTextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_overlay_input"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SlateAccent
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { searchViewModel.clearSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = SlateTextSecondary
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SlateSurface,
                        unfocusedContainerColor = SlateSurface,
                        focusedBorderColor = SlateAccent,
                        unfocusedBorderColor = SlateDivider,
                        focusedTextColor = SlateTextPrimary,
                        unfocusedTextColor = SlateTextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboardController?.hide()
                        searchViewModel.search()
                    })
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick search chips (Trending, Top Hits, Lo-Fi, Synthwave, etc.)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(searchViewModel.quickChips) { chip ->
                    val isSelected = query.equals(chip, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SlateAccent else SlateSurface)
                            .clickable {
                                keyboardController?.hide()
                                searchViewModel.search(chip)
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = chip,
                            color = if (isSelected) SlateBackground else SlateTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on search state (actively querying YouTube)
            when (val state = searchUiState) {
                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = SlateAccent,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Searching YouTube...",
                                color = SlateTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                is SearchUiState.Success -> {
                    Text(
                        text = "YouTube Results (${state.songs.size})",
                        color = SlateTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(state.songs) { song ->
                            SongListItem(
                                song = song,
                                onClick = { onSongClick(song, state.songs) }
                            )
                        }
                    }
                }

                is SearchUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = SlateTextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { searchViewModel.search() },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateSurface)
                            ) {
                                Text("Retry Search", color = SlateAccent)
                            }
                        }
                    }
                }

                is SearchUiState.Idle -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = SlateTextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Search any song, artist, or album on YouTube",
                                color = SlateTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

