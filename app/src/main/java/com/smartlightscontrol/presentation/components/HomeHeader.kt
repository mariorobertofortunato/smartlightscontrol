package com.smartlightscontrol.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.smartlightscontrol.R
import com.smartlightscontrol.presentation.HomeScreenEvent
import com.smartlightscontrol.presentation.LocalHomeScreenEventTunnel
import com.smartlightscontrol.ui.theme.ErrorContainer
import com.smartlightscontrol.ui.theme.OnErrorContainer
import com.smartlightscontrol.ui.theme.SurfacePill
import com.smartlightscontrol.ui.theme.TextPrimary
import com.smartlightscontrol.ui.theme.TextSecondary

@Composable
fun HomeHeader(
    failureRatePercent: Int,
    modifier: Modifier = Modifier
) {
    val eventTunnel = LocalHomeScreenEventTunnel.current
    val context = LocalContext.current
    val houseBitmap = remember(context) {
        BitmapFactory.decodeResource(context.resources, R.raw.home_lights_selector_image)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: House Icon + "At home"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "At home",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Right: Failure Rate Cycle Pill, "Pair buttons" pill & 3 dots menu
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Failure Rate Cycle Pill (0%, 10%, 50%, 100%)
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (failureRatePercent > 0) ErrorContainer else SurfacePill)
                        .clickable {
                            eventTunnel(HomeScreenEvent.CycleFailureRate)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .semantics {
                            contentDescription = "Failure rate set to $failureRatePercent percent. Tap to cycle."
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Fail: $failureRatePercent%",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnErrorContainer
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SurfacePill)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pair buttons",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Centered House Image Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            contentAlignment = Alignment.Center
        ) {
            if (houseBitmap != null) {
                Image(
                    bitmap = houseBitmap.asImageBitmap(),
                    contentDescription = "House overview",
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(100.dp)
                )
            }
        }
    }
}
