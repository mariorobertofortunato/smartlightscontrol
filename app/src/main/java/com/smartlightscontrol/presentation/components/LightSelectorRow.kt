package com.smartlightscontrol.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartlightscontrol.domain.Light
import com.smartlightscontrol.presentation.HomeScreenEvent
import com.smartlightscontrol.presentation.LocalHomeScreenEventTunnel
import com.smartlightscontrol.ui.theme.SmartLightsControlTheme
import com.smartlightscontrol.ui.theme.SurfaceCard
import com.smartlightscontrol.ui.theme.TextPrimary
import com.smartlightscontrol.ui.theme.TextSecondary

@Composable
fun LightSelectorRow(
    lights: List<Light>,
    selectedLightId: String,
    modifier: Modifier = Modifier
) {
    val eventTunnel = LocalHomeScreenEventTunnel.current
    var expanded by remember { mutableStateOf(false) }

    val selectedLight = lights.firstOrNull { it.id == selectedLightId } ?: lights.firstOrNull()

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCard)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .semantics {
                    role = Role.DropdownList
                    contentDescription = "Select light to control, currently ${selectedLight?.name ?: "None"}"
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Control:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedLight?.name ?: "Select light",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = TextSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(SurfaceCard)
                .padding(vertical = 4.dp)
        ) {
            lights.forEach { light ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = light.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (light.id == selectedLightId) MaterialTheme.colorScheme.primary else TextPrimary
                        )
                    },
                    onClick = {
                        expanded = false
                        eventTunnel(HomeScreenEvent.SelectLight(light.id))
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun LightSelectorRowPreview() {
    SmartLightsControlTheme {
        CompositionLocalProvider(LocalHomeScreenEventTunnel provides {}) {
            LightSelectorRow(
                lights = listOf(
                    Light("bed_light", "Bed light", true, true, 75, 75),
                    Light("group_all", "All the lights in the house", false, true, 100, 100),
                    Light("entrance_light", "Entrance light", false, false, 0, 100)
                ),
                selectedLightId = "bed_light",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
