<div align="center">

  <!-- Di Music Logo -->
  <img src="https://github.com/Dinuraofficial/Di-Music-/blob/main/C02487F5-1FC3-46B9-812B-B313AEEBC9FE.png" width="128" height="128" alt="Di Music Logo"/>

  <h1>Di Music</h1>

  <p><b>An Android music application for streaming music from YouTube Music</b></p>

</div>

<br />
<hr />
<br />

<!-- Screenshots Grid -->
<p align="center">
  <img src="./screenshots/1.png" width="30%" alt="Quick Picks"/>
  <img src="./screenshots/2.png" width="30%" alt="Search"/>
  <img src="./screenshots/3.png" width="30%" alt="Artist and Album"/>
  <br /><br />
  <img src="./screenshots/4.png" width="30%" alt="Full Player"/>
  <img src="./screenshots/5.png" width="30%" alt="Lock Screen Controls"/>
  <img src="./screenshots/6.png" width="30%" alt="Notification Controls"/>

  <H1> still looking for a good device to take some ss.... btw why are y here i never invited u here </H1>
</p>

<br />
<hr />
<br />

## 🌟 Key Features & Options Overview

### 1. 🎧 Audio Playback & Engine
- **Background Playback**: Continuous audio streaming powered by Android Media3 / ExoPlayer with a foreground playback service.
- **Lock Screen & Notification Controls**: Media session support with play, pause, skip, previous, and seek controls directly from notifications and lock screen.
- **Audio Focus Management**: Automatically pauses when unplugging headphones or receiving phone calls.
- **YouTube Stream Resolution**: Direct audio stream extraction for fast and high-quality streaming.

---

### 2. 🧭 Navigation & Core Interface
- **Left Navigation Rail**: Minimalist vertical navigation with rotated labels and Material 3 icons:
  - **Quick Picks**: Personalized home dashboard with adaptive recommendations.
  - **Songs**: Flat list of songs and tracks.
  - **Albums**: Grid and list of curated and discovered albums.
  - **Artists**: Explore featured artists, top tracks, and discographies.
  - **Playlists**: Access Favorites and custom playlists.
- **Mixer / Settings Icon (`Tune`)**: Top-left equalizer/mixer icon with movable switches to access settings and preferences.
- **Floating Search Button**: Quick-access squircle floating action button to open search anytime.
- **Edge-to-Edge Design**: Full immersive layout with dynamic Android status and navigation bar insets.

---

### 3. 🔍 Comprehensive Search System
- **Multi-Category Filter Tabs**:
  - **Songs**: Single tracks and songs.
  - **Albums**: Complete albums and EPs.
  - **Artists**: Performers and bands.
  - **Videos**: Music videos (seamlessly streamed as audio).
  - **Playlists**: YouTube Music community playlists.
  - **Featured**: Trending and curated releases.
- **Interactive Search Results**:
  - Tapping any artist opens the full **Artist Page**.
  - Tapping any album opens the full **Album Detail Page**.
  - Tapping any song begins immediate playback.
- **Search History**: Instant search query recall with one-tap re-querying and clear history options.

---

### 4. 🧠 Adaptive History & Personalization
- **Listening History Tracker (`ListeningHistoryManager`)**:
  - Automatically records played tracks and builds play counts per artist.
  - Saves recent search queries and listening history locally in private storage.
- **Smart Quick Picks**:
  - Dynamically prioritizes albums and songs from artists you listen to most.
  - Performs concurrent background fetches to surface new albums related to your favorite artists.
  - 22+ curated fallback albums across multiple genres (Synthwave, R&B, Pop, Rock, Hip-Hop, Indie).

---

### 5. ⋯ 3-Dot Song Context Menu
Available across Search, Quick Picks, Song List, Artist Page, and Album View:
- **Play Next**: Inserts the track immediately after the currently playing song.
- **Add to Queue**: Appends the song to the end of the current playback queue.
- **Add to Playlist**: Opens a modern 2x2 visual grid dialog.
- **Save to Favorites / Remove**: One-tap toggle for your Favorites library.
- **View Artist**: Direct jump to the artist's profile page.

---

### 6. 🗂️ Playlists & Library
- **2x2 Visual Add-to-Playlist Grid**:
  - Clean card grid displaying playlists with artwork/icon, title, and song count.
  - One-tap addition to any playlist or Favorites.
  - In-dialog "+ New Playlist" creation.
- **Playlist Detail View**:
  - Track count, duration, and cover banner.
  - Play and Shuffle all tracks in the playlist.
  - Reorder and remove individual tracks.
  - Delete user-created playlists.
  - Add songs directly from existing library tracks.

---

### 7. 🎛️ Now Playing & Player Modes
- **Persistent Mini Player**:
  - Floats at the bottom above content.
  - Displays thumbnail, title, artist, live playback progress, and play/pause/skip buttons.
  - Tap anywhere to expand to full player.
- **Fullscreen Player**:
  - High-res artwork display with ambient gradient backdrop.
  - Interactive scrub bar with real-time position and duration timestamps.
  - Play/Pause, Next, Previous, Shuffle, and Repeat mode toggles.
  - **Interactive Queue Drawer**: View upcoming songs and reorder tracks.
  - **3D Flip Lyrics Screen**: Synchronized lyrics view.
  - **Clickable Artist**: Direct navigation from player to the artist's full catalog.

---

### 8. 🎤 Artist & 💿 Album Detail Pages
- **Artist Page**:
  - High-resolution hero image, subscriber stats, and monthly listener metrics.
  - Play and Shuffle buttons for the artist's catalog.
  - Top songs list with 3-dot options.
  - Albums / Discography grid with drill-down into each album.
  - Origin-aware back navigation (returns to Search if opened from Search, or Home if opened from Home).
- **Album Page**:
  - High-res album cover, release date, artist name, and track count.
  - Complete tracklist with durations and active playing track highlight.
  - Play and Shuffle controls.

---

### 9. ⚙️ Settings & Customization
- **Mixer / Equalizer Screen**:
  - Google / YouTube account login and status.
  - Audio quality and streaming preferences.
  - App theme and visual settings.
  - Local cache and playback history management.

---

## 🛠️ Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Audio Engine**: AndroidX Media3 / ExoPlayer
- **Image Loading**: Coil
- **Architecture**: MVVM with Kotlin Coroutines & StateFlow
- **Persistence**: SharedPreferences / Local Cache
<h2>Installation</h2>
<br>
<a href="https://github.com/Dinuraofficial/Di-Music-/releases/download/V0.0.2/v0.0.2.apk">
<a href="https://github.com/Dinuraofficial/Di-Music-/releases/download/V0.0.2/V.0.0.2.apk">
  <img src="https://raw.githubusercontent.com/machiav3lli/oandbackupx/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" alt="Get it on GitHub" height="80" />
</a>
