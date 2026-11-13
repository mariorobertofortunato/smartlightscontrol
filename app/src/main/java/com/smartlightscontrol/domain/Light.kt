package com.smartlightscontrol.domain

data class Light(
    val id: String,
    val name: String,
    val dimmable: Boolean,
    val isOn: Boolean,
    val brightness: Int,
    val lastBrightness: Int
)
