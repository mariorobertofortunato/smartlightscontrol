package com.smartlightscontrol.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartlightscontrol.presentation.HomeScreenEvent
import com.smartlightscontrol.presentation.LocalHomeScreenEventTunnel
import com.smartlightscontrol.ui.theme.ErrorContainer
import com.smartlightscontrol.ui.theme.OnErrorContainer
import com.smartlightscontrol.ui.theme.SmartLightsControlTheme

@Composable
fun ErrorToast(
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val eventTunnel = LocalHomeScreenEventTunnel.current

    Row(
        modifier = modifier
            // Removed fillMaxWidth() so toast is a floating card with proper width per Figma screenshot
            .clip(RoundedCornerShape(16.dp))
            .background(ErrorContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = errorMessage ?: "Error banner"
            },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = OnErrorContainer
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Try again later",
                style = MaterialTheme.typography.bodySmall,
                color = OnErrorContainer.copy(alpha = 0.85f)
            )
        }

        // Close Button with minimum 48x48 dp touch target wrapping a solid white circular badge with dark red close icon
        Box(
            modifier = Modifier
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White) // Solid white circular badge per Figma screenshot
                    .clickable {
                        eventTunnel(HomeScreenEvent.DismissError)
                    }
                    .semantics {
                        contentDescription = "Dismiss error"
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = ErrorContainer, // Dark red icon inside white circle
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun ErrorToastPreview() {
    SmartLightsControlTheme {
        ErrorToast(errorMessage = "Something went wrong · Test")
    }
}
