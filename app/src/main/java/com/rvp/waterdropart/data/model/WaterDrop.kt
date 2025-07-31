package com.rvp.waterdropart.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.random.Random
import kotlin.math.sin
import kotlin.math.cos

data class WaterDrop(
    val id: Int,
    var position: Offset,
    var velocity: Offset = Offset.Zero,
    var size: Float = 6f,
    var color: Color = Color.White,
    var alpha: Float = 1f,
    var life: Float = 0f,
    var targetPosition: Offset? = null,
    var isMorphing: Boolean = false,
    var morphProgress: Float = 0f,
    var originalPosition: Offset? = null,
    var drift: Float = 0f,
    var trailPositions: MutableList<Offset> = mutableListOf()
) {
    companion object {
        private var nextId = 0
        
        fun createRandom(width: Float, height: Float): WaterDrop {
            return WaterDrop(
                id = nextId++,
                position = Offset(
                    Random.nextFloat() * width,
                    Random.nextFloat() * height // Start across full height
                ),
                size = Random.nextFloat() * 6f + 4f,
                color = Color.White // White drops
            )
        }
        
        fun createForImage(
            id: Int,
            position: Offset,
            targetPosition: Offset,
            color: Color
        ): WaterDrop {
            return WaterDrop(
                id = id,
                position = position,
                targetPosition = targetPosition,
                color = color,
                size = 8f, // Larger size for morphed drops
                isMorphing = true,
                originalPosition = position
            )
        }
    }
    
    fun update(deltaTime: Float, gravity: Float = 300f, width: Float, height: Float) {
        if (isMorphing && targetPosition != null) {
            // Enhanced fluid morphing - particles dynamically arrange into specific shapes
            val distance = (targetPosition!! - position).getDistance()
            
            if (distance > 10f) {
                // Smooth flow towards target with enhanced water-like motion
                val direction = (targetPosition!! - position) / distance
                val flowSpeed = 150f + distance * 0.8f // More dynamic speed variation
                
                // Add natural water turbulence
                val turbulence = Offset(
                    sin(System.currentTimeMillis() * 0.002f + id * 0.1f) * 15f,
                    cos(System.currentTimeMillis() * 0.0015f + id * 0.1f) * 10f
                )
                
                velocity = direction * flowSpeed + turbulence * 0.1f
                position += velocity * deltaTime
                
                // Enhanced drift for more natural water movement
                velocity += Offset(drift, 0f) * deltaTime * 30f
            } else if (distance > 2f) {
                // Fine-tuning phase - gentle approach to final position
                val direction = (targetPosition!! - position) / distance
                val fineSpeed = 50f + distance * 2f
                
                velocity = direction * fineSpeed
                position += velocity * deltaTime
                velocity *= 0.95f // Gradual damping
            } else {
                // Settle into final position with gentle oscillation
                velocity *= 0.8f // Stronger damping
                position += velocity * deltaTime
                
                // Gentle oscillation around target - like water droplets settling
                val oscillation = Offset(
                    sin(System.currentTimeMillis() * 0.003f + id * 0.5f) * 1.5f,
                    cos(System.currentTimeMillis() * 0.002f + id * 0.5f) * 1.5f
                )
                position = targetPosition!! + oscillation
            }
            
            // Update morph progress for visual effects - more dynamic
            morphProgress = 1f - (distance / 150f).coerceIn(0f, 1f)
            
        } else {
            // Normal falling animation
            velocity += Offset(0f, gravity * deltaTime)
            velocity += Offset(drift, 0f) * deltaTime * 10f
            
            position += velocity * deltaTime
            
            // Reset when drop goes off screen
            if (position.y > height + 100f) {
                reset(width, height)
            }
            
            // Keep alpha at 1 for visible drops
            alpha = 1f
        }
    }
    
    fun isDead(): Boolean = false // Never die, just reset
    
    fun reset(width: Float, height: Float) {
        position = Offset(
            Random.nextFloat() * width,
            Random.nextFloat() * height // Start across full height
        )
        velocity = Offset(0f, 0f)
        life = 1f
        alpha = 1f
        isMorphing = false
        morphProgress = 0f
        drift = (Random.nextFloat() * 2f - 1f) * 0.3f
    }
} 