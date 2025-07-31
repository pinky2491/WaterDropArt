package com.rvp.waterdropart.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

data class AnimationState(
    val isPlaying: Boolean = false,
    val isImageMode: Boolean = false,
    val dropSpeed: Float = 1f,
    val particleDensity: Float = 0.5f,
    val morphTransitionTime: Float = 2f,
    val selectedImageUri: String? = null,
    val imagePixels: List<ImagePixel> = emptyList(),
    val morphProgress: Float = 0f,
    val isMorphing: Boolean = false
)

data class ImagePixel(
    val position: Offset,
    val color: Color,
    val brightness: Float
) 