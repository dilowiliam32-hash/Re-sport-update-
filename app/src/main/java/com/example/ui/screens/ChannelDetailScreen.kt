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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Channel
import com.example.ui.ZeSportViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelDetailScreen(
    channelId: String,
    viewModel: ZeSportViewModel,
    onBack: () -> Unit
) {
    val channel = remember(channelId) { viewModel.getChannelById(channelId) }
    val favoriteChannels by viewModel.favoriteChannels.collectAsStateWithLifecycle()
    val isFavorite = favoriteChannels.any { it.channelId == channelId }

    if (channel == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ZeSportBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Channel not found", color = ZeSportText, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = ZeSportAccent)) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    // Set Active Chat target to this channel
    LaunchedEffect(channelId) {
        viewModel.setActiveChatTarget("channel_$channelId")
    }

    val comments by viewModel.activeChatComments.collectAsStateWithLifecycle()
    var inputMessage by remember { mutableStateOf("") }
    var selectedServer by remember { mutableStateOf(0) }
    var isAdVisible by remember { mutableStateOf(true) }

    val resolvedStreams by viewModel.resolvedStreams.collectAsStateWithLifecycle()
    val isResolvingStream by viewModel.isResolvingStream.collectAsStateWithLifecycle()

    val streamResult = resolvedStreams[channelId]
    val isLoading = isResolvingStream[channelId] ?: false

    // Auto-trigger resolution for this channel stream on launch
    LaunchedEffect(channelId) {
        viewModel.resolveStream(
            id = channelId,
            title = channel.title,
            subtitle = channel.subtitle,
            type = "Channel"
        )
    }

    val listState = rememberLazyListState()

    // Scroll chat to end
    LaunchedEffect(comments.size) {
        if (comments.isNotEmpty()) {
            listState.animateScrollToItem(comments.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = channel.title,
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
                    IconButton(onClick = { viewModel.toggleChannelFavorite(channel) }) {
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
            // Visual Player Block
            VideoPlayerSimulator(
                title = channel.title,
                selectedServer = selectedServer,
                onServerSelected = { selectedServer = it },
                resolvedStreamResult = streamResult,
                isLoading = isLoading,
                onRefresh = {
                    viewModel.resolveStream(
                        id = channelId,
                        title = channel.title,
                        subtitle = channel.subtitle,
                        type = "Channel"
                    )
                }
            )

            // Closeable Ad Spot
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
                            text = "PROMOTED SPONSOR",
                            color = ZeSportAmber,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enjoy buffer-free streaming? Support us by visiting our partner sites!",
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

            // Info Card Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ZeSportLine)
                    .background(ZeSportSurface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ZeSportAccent, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(channel.logoInitial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(channel.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text("Language: ${channel.language} · Stream format: HLS (m3u8)", color = ZeMuted, fontSize = 11.sp)
                }
                if (channel.isHd) {
                    Box(
                        modifier = Modifier
                            .background(ZeSportAmber.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("HD 1080P", color = ZeSportAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Chat Feed Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
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
                        Text("CHANNEL CHAT", color = ZeSportText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Active viewers: 42,912", color = ZeSportMuted, fontSize = 10.sp)
                }

                // Chat Messages List
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

                // Message Text Field Editor Bar
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
                            .testTag("channel_chat_input_text_field"),
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
}
val ZeMuted = Color(0xFF98A2B3)
