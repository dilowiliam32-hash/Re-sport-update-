package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.ChatComment
import com.example.data.Match
import com.example.data.MatchStat
import com.example.ui.ZeSportViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: String,
    viewModel: ZeSportViewModel,
    onBack: () -> Unit
) {
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val match = remember(matches, matchId) { matches.find { it.id == matchId } }
    val favoriteMatches by viewModel.favoriteMatches.collectAsStateWithLifecycle()
    val isFavorite = favoriteMatches.any { it.matchId == matchId }

    if (match == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ZeSportBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Match not found", color = ZeSportText, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = ZeSportAccent)) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    // Initialize chat room target id for this match
    LaunchedEffect(matchId) {
        viewModel.setActiveChatTarget("match_$matchId")
    }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Stream, 1 = Info, 2 = Stats, 3 = Lineups

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${match.homeTeam} vs ${match.awayTeam}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ZeSportText
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleMatchFavorite(match) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isFavorite) ZeSportAmber else ZeSportText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZeSportSurface,
                    titleContentColor = ZeSportText
                )
            )
        },
        containerColor = ZeSportBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ZeSportBg)
        ) {
            // Live/Upcoming Scoreboard Hero Header
            MatchScoreboardHeader(match = match)

            // Interactive Live Simulator Panel for today's match
            if (match.matchDate == "2026-07-11") {
                MatchSimulatorPanel(
                    status = match.status,
                    onSimulate = { viewModel.advanceMatchStatus(match.id) }
                )
            }

            // Tabs Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ZeSportSurface,
                contentColor = ZeSportText,
                divider = { HorizontalDivider(color = ZeSportLine) },
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ZeSportAccent
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Stream", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Info", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Stats", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Lineups", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
            }

            // Tab Contents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> MatchStreamTab(viewModel = viewModel, match = match)
                    1 -> MatchInfoTab(match = match)
                    2 -> MatchStatsTab(match = match)
                    3 -> MatchLineupsTab(match = match)
                }
            }
        }
    }
}

@Composable
fun MatchScoreboardHeader(match: Match) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ZeSportSurface2)
            .border(1.dp, ZeSportLine)
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${match.competition} · ${match.matchDate}",
                color = ZeSportAmber,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = match.homeBadge,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.homeTeam,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Scores & Status Block
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(100.dp)
                ) {
                    if (match.status != "Upcoming") {
                        Text(
                            text = "${match.homeScore} - ${match.awayScore}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        Text(
                            text = "VS",
                            color = ZeSportMuted,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
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
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = match.status,
                            color = when (match.status) {
                                "Live" -> ZeSportGreen
                                "Upcoming" -> ZeSportBlue
                                else -> ZeSportMuted
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Away Team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = match.awayBadge,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.awayTeam,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun MatchStreamTab(viewModel: ZeSportViewModel, match: Match) {
    val comments by viewModel.activeChatComments.collectAsStateWithLifecycle()
    var inputMessage by remember { mutableStateOf("") }
    var selectedServer by remember { mutableStateOf(0) }
    var isAdVisible by remember { mutableStateOf(true) }

    val resolvedStreams by viewModel.resolvedStreams.collectAsStateWithLifecycle()
    val isResolvingStream by viewModel.isResolvingStream.collectAsStateWithLifecycle()

    val streamResult = resolvedStreams[match.id]
    val isLoading = isResolvingStream[match.id] ?: false

    // Auto-trigger resolution for this match stream on launch
    LaunchedEffect(match.id) {
        viewModel.resolveStream(
            id = match.id,
            title = "${match.homeTeam} vs ${match.awayTeam}",
            subtitle = match.competition,
            type = "Match"
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Auto-scroll chat to latest messages
    LaunchedEffect(comments.size) {
        if (comments.isNotEmpty()) {
            listState.animateScrollToItem(comments.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // High-Fidelity Video Player Simulator with dynamic live streams
        VideoPlayerSimulator(
            title = "${match.homeTeam} vs ${match.awayTeam}",
            selectedServer = selectedServer,
            onServerSelected = { selectedServer = it },
            resolvedStreamResult = streamResult,
            isLoading = isLoading,
            onRefresh = {
                viewModel.resolveStream(
                    id = match.id,
                    title = "${match.homeTeam} vs ${match.awayTeam}",
                    subtitle = match.competition,
                    type = "Match"
                )
            }
        )

        // Closeable AD Banner block (matches CSS styles perfectly)
        AnimatedVisibility(
            visible = isAdVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ZeSportLine)
                    .background(Color(0xFF171410))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ADVERTISEMENT",
                            color = ZeSportAmber,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Get Ze Sport Premium for Ad-free streams & Ultra HD 4K!",
                            color = ZeSportText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = { isAdVisible = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Ad",
                            tint = ZeSportMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Live Chat Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(ZeSportBg)
        ) {
            // Live Chat Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZeSportSurface)
                    .border(1.dp, ZeSportLine)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(ZeSportGreen, CircleShape))
                    Text("LIVE CHAT", color = ZeSportText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text("Room: Stream_Server_${selectedServer + 1}", color = ZeSportMuted, fontSize = 10.sp)
            }

            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(comments, key = { it.id }) { comment ->
                    ChatItemRow(comment = comment)
                }
            }

            // Input Editor Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZeSportSurface)
                    .border(1.dp, ZeSportLine)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = { Text("Write a message...", color = ZeSportMuted, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text_field"),
                    maxLines = 2,
                    textStyle = TextStyle(color = ZeSportText, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ZeSportBg,
                        unfocusedContainerColor = ZeSportBg,
                        focusedBorderColor = ZeSportAccent,
                        unfocusedBorderColor = ZeSportLine
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputMessage.isNotBlank()) {
                                viewModel.postComment(inputMessage)
                                inputMessage = ""
                            }
                        }
                    )
                )

                IconButton(
                    onClick = {
                        if (inputMessage.isNotBlank()) {
                            viewModel.postComment(inputMessage)
                            inputMessage = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(ZeSportAccent, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayerSimulator(
    title: String,
    selectedServer: Int,
    onServerSelected: (Int) -> Unit,
    resolvedStreamResult: com.example.data.ExtractedStreamResult? = null,
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var copyFeedbackVisible by remember { mutableStateOf(false) }

    LaunchedEffect(copyFeedbackVisible) {
        if (copyFeedbackVisible) {
            kotlinx.coroutines.delay(2000)
            copyFeedbackVisible = false
        }
    }

    val streamList = resolvedStreamResult?.streamUrls ?: emptyList()
    val currentStreamUrl = if (streamList.isNotEmpty() && selectedServer < streamList.size) {
        streamList[selectedServer].url
    } else {
        ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(
                    color = ZeSportAccent,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Connecting directly to ze-sport.com...",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gemini resolving live stream servers...",
                    color = ZeSportMuted,
                    fontSize = 11.sp
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(ZeSportAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Stream",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Streaming: $title",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (streamList.isNotEmpty() && selectedServer < streamList.size) {
                        "${streamList[selectedServer].name} · Active live stream"
                    } else {
                        "Server ${selectedServer + 1} · Live simulation playback"
                    },
                    color = ZeSportGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top Overlays: Servers + Manual Sync/Refresh button
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (streamList.isNotEmpty()) {
                    streamList.forEachIndexed { index, stream ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selectedServer == index) ZeSportAccent else Color.DarkGray,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { onServerSelected(index) }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = stream.name,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    listOf("Server 1", "Server 2", "Server 3 (Back-up)").forEachIndexed { index, name ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selectedServer == index) ZeSportAccent else Color.DarkGray,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { onServerSelected(index) }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = name,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Sync/Refresh Button
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync from original site",
                    tint = if (isLoading) ZeSportAccent else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Bottom Overlay: Copy Stream URL for VLC/external player if resolved
        if (currentStreamUrl.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "HLS Stream Link",
                        tint = ZeSportAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = currentStreamUrl,
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (copyFeedbackVisible) ZeSportGreen else ZeSportAccent)
                        .clickable {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(currentStreamUrl))
                            copyFeedbackVisible = true
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (copyFeedbackVisible) "Copied!" else "Copy URL",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ChatItemRow(comment: ChatComment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // User Initial Avatar icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF263040), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.username.firstOrNull()?.toString()?.uppercase() ?: "U",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.username,
                    color = ZeSportAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(comment.timestamp),
                    color = ZeSportMuted,
                    fontSize = 9.sp
                )
            }
            Text(
                text = comment.message,
                color = ZeSportText,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun MatchInfoTab(match: Match) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ZeSportSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ZeSportLine)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Match Details",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    HorizontalDivider(color = ZeSportLine)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Competition", color = ZeSportMuted, fontSize = 12.sp)
                        Text(match.competition, color = ZeSportText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Stadium", color = ZeSportMuted, fontSize = 12.sp)
                        Text("MetLife Stadium, NY", color = ZeSportText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Capacity", color = ZeSportMuted, fontSize = 12.sp)
                        Text("82,500 seats", color = ZeSportText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Referee", color = ZeSportMuted, fontSize = 12.sp)
                        Text("Szymon Marciniak (Poland)", color = ZeSportText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ZeSportSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ZeSportLine)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Match Summary",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    HorizontalDivider(color = ZeSportLine)
                    Text(
                        text = match.matchSummary.ifEmpty { "Detailed information regarding player stats, stream channels, and recap logs will show here dynamically." },
                        color = ZeSportText,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Timeline events
        if (match.timelineEvents.isNotEmpty()) {
            item {
                Text(
                    text = "Timeline Events",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(match.timelineEvents) { event ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ZeSportLine, RoundedCornerShape(8.dp))
                        .background(ZeSportSurface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(ZeSportAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = event.minute,
                            color = ZeSportAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.playerName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${event.type} · ${event.detail}",
                            color = ZeSportMuted,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (event.team == "home") match.homeTeam else match.awayTeam,
                            color = ZeSportAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MatchStatsTab(match: Match) {
    if (match.statistics.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No statistics logs compiled yet.", color = ZeSportMuted, fontSize = 14.sp)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(match.statistics) { stat ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stat.homeValue, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(stat.name, color = ZeSportMuted, fontSize = 12.sp)
                    Text(stat.awayValue, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Custom double-progress indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ZeSportLine)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(stat.homeProgress)
                            .background(ZeSportAccent)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f - stat.homeProgress + 1f - stat.awayProgress)
                            .background(ZeSportLine)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(stat.awayProgress)
                            .background(ZeSportAmber)
                    )
                }
            }
        }
    }
}

@Composable
fun MatchLineupsTab(match: Match) {
    if (match.homeLineup.isEmpty() && match.awayLineup.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Starting lineups unannounced.", color = ZeSportMuted, fontSize = 14.sp)
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Home Squad
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(match.homeTeam, color = ZeSportAccent, fontWeight = FontWeight.Black, fontSize = 14.sp)
            HorizontalDivider(color = ZeSportAccent)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(match.homeLineup) { player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ZeSportLine, RoundedCornerShape(6.dp))
                            .background(ZeSportSurface)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            player.number,
                            color = ZeSportMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.width(18.dp)
                        )
                        Column {
                            Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(player.position, color = ZeSportMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Away Squad
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(match.awayTeam, color = ZeSportAmber, fontWeight = FontWeight.Black, fontSize = 14.sp)
            HorizontalDivider(color = ZeSportAmber)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(match.awayLineup) { player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ZeSportLine, RoundedCornerShape(6.dp))
                            .background(ZeSportSurface)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            player.number,
                            color = ZeSportMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.width(18.dp)
                        )
                        Column {
                            Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(player.position, color = ZeSportMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchSimulatorPanel(
    status: String,
    onSimulate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, ZeSportLine, RoundedCornerShape(10.dp))
            .background(ZeSportSurface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                when (status) {
                                    "Live" -> ZeSportGreen
                                    "Upcoming" -> ZeSportBlue
                                    else -> ZeSportMuted
                                },
                                CircleShape
                            )
                    )
                    Text(
                        text = "Match Simulator",
                        color = ZeSportAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (status) {
                        "Upcoming" -> "Start live match simulation"
                        "Live" -> "Simulate next match event & goals"
                        else -> "Match finished. Reset to Upcoming"
                    },
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onSimulate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (status) {
                        "Upcoming" -> ZeSportBlue
                        "Live" -> ZeSportGreen
                        else -> ZeSportAccent
                    }
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = when (status) {
                        "Upcoming" -> "Kickoff"
                        "Live" -> "Next Event"
                        else -> "Reset"
                    },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
