package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ZeSportViewModel
import com.example.ui.theme.*

@Composable
fun HighlightsScreen(
    viewModel: ZeSportViewModel,
    onNavigateToHighlight: (String) -> Unit
) {
    val highlights by viewModel.highlights.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ZeSportBg)
            .testTag("highlights_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Headers
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Video Highlights",
                    color = ZeSportAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Match Recaps",
                    color = ZeSportText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // List of Highlights
        items(highlights, key = { it.id }) { highlight ->
            HighlightCardItem(
                highlight = highlight,
                onClick = { onNavigateToHighlight(highlight.id) }
            )
        }
    }
}
