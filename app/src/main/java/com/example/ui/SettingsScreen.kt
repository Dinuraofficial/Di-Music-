package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.YouTubeAuthManager
import com.example.ui.theme.SlateAccent
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateDivider
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary

@Composable
fun SettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authManager = remember { YouTubeAuthManager.getInstance(context) }
    val isLoggedIn by authManager.isLoggedIn.collectAsState()

    var audioQuality by remember { mutableStateOf("High (320 kbps)") }
    var showQualityDialog by remember { mutableStateOf(false) }
    var normalizeVolume by remember { mutableStateOf(true) }
    var gaplessPlayback by remember { mutableStateOf(true) }
    var hdArtwork by remember { mutableStateOf(true) }
    var coinFlipLyrics by remember { mutableStateOf(true) }
    var cacheClearedMessage by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
            .statusBarsPadding()
            .testTag("settings_screen")
    ) {
        // Top Header with Mixer / Tune Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SlateSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Mixer Controller",
                        tint = SlateAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Settings",
                        color = SlateTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "DiMusic Mixer & Preferences",
                        color = SlateTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("settings_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Settings",
                    tint = SlateTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        HorizontalDivider(color = SlateDivider)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Account
            item {
                SettingsSectionHeader(title = "Account & Sync")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (isLoggedIn) SlateAccent else SlateTextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Column {
                                Text(
                                    text = if (isLoggedIn) "Google Account Connected" else "Not Signed In",
                                    color = SlateTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isLoggedIn) "YouTube Music Session Active" else "Sign in to access restricted tracks",
                                    color = SlateTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (isLoggedIn) {
                            OutlinedButton(
                                onClick = { showLogoutDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Sign Out", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Section 2: Audio Quality & Playback
            item {
                SettingsSectionHeader(title = "Audio & Playback")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsCard {
                    Column {
                        SettingsClickableRow(
                            icon = Icons.Default.HighQuality,
                            title = "Streaming Quality",
                            subtitle = audioQuality,
                            onClick = { showQualityDialog = true }
                        )

                        HorizontalDivider(color = SlateDivider.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                        SettingsSwitchRow(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            title = "Normalize Volume",
                            subtitle = "Adjust track volume to consistent loudness levels",
                            checked = normalizeVolume,
                            onCheckedChange = { normalizeVolume = it }
                        )

                        HorizontalDivider(color = SlateDivider.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                        SettingsSwitchRow(
                            icon = Icons.Default.MusicNote,
                            title = "Gapless Playback",
                            subtitle = "Seamless audio transitions between queue tracks",
                            checked = gaplessPlayback,
                            onCheckedChange = { gaplessPlayback = it }
                        )
                    }
                }
            }

            // Section 3: Visuals & Player Customization
            item {
                SettingsSectionHeader(title = "Player & Visuals")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsCard {
                    Column {
                        SettingsClickableRow(
                            icon = Icons.Default.Palette,
                            title = "App Theme",
                            subtitle = "Pure Dark, Purple, Black",
                            onClick = { /* TODO: Show theme dialog */ }
                        )

                        HorizontalDivider(color = SlateDivider.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                        SettingsSwitchRow(
                            icon = Icons.Default.HighQuality,
                            title = "HD Album Covers",
                            subtitle = "Upgrade thumbnail feeds to high-resolution 1080p artwork",
                            checked = hdArtwork,
                            onCheckedChange = { hdArtwork = it }
                        )

                        HorizontalDivider(color = SlateDivider.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                        SettingsSwitchRow(
                            icon = Icons.Default.Tune,
                            title = "Coin Flip Cover & Lyrics",
                            subtitle = "Tap album cover to 3D flip between artwork and synchronized lyrics",
                            checked = coinFlipLyrics,
                            onCheckedChange = { coinFlipLyrics = it }
                        )
                    }
                }
            }

            // Section 4: Storage & Cache
            item {
                SettingsSectionHeader(title = "Storage & Cache")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsCard {
                    Column {
                        SettingsClickableRow(
                            icon = Icons.Default.DeleteSweep,
                            title = "Clear Thumbnail Cache",
                            subtitle = "Frees up local image cache memory",
                            onClick = {
                                cacheClearedMessage = "Thumbnail cache cleaned successfully!"
                            }
                        )

                        cacheClearedMessage?.let { msg ->
                            Text(
                                text = msg,
                                color = SlateAccent,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                            )
                        }
                    }
                }
            }

            // Section 5: About DiMusic
            item {
                SettingsSectionHeader(title = "About")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(
                            text = "Empty for now.",
                            color = SlateTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Audio Quality Dialog
    if (showQualityDialog) {
        val options = listOf("High (320 kbps)", "Standard (256 kbps)", "Data Saver (128 kbps)")
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            containerColor = SlateSurface,
            title = {
                Text("Streaming Audio Quality", color = SlateTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    audioQuality = option
                                    showQualityDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (audioQuality == option),
                                onClick = {
                                    audioQuality = option
                                    showQualityDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = SlateAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option, color = SlateTextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("Cancel", color = SlateAccent)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = SlateSurface,
            title = {
                Text("Disconnect Account", color = SlateTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to sign out? You will need to sign in again to play age-restricted or premium tracks.",
                    color = SlateTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authManager.clearAuth()
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = SlateTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = SlateAccent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SlateAccent,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    color = SlateTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = SlateTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SlateAccent,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    color = SlateTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = SlateTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SlateAccent,
                uncheckedThumbColor = SlateTextSecondary,
                uncheckedTrackColor = SlateSurfaceVariant
            )
        )
    }
}
