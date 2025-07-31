package com.rvp.waterdropart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rvp.waterdropart.data.model.AnimationState

@Composable
fun ControlPanel(
    animationState: AnimationState,
    onPlayPause: () -> Unit,
    onToggleImageMode: () -> Unit,
    onDropSpeedChange: (Float) -> Unit,
    onParticleDensityChange: (Float) -> Unit,
    onMorphTransitionTimeChange: (Float) -> Unit,
    onRefresh: () -> Unit,
    onSelectImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = "Water Drop Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Main control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Play/Pause button
                FloatingActionButton(
                    onClick = onPlayPause,
                    containerColor = if (animationState.isPlaying) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (animationState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (animationState.isPlaying) "Pause" else "Play"
                    )
                }
                
                // Image mode toggle (using PlayArrow/Pause as placeholder)
                FloatingActionButton(
                    onClick = onToggleImageMode,
                    containerColor = if (animationState.isImageMode) 
                        MaterialTheme.colorScheme.secondary 
                    else 
                        MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(
                        imageVector = if (animationState.isImageMode) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (animationState.isImageMode) "Image Mode" else "Free Fall Mode"
                    )
                }
                
                // Refresh button
                FloatingActionButton(
                    onClick = onRefresh,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh"
                    )
                }
                
                // Select image button
                FloatingActionButton(
                    onClick = onSelectImage,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Select Image"
                    )
                }
            }
            
            // Sliders
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Drop Speed Slider
                Text(
                    text = "Drop Speed: ${String.format("%.1f", animationState.dropSpeed)}x",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = animationState.dropSpeed,
                    onValueChange = onDropSpeedChange,
                    valueRange = 0.1f..3f,
                    steps = 29
                )
                
                // Particle Density Slider
                Text(
                    text = "Particle Density: ${String.format("%.1f", animationState.particleDensity)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = animationState.particleDensity,
                    onValueChange = onParticleDensityChange,
                    valueRange = 0.1f..1f,
                    steps = 9
                )
                
                // Morph Transition Time Slider
                Text(
                    text = "Morph Time: ${String.format("%.1f", animationState.morphTransitionTime)}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = animationState.morphTransitionTime,
                    onValueChange = onMorphTransitionTimeChange,
                    valueRange = 0.5f..5f,
                    steps = 9
                )
            }
            
            // Status indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode indicator
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (animationState.isImageMode) 
                            MaterialTheme.colorScheme.secondary 
                        else 
                            MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(
                        text = if (animationState.isImageMode) "Image Mode" else "Free Fall",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                
                // Play status
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (animationState.isPlaying) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = if (animationState.isPlaying) "Playing" else "Paused",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (animationState.isPlaying) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
} 