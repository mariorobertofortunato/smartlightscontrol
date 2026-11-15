package com.smartlightscontrol.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Blinds
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smartlightscontrol.ui.theme.SurfaceCard
import com.smartlightscontrol.ui.theme.TextOnWidget
import com.smartlightscontrol.ui.theme.TextPrimary
import com.smartlightscontrol.ui.theme.WidgetIconOff

@Composable
fun PlaceholderGridCard(
    title: String,
    statusText: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 10.dp // Inner card radius nesting inside 16.dp container

    Box(
        modifier = modifier
            .height(107.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(SurfaceCard)
            .border(1.dp, Color(0xFF2A3237), RoundedCornerShape(cornerRadius))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp)
        ) {
            // Top-Left: Icon with left padding
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = WidgetIconOff,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp)
            )

            // Center-Right: Chevron touch target Box flush against right edge with centered icon
            Box(
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextOnWidget,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Bottom-Left: Title & Status with left padding
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnWidget,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun EntranceWindowCard(modifier: Modifier = Modifier) {
    PlaceholderGridCard(
        title = "Entrance window",
        statusText = "Closed",
        icon = Icons.Default.Window,
        modifier = modifier
    )
}

@Composable
fun CloseAllBlindsCard(modifier: Modifier = Modifier) {
    PlaceholderGridCard(
        title = "Close all blinds",
        statusText = "5 active",
        icon = Icons.Default.Blinds,
        modifier = modifier
    )
}

@Composable
fun NightTimeCard(modifier: Modifier = Modifier) {
    PlaceholderGridCard(
        title = "Night time",
        statusText = "Scene",
        icon = Icons.Default.NightsStay,
        modifier = modifier
    )
}
