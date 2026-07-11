package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Match
import com.example.data.TournamentGroup
import com.example.ui.ZeSportViewModel
import com.example.ui.theme.*

// Custom World Cup Accent Color (from CSS --ss-tournament)
val WorldCupAccent = Color(0xFF42D3C1)

@Composable
fun WorldCupScreen(
    viewModel: ZeSportViewModel,
    onNavigateToMatch: (String) -> Unit
) {
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val groups by viewModel.worldCupGroups.collectAsStateWithLifecycle()
    val favoriteMatches by viewModel.favoriteMatches.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Standings, 1 = Fixtures

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ZeSportBg)
            .testTag("world_cup_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // World Cup Hero Banner
        item {
            WorldCupHeroBanner()
        }

        // Tab Row Switcher (Standings vs Fixtures)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ZeSportSurface,
                contentColor = ZeSportText,
                divider = { HorizontalDivider(color = ZeSportLine) },
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = WorldCupAccent
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Standings", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Fixtures", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTab == 0) {
            // Group Standings View
            items(groups) { group ->
                GroupStandingsTable(group = group)
            }
        } else {
            // World Cup Fixtures View
            val wcMatches = matches.filter { it.competition == "FIFA World Cup" }
            if (wcMatches.isEmpty()) {
                item {
                    Text(
                        text = "No fixtures found.",
                        color = ZeSportMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    )
                }
            } else {
                items(wcMatches) { match ->
                    val isFav = favoriteMatches.any { it.matchId == match.id }
                    MatchRowItem(
                        match = match,
                        isFavorite = isFav,
                        onToggleFavorite = { viewModel.toggleMatchFavorite(match) },
                        onNavigate = { onNavigateToMatch(match.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun WorldCupHeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ZeSportSurface)
            .border(1.dp, ZeSportLine, RoundedCornerShape(8.dp))
            .border(
                width = 3.dp,
                color = WorldCupAccent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TOURNAMENT CENTER",
                    color = WorldCupAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "FIFA World Cup 2026",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Co-hosted by Canada, Mexico, and the United States.",
                    color = ZeSportMuted,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(76.dp)
            ) {
                Text(
                    text = "48",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 32.sp
                )
                Text(
                    text = "TEAMS",
                    color = WorldCupAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GroupStandingsTable(group: TournamentGroup) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ZeSportSurface),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ZeSportLine),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Group Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZeSportSurface2)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = group.groupName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Table Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#", color = ZeSportMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                Text(text = "Team", color = ZeSportMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.width(130.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "PL", color = ZeSportMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                    Text(text = "W", color = ZeSportMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
                    Text(text = "GD", color = ZeSportMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
                    Text(text = "PTS", color = WorldCupAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(36.dp))
                }
            }

            HorizontalDivider(color = ZeSportLine)

            // Table Rows
            group.standings.forEach { row ->
                val diff = row.goalsFor - row.goalsAgainst
                val diffText = if (diff > 0) "+$diff" else "$diff"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.position.toString(),
                        color = if (row.position <= 2) WorldCupAccent else ZeSportMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.width(20.dp)
                    )

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = row.teamBadge,
                            contentDescription = "${row.teamName} logo",
                            modifier = Modifier.size(20.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = row.teamName,
                            color = ZeSportText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier.width(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = row.played.toString(), color = ZeSportText, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                        Text(text = row.won.toString(), color = ZeSportText, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
                        Text(text = diffText, color = if (diff > 0) ZeSportGreen else if (diff < 0) ZeSportAccent else ZeSportMuted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
                        Text(text = row.points.toString(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, modifier = Modifier.width(36.dp))
                    }
                }
                HorizontalDivider(color = ZeSportLine.copy(alpha = 0.5f))
            }
        }
    }
}
