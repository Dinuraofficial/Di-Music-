package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Song
import com.example.ui.components.AlbumGridItem
import com.example.ui.components.SongListItem
import com.example.ui.theme.SlateAccent
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary

// ViMusic / DiMusic Default Placeholder Datasets
object SampleData {
    val quickPicks = listOf(
        Song(
            videoId = "34Na4j8HLjc",
            title = "Starboy",
            artist = "The Weeknd, Daft Punk",
            artUrl = "https://upload.wikimedia.org/wikipedia/en/3/39/The_Weeknd_-_Starboy.png",
            durationText = "3:50"
        ),
        Song(
            videoId = "sBzrzS1Ag_g",
            title = "The Less I Know The Better",
            artist = "Tame Impala",
            artUrl = "https://upload.wikimedia.org/wikipedia/en/9/9b/Tame_Impala_-_Currents.png",
            durationText = "3:36"
        ),
        Song(
            videoId = "4NRXx6U8ABQ",
            title = "Blinding Lights",
            artist = "The Weeknd",
            artUrl = "https://upload.wikimedia.org/wikipedia/en/c/c1/The_Weeknd_-_After_Hours.png",
            durationText = "3:20"
        ),
        Song(
            videoId = "a5uQMwRMHcs",
            title = "Instant Crush",
            artist = "Daft Punk ft. Julian Casablancas",
            artUrl = "https://upload.wikimedia.org/wikipedia/en/a/a7/Random_Access_Memories.jpg",
            durationText = "5:37"
        ),
        Song(
            videoId = "vpbblMR_jUo",
            title = "Borderline",
            artist = "Tame Impala",
            artUrl = "https://upload.wikimedia.org/wikipedia/en/b/b7/Tame_Impala_-_The_Slow_Rush.png",
            durationText = "3:57"
        ),
        Song(
            videoId = "5NV6Rdv1a3I",
            title = "Get Lucky",
            artist = "Daft Punk ft. Pharrell Williams",
            artUrl = "https://upload.wikimedia.org/wikipedia/en/a/a7/Random_Access_Memories.jpg",
            durationText = "4:08"
        ),
        Song(
            videoId = "sOS9aOIXPEk",
            title = "Something About Us",
            artist = "Daft Punk",
            artUrl = "https://upload.wikimedia.org/wikipedia/en/a/ae/Daft_Punk_-_Discovery.jpg",
            durationText = "3:51"
        ),
        Song(
            videoId = "XXYlCGOWIEY",
            title = "Save Your Tears",
            artist = "The Weeknd",
            artUrl = "https://upload.wikimedia.org/wikipedia/en/c/c1/The_Weeknd_-_After_Hours.png",
            durationText = "3:35"
        )
    )

    val albums = listOf(
        Album(
            id = "album_1",
            title = "Random Access Memories",
            artist = "Daft Punk",
            artUrl = "https://upload.wikimedia.org/wikipedia/en/a/a7/Random_Access_Memories.jpg",
            year = "2013",
            songCount = 13
        ),
        Album(
            id = "album_2",
            title = "After Hours",
            artist = "The Weeknd",
            artUrl = "https://upload.wikimedia.org/wikipedia/en/c/c1/The_Weeknd_-_After_Hours.png",
            year = "2020",
            songCount = 14
        ),
        Album(
            id = "album_3",
            title = "Currents",
            artist = "Tame Impala",
            artUrl = "https://upload.wikimedia.org/wikipedia/en/9/9b/Tame_Impala_-_Currents.png",
            year = "2015",
            songCount = 13
        ),
        Album(
            id = "album_4",
            title = "Starboy",
            artist = "The Weeknd",
            artUrl = "https://upload.wikimedia.org/wikipedia/en/3/39/The_Weeknd_-_Starboy.png",
            year = "2016",
            songCount = 18
        )
    )

    val artists = listOf(
        Artist(
            id = "artist_1",
            name = "The Weeknd",
            imageUrl = "https://i.scdn.co/image/ab6761610000e5eb214f3cf1cbc911ae5f31ce55",
            subscribers = "34M",
            monthlyListeners = "105M"
        ),
        Artist(
            id = "artist_2",
            name = "Daft Punk",
            imageUrl = "https://i.scdn.co/image/ab6761610000e5ebf38e0ad4de3cb7eb8f1bc40c",
            subscribers = "18M",
            monthlyListeners = "25M"
        ),
        Artist(
            id = "artist_3",
            name = "Tame Impala",
            imageUrl = "https://i.scdn.co/image/ab6761610000e5eb3e852960f279184df6d50ff4",
            subscribers = "5.2M",
            monthlyListeners = "30M"
        )
    )
}

@Composable
fun QuickPicksScreen(
    songs: List<Song> = SampleData.quickPicks,
    albums: List<Album> = SampleData.albums,
    artists: List<Artist> = SampleData.artists,
    currentSong: Song? = null,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("quick_picks_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp, start = 8.dp, end = 16.dp)
    ) {
        // Section: Quick picks header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Quick picks",
                        color = SlateTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "START RADIO",
                        color = SlateAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }

                // User Profile Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color = androidx.compose.ui.graphics.Color(0xFFE91E63), shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "D",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Section: Horizontal Scrolling Grid of Quick picks (4 rows per column, classic ViMusic)
        item {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(248.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(songs) { song ->
                    Box(modifier = Modifier.width(280.dp)) {
                        SongListItem(
                            song = song,
                            isPlaying = currentSong?.title == song.title,
                            onClick = { onSongClick(song) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // Section: Albums / Recommended Releases
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Albums",
                    color = SlateTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(albums) { album ->
                    Box(modifier = Modifier.width(140.dp)) {
                        AlbumGridItem(
                            album = album,
                            onClick = { onAlbumClick(album) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // Section: Artists
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Artists",
                    color = SlateTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(artists) { artist ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(88.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onArtistClick(artist) }
                            .padding(4.dp)
                            .testTag("artist_chip_${artist.name}")
                    ) {
                        AsyncImage(
                            model = artist.imageUrl,
                            contentDescription = artist.name,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(SlateSurface),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = artist.name,
                            color = SlateTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
