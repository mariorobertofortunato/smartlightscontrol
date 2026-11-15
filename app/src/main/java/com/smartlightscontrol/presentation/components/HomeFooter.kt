package com.smartlightscontrol.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smartlightscontrol.R
import com.smartlightscontrol.ui.theme.SurfaceContainer
import com.smartlightscontrol.ui.theme.SurfaceImageContainer
import com.smartlightscontrol.ui.theme.SurfaceInput
import com.smartlightscontrol.ui.theme.TextPrimary
import com.smartlightscontrol.ui.theme.TextTertiary

@Composable
fun ExternalCarouselCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmapHome = remember(context) {
        BitmapFactory.decodeResource(context.resources, R.raw.external_home)
    }
    val bitmapGarden = remember(context) {
        BitmapFactory.decodeResource(context.resources, R.raw.external_garden)
    }

    // Carousel container box matching 2x2 grid container style (16.dp radius, surfaceContainer background, 1dp border, padding)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainer)
            .border(1.dp, Color(0xFF2A3237), RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 24.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .width(310.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceImageContainer)
                        .border(1.dp, Color(0xFF2A3237), RoundedCornerShape(10.dp))
                ) {
                    if (bitmapHome != null) {
                        Image(
                            bitmap = bitmapHome.asImageBitmap(),
                            contentDescription = "External home view",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = "External",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .width(310.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceImageContainer)
                        .border(1.dp, Color(0xFF2A3237), RoundedCornerShape(10.dp))
                ) {
                    if (bitmapGarden != null) {
                        Image(
                            bitmap = bitmapGarden.asImageBitmap(),
                            contentDescription = "Garden view",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = "Garden",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AssistantInputBar(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Message Input Placeholder with 1dp border outline
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(50))
                .background(SurfaceInput)
                .border(1.dp, Color(0xFF384046), RoundedCornerShape(50))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Message Qe'ra…",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Mic Button with 48x48 dp touch target
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(50))
                .background(TextPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice assistant",
                tint = SurfaceInput,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
