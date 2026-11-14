package com.smartlightscontrol.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
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
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ErrorContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = errorMessage ?: "Error banner"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
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

        // Close Button with 48x48 dp touch target
        Box(
            modifier = Modifier
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                .clip(RoundedCornerShape(50))
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
                tint = OnErrorContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview
@Composable
fun ErrorToastPreview() {
    SmartLightsControlTheme {
        ErrorToast(errorMessage = "Something went wrong · Try again later")
    }
}
