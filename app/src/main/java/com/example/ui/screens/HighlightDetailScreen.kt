package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Highlight
import com.example.ui.ZeSportViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightDetailScreen(
    highlightId: String,
    viewModel: ZeSportViewModel,
    onBack: () -> Unit,
    onNavigateToHighlight: (String) -> Unit
) {
    val highlight = remember(highlightId) { viewModel.getHighlightById(highlightId) }
    val highlights by viewModel.highlights.collectAsStateWithLifecycle()

    if (highlight == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ZeSportBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Highlight not found", color = ZeSportText, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = ZeSportAccent)) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Highlight recap",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZeSportSurface,
                    titleContentColor = ZeSportText
                )
            )
        },
        containerColor = ZeSportBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ZeSportBg)
                .testTag("highlight_detail_content"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player Block
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = highlight.thumbnailUrl,
                        contentDescription = highlight.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(ZeSportAccent, RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            // Title & Info
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = highlight.subtitle,
                        color = ZeSportAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = highlight.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Published on ${highlight.date} · 1080p stream recap",
                        color = ZeSportMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Match highlights info card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ZeSportSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZeSportLine),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Match recap description",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        HorizontalDivider(color = ZeSportLine)
                        Text(
                            text = "Enjoy the full video compilation containing goals, match-saving saves, red/yellow card controversies, and emotional post-match interviews from managers and players.",
                            color = ZeSportText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Recommended Highlights heading
            item {
                Text(
                    text = "Recommended recaps",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // List of other highlights
            val related = highlights.filter { it.id != highlightId }.take(3)
            items(related) { relHighlight ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ZeSportLine, RoundedCornerShape(8.dp))
                        .background(ZeSportSurface)
                        .clickable { onNavigateToHighlight(relHighlight.id) }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = relHighlight.thumbnailUrl,
                            contentDescription = relHighlight.title,
                            modifier = Modifier
                                .size(80.dp, 45.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = relHighlight.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = relHighlight.subtitle,
                                color = ZeMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
