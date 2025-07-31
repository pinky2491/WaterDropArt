package com.rvp.waterdropart.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.rvp.waterdropart.ui.components.WaterDropViewComposable
import com.rvp.waterdropart.ui.components.MorphingStyle
import com.rvp.waterdropart.ui.components.FilterType
import com.rvp.waterdropart.ui.components.MorphingConfig
import com.rvp.waterdropart.ui.components.ImageProcessingCallback
import com.rvp.waterdropart.ui.components.SpecialEffect
import com.rvp.waterdropart.service.MediaRecorderService
import com.rvp.waterdropart.service.ShareService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var waterDropView by remember { mutableStateOf<com.rvp.waterdropart.ui.components.WaterDropView?>(null) }
    var currentStyle by remember { mutableStateOf(MorphingStyle.SMOOTH) }
    var currentFilter by remember { mutableStateOf(FilterType.NONE) }
    var currentSpecialEffect by remember { mutableStateOf(SpecialEffect.NONE) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingMessage by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingMessage by remember { mutableStateOf("") }
    var reverseMorphMessage by remember { mutableStateOf("") }
    
    // Services
    val mediaRecorderService = remember { MediaRecorderService(context) }
    val shareService = remember { ShareService(context) }
    
    // Image processing callback
    val processingCallback = remember {
        object : ImageProcessingCallback {
            override fun onProcessingStarted() {
                isProcessing = true
                processingMessage = "Processing image..."
            }
            
            override fun onProcessingCompleted(bitmap: android.graphics.Bitmap, config: MorphingConfig) {
                isProcessing = false
                processingMessage = ""
            }
            
            override fun onProcessingError(error: String) {
                isProcessing = false
                processingMessage = "Error: $error"
            }
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        println("DEBUG: Image picker result received: $uri")
        uri?.let { 
            println("DEBUG: Calling waterDropView.morphToImage")
            waterDropView?.let { view ->
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    val config = MorphingConfig(
                        style = currentStyle, 
                        filter = currentFilter,
                        specialEffect = currentSpecialEffect
                    )
                    view.morphToImage(bitmap, config)
                } catch (e: Exception) {
                    e.printStackTrace()
                    processingMessage = "Error loading image: ${e.message}"
                }
            }
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Custom water drop view
        WaterDropViewComposable(
            modifier = Modifier.fillMaxSize(),
            onViewCreated = { view ->
                waterDropView = view
                view.setProcessingCallback(processingCallback)
                view.setSpecialEffect(currentSpecialEffect)
            }
        )
        
        // Minimal top status bar
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Text(
                text = "Water Drop Art",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Loading overlay
        if (isProcessing || isRecording) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isProcessing) processingMessage else recordingMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isProcessing) 
                                "Please wait while processing your image..." 
                            else 
                                "Please wait while recording your creation...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // Control Panel with Simple Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Style and Filter Info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Style: ${when (currentStyle) {
                            MorphingStyle.SMOOTH -> "Smooth"
                            MorphingStyle.PIXELATED -> "Pixelated"
                            MorphingStyle.EDGE_DETECTION -> "Edge Detection"
                            MorphingStyle.ARTISTIC -> "Artistic"
                        }}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Filter: ${when (currentFilter) {
                            FilterType.NONE -> "None"
                            FilterType.GRAYSCALE -> "Grayscale"
                            FilterType.SEPIA -> "Sepia"
                            FilterType.NEON -> "Neon"
                        }}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                                            Text(
                            text = "Effect: ${when (currentSpecialEffect) {
                                SpecialEffect.NONE -> "None"
                                SpecialEffect.FIREWORKS -> "Fireworks"
                                SpecialEffect.GALAXY -> "Galaxy"
                                SpecialEffect.UNDERWATER -> "Underwater"
                                SpecialEffect.STORM -> "Storm"
                            }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (reverseMorphMessage.isNotEmpty()) {
                            Text(
                                text = reverseMorphMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                }
            }
            
            // Control Buttons - Horizontally Scrollable
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                item {
                    // Style Cycle Button
                    FloatingActionButton(
                        onClick = {
                            currentStyle = when (currentStyle) {
                                MorphingStyle.SMOOTH -> MorphingStyle.PIXELATED
                                MorphingStyle.PIXELATED -> MorphingStyle.EDGE_DETECTION
                                MorphingStyle.EDGE_DETECTION -> MorphingStyle.ARTISTIC
                                MorphingStyle.ARTISTIC -> MorphingStyle.SMOOTH
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Text(
                            text = "Style",
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                item {
                    // Filter Cycle Button
                    FloatingActionButton(
                        onClick = {
                            currentFilter = when (currentFilter) {
                                FilterType.NONE -> FilterType.GRAYSCALE
                                FilterType.GRAYSCALE -> FilterType.SEPIA
                                FilterType.SEPIA -> FilterType.NEON
                                FilterType.NEON -> FilterType.NONE
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ) {
                        Text(
                            text = "Filter",
                            color = MaterialTheme.colorScheme.onTertiary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                item {
                    // Special Effect Cycle Button
                    FloatingActionButton(
                        onClick = {
                            currentSpecialEffect = when (currentSpecialEffect) {
                                SpecialEffect.NONE -> SpecialEffect.FIREWORKS
                                SpecialEffect.FIREWORKS -> SpecialEffect.GALAXY
                                SpecialEffect.GALAXY -> SpecialEffect.UNDERWATER
                                SpecialEffect.UNDERWATER -> SpecialEffect.STORM
                                SpecialEffect.STORM -> SpecialEffect.NONE
                            }
                            waterDropView?.setSpecialEffect(currentSpecialEffect)
                        },
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = "Effect",
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                item {
                    // Image selection button
                    FloatingActionButton(
                        onClick = {
                            println("DEBUG: + button pressed, launching image picker")
                            imagePickerLauncher.launch("image/*")
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Select Image",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                item {
                    // Reverse morph button
                    FloatingActionButton(
                        onClick = {
                            println("DEBUG: Reverse morph button pressed")
                            reverseMorphMessage = "Reversing morph..."
                            waterDropView?.let { view ->
                                println("DEBUG: Calling reverseMorph on waterDropView")
                                view.reverseMorph()
                                println("DEBUG: reverseMorph called successfully")
                                reverseMorphMessage = "Morph reversed!"
                                // Clear message after 2 seconds
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(2000)
                                    reverseMorphMessage = ""
                                }
                            } ?: run {
                                println("DEBUG: waterDropView is null")
                                reverseMorphMessage = "Error: View not ready"
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reverse Morph",
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                }
                
                item {
                    // Record video button
                    FloatingActionButton(
                        onClick = {
                            if (!isRecording) {
                                isRecording = true
                                recordingMessage = "Recording video..."
                                waterDropView?.let { view ->
                                    mediaRecorderService.startVideoRecording(view) { file ->
                                        isRecording = false
                                        if (file != null) {
                                            recordingMessage = "Video saved!"
                                            // Auto-share the video
                                            shareService.shareVideo(file)
                                        } else {
                                            recordingMessage = "Recording failed"
                                        }
                                    }
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VideoLibrary,
                            contentDescription = "Record Video",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                item {
                    // Create GIF button
                    FloatingActionButton(
                        onClick = {
                            if (!isRecording) {
                                isRecording = true
                                recordingMessage = "Creating GIF..."
                                waterDropView?.let { view ->
                                    mediaRecorderService.createGIF(view) { file ->
                                        isRecording = false
                                        if (file != null) {
                                            recordingMessage = "GIF created!"
                                            // Auto-share the GIF
                                            shareService.shareGIF(file)
                                        } else {
                                            recordingMessage = "GIF creation failed"
                                        }
                                    }
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Gif,
                            contentDescription = "Create GIF",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
} 