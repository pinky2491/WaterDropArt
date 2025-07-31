package com.rvp.waterdropart.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import com.rvp.waterdropart.data.model.WaterDrop
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import com.rvp.waterdropart.R

@Composable
fun WaterDropCanvas(
    waterDrops: List<WaterDrop>,
    onCanvasSizeChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(Offset.Zero) }
    var sparkleTime by remember { mutableStateOf(0f) }
    val dropPainter = painterResource(id = R.drawable.ic_water_drop)
    
    // Animation loop for sparkles
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            sparkleTime += 0.05f
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                canvasSize = Offset(size.width.toFloat(), size.height.toFloat())
                onCanvasSizeChanged(size.width.toFloat(), size.height.toFloat())
            }
    ) {
        val width = size.width
        val height = size.height
        
        // Always draw black background
        drawRect(Color.Black)
        
        // Draw water drops as simple circles for debugging
        waterDrops.forEach { drop ->
            drawSimpleDrop(drop)
        }
        
        // Debug: Draw simple circles to test if canvas is working
        drawCircle(Color.Red, radius = 30f, center = Offset(100f, 100f))
        drawCircle(Color.Green, radius = 25f, center = Offset(200f, 200f))
        drawCircle(Color.Blue, radius = 20f, center = Offset(300f, 300f))
        
        // Debug: Log drop count during morphing
        if (waterDrops.any { it.isMorphing }) {
            println("DEBUG: Drawing ${waterDrops.size} drops, ${waterDrops.count { it.isMorphing }} are morphing")
        }
        
        // Draw sparkles
        drawSparkles(width, height, sparkleTime)
    }
}

private fun DrawScope.drawSimpleDrop(drop: WaterDrop) {
    val position = drop.position
    val size = drop.size
    val clampedAlpha = drop.alpha.coerceIn(0f, 1f)
    val actualPosition = position
    
    // Enhanced visual effects for water-like appearance
    if (drop.isMorphing) {
        // Add glow effect for morphing drops
        drawCircle(
            color = drop.color.copy(alpha = clampedAlpha * 0.3f),
            radius = size * 1.5f,
            center = actualPosition
        )
    }
    
    drawCircle(
        color = drop.color.copy(alpha = clampedAlpha), // Use drop's actual color
        radius = size,
        center = actualPosition
    )
    
    // Add highlight for water-like shine
    if (drop.isMorphing) {
        drawCircle(
            color = Color.White.copy(alpha = clampedAlpha * 0.6f),
            radius = size * 0.3f,
            center = actualPosition + Offset(-size * 0.2f, -size * 0.2f)
        )
    }
}

private fun DrawScope.drawSparkles(width: Float, height: Float, time: Float) {
    val sparkleCount = 10
    val sparkleSize = 1.5f
    
    repeat(sparkleCount) { index ->
        val x = (index * 37.5f + time * 30f) % width
        val y = (index * 23.4f + time * 20f) % height
        val alpha = (sin(time * 2f + index) * 0.3f + 0.2f).coerceIn(0f, 1f)
        
        val sparkleColor = Color(
            red = 0.8f + 0.2f * sin(time + index),
            green = 0.9f + 0.1f * cos(time + index),
            blue = 1f,
            alpha = alpha * 0.4f
        )
        
        drawCircle(
            color = sparkleColor,
            radius = sparkleSize,
            center = Offset(x, y)
        )
    }
} 