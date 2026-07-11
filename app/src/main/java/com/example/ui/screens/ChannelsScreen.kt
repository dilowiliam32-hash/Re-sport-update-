package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Channel
import com.example.ui.ZeSportViewModel
import com.example.ui.theme.*

@Composable
fun ChannelsScreen(
    viewModel: ZeSportViewModel,
    onNavigateToChannel: (String) -> Unit
) {
    val allChannels by viewModel.channels.collectAsStateWithLifecycle()
    val favoriteChannels by viewModel.favoriteChannels.collectAsStateWithLifecycle()

    var showOnlyFavorites by remember { mutableStateOf(false) }

    val displayedChannels = if (showOnlyFavorites) {
        allChannels.filter { ch -> favoriteChannels.any { it.channelId == ch.id } }
    } else {
        allChannels
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZeSportBg)
            .testTag("channels_screen_content")
    ) {
        // Upper Title Section
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Live Broadcasters",
                color = ZeSportAmber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Watch Channels",
                color = ZeSportText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Segment Tabs to select All vs Bookmarked Favorite Channels
        TabRow(
            selectedTabIndex = if (showOnlyFavorites) 1 else 0,
            containerColor = ZeSportSurface,
            contentColor = ZeSportText,
            divider = { HorizontalDivider(color = ZeSportLine) },
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (showOnlyFavorites) 1 else 0]),
                    color = ZeSportAccent
                )
            }
        ) {
            Tab(
                selected = !showOnlyFavorites,
                onClick = { showOnlyFavorites = false },
                text = { Text("All Channels", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = showOnlyFavorites,
                onClick = { showOnlyFavorites = true },
                text = { Text("My Favorites (${favoriteChannels.size})", fontWeight = FontWeight.Bold) }
            )
        }

        if (displayedChannels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showOnlyFavorites) "No favorite channels added yet.\nTap the star to add!" else "No channels available.",
                    color = ZeSportMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedChannels, key = { it.id }) { channel ->
                    val isFav = favoriteChannels.any { it.channelId == channel.id }
                    ChannelCard(
                        channel = channel,
                        isFavorite = isFav,
                        onToggleFavorite = { viewModel.toggleChannelFavorite(channel) },
                        onWatch = { onNavigateToChannel(channel.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelCard(
    channel: Channel,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onWatch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ZeSportLine, RoundedCornerShape(8.dp))
            .background(ZeSportSurface)
            .clickable { onWatch() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Live Header tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(ZeSportAccent, CircleShape)
                    )
                    Text(
                        text = "ON AIR",
                        color = ZeSportAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (isFavorite) ZeSportAmber else ZeSportMuted
                    )
                }
            }

            // Main Channel Info Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ZeSportAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = channel.logoInitial,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = channel.title,
                            color = ZeSportText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        if (channel.isHd) {
                            Box(
                                modifier = Modifier
                                    .background(ZeSportAmber.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "HD",
                                    color = ZeSportAmber,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = channel.subtitle,
                        color = ZeSportMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Watch bottom bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .border(1.dp, ZeSportLine, RoundedCornerShape(4.dp))
                    .background(ZeSportSurface2)
                    .clickable { onWatch() }
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Watch live stream now",
                    color = ZeSportText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = ZeSportAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
