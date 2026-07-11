package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Channel
import com.example.data.Highlight
import com.example.data.Match
import com.example.ui.ZeSportViewModel
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: ZeSportViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToChannel: (String) -> Unit,
    onNavigateToHighlight: (String) -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val matches by viewModel.filteredMatches.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val highlights by viewModel.highlights.collectAsStateWithLifecycle()
    val competitions by viewModel.competitions.collectAsStateWithLifecycle()
    val favoriteMatches by viewModel.favoriteMatches.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ZeSportBg)
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero / Banner Card
        item {
            HomeHeroBanner()
        }

        // Match Center Header & Date Selector
        item {
            MatchCenterSelector(
                selectedDate = selectedDate,
                onDateSelected = { viewModel.updateDateFilter(it) }
            )
        }

        // Today's Matches Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's schedule",
                        color = ZeSportAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Today's matches",
                        color = ZeSportText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = "${matches.size} matches",
                    color = ZeSportMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Today's Matches List
        if (matches.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ZeSportLine, RoundedCornerShape(8.dp))
                        .background(ZeSportSurface)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = ZeSportMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No matches scheduled for this date.",
                            color = ZeSportMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(matches, key = { it.id }) { match ->
                val isFavorite = favoriteMatches.any { it.matchId == match.id }
                MatchRowItem(
                    match = match,
                    isFavorite = isFavorite,
                    onToggleFavorite = { viewModel.toggleMatchFavorite(match) },
                    onNavigate = { onNavigateToMatch(match.id) }
                )
            }
        }

        // Competitions horizontal scroll
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Featured Competitions",
                    color = ZeSportText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(competitions) { comp ->
                        CompetitionChip(name = comp)
                    }
                }
            }
        }

        // Watch Now / Mini Channels Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Channels",
                        color = ZeSportAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Watch now",
                        color = ZeSportText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        items(channels, key = { it.id }) { channel ->
            ChannelMiniRowItem(
                channel = channel,
                onClick = { onNavigateToChannel(channel.id) }
            )
        }

        // Video Highlights Header
        item {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Text(
                    text = "Video",
                    color = ZeSportAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Highlights",
                    color = ZeSportText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Highlights list
        items(highlights, key = { it.id }) { highlight ->
            HighlightCardItem(
                highlight = highlight,
                onClick = { onNavigateToHighlight(highlight.id) }
            )
        }
    }
}

@Composable
fun HomeHeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(ZeSportAccent, Color(0xFF6B0000))
                )
            )
            .border(1.dp, ZeSportLine, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "LIVE BROADCASTS",
                    color = ZeSportAmber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "FIFA World Cup 2026",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Full live streams, schedules and match summaries offline.",
                color = ZeSportText.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun MatchCenterSelector(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    // Dates selector mock (allows switching between 2026-07-10, 2026-07-11, 2026-07-12)
    val dates = listOf("2026-07-09", "2026-07-10", "2026-07-11", "2026-07-12")
    var currentIndex by remember(selectedDate) {
        mutableStateOf(dates.indexOf(selectedDate).coerceIn(0, dates.lastIndex))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ZeSportLine, RoundedCornerShape(8.dp))
            .background(ZeSportSurface)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 16.dp)
                        .background(ZeSportAccent, RoundedCornerShape(2.dp))
                )
                Text(
                    text = "Match Center",
                    color = ZeSportText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Selected Date",
                    color = ZeSportMuted,
                    fontSize = 11.sp
                )
            }
            Text(
                text = "Fixtures, live scores and results for selected date.",
                color = ZeSportMuted,
                fontSize = 11.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentIndex > 0) {
                            currentIndex--
                            onDateSelected(dates[currentIndex])
                        }
                    },
                    enabled = currentIndex > 0
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Date", tint = if (currentIndex > 0) ZeSportText else ZeSportMuted)
                }

                Text(
                    text = dates[currentIndex],
                    color = ZeSportAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = {
                        if (currentIndex < dates.lastIndex) {
                            currentIndex++
                            onDateSelected(dates[currentIndex])
                        }
                    },
                    enabled = currentIndex < dates.lastIndex
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Date", tint = if (currentIndex < dates.lastIndex) ZeSportText else ZeSportMuted)
                }
            }
        }
    }
}

@Composable
fun MatchRowItem(
    match: Match,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onNavigate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ZeSportLine, RoundedCornerShape(8.dp))
            .background(ZeSportSurface)
            .clickable { onNavigate() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time & Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(64.dp)
            ) {
                Text(
                    text = match.matchTime,
                    color = ZeSportText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(
                            when (match.status) {
                                "Live" -> ZeSportGreen.copy(alpha = 0.15f)
                                "Upcoming" -> ZeSportBlue.copy(alpha = 0.15f)
                                else -> ZeSportMuted.copy(alpha = 0.15f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = match.status,
                        color = when (match.status) {
                            "Live" -> ZeSportGreen
                            "Upcoming" -> ZeSportBlue
                            else -> ZeSportMuted
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Teams & Score
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Team Home
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = match.homeBadge,
                        contentDescription = "${match.homeTeam} Badge",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = match.homeTeam,
                        color = ZeSportText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Team Away
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = match.awayBadge,
                        contentDescription = "${match.awayTeam} Badge",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = match.awayTeam,
                        color = ZeSportText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Scores
            if (match.status != "Upcoming") {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = match.homeScore,
                        color = ZeSportText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = match.awayScore,
                        color = ZeSportText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                Text(
                    text = "VS",
                    color = ZeSportMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Bookmark Icon Button
            IconButton(
                onClick = { onToggleFavorite() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite Match",
                    tint = if (isFavorite) ZeSportAmber else ZeSportMuted
                )
            }
        }
    }
}

@Composable
fun CompetitionChip(name: String) {
    Box(
        modifier = Modifier
            .background(ZeSportSurface, RoundedCornerShape(16.dp))
            .border(1.dp, ZeSportLine, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(Color(0xFF263040), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.toString() ?: "C",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
            Text(
                text = name,
                color = ZeSportText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ChannelMiniRowItem(
    channel: Channel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ZeSportLine, RoundedCornerShape(8.dp))
            .background(ZeSportSurface)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ZeSportAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = channel.logoInitial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.title,
                    color = ZeSportText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = channel.subtitle,
                    color = ZeSportMuted,
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Stream Now",
                tint = ZeSportAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun HighlightCardItem(
    highlight: Highlight,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ZeSportLine, RoundedCornerShape(8.dp))
            .background(ZeSportSurface)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.BottomStart
            ) {
                AsyncImage(
                    model = highlight.thumbnailUrl,
                    contentDescription = highlight.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Play Icon Overlay
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(36.dp)
                        .background(ZeSportAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Highlight",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = highlight.subtitle,
                    color = ZeSportAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = highlight.title,
                    color = ZeSportText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = highlight.date,
                    color = ZeSportMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
