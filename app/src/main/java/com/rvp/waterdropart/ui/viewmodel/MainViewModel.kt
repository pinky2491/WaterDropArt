package com.rvp.waterdropart.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvp.waterdropart.data.model.AnimationState
import com.rvp.waterdropart.data.model.ImagePixel
import com.rvp.waterdropart.data.model.WaterDrop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.random.Random

class MainViewModel : ViewModel() {
    
    private val _animationState = MutableStateFlow(AnimationState(isPlaying = true))
    val animationState: StateFlow<AnimationState> = _animationState.asStateFlow()
    
    private val _waterDrops = MutableStateFlow<List<WaterDrop>>(emptyList())
    val waterDrops: StateFlow<List<WaterDrop>> = _waterDrops.asStateFlow()
    
    private val _canvasSize = MutableStateFlow(Offset(0f, 0f))
    val canvasSize: StateFlow<Offset> = _canvasSize.asStateFlow()
    
    // New state for continuous animation
    private var lastDropSpawnTime = 0L
    private val spawnInterval = 15L // Spawn new drop every 15ms for much better coverage
    private var morphSequenceActive = false
    
    fun updateCanvasSize(width: Float, height: Float) {
        _canvasSize.value = Offset(width, height)
        // Always generate initial drops when canvas size is set
        generateInitialDrops(width, height)
    }
    
    fun togglePlayPause() {
        _animationState.value = _animationState.value.copy(
            isPlaying = !_animationState.value.isPlaying
        )
    }
    
    fun handleVolumeButtonPress() {
        // Pause animation and trigger image selection
        _animationState.value = _animationState.value.copy(isPlaying = false)
        // This will be handled by the UI to open gallery
    }
    
    fun toggleImageMode() {
        val newState = _animationState.value.copy(
            isImageMode = !_animationState.value.isImageMode
        )
        _animationState.value = newState
        
        if (newState.isImageMode && newState.imagePixels.isNotEmpty()) {
            morphToImage()
        } else {
            resetToFreeFall()
        }
    }
    
    fun updateDropSpeed(speed: Float) {
        _animationState.value = _animationState.value.copy(dropSpeed = speed)
    }
    
    fun updateParticleDensity(density: Float) {
        _animationState.value = _animationState.value.copy(particleDensity = density)
        regenerateDrops()
    }
    
    fun updateMorphTransitionTime(time: Float) {
        _animationState.value = _animationState.value.copy(morphTransitionTime = time)
    }
    
    fun refreshDrops() {
        val size = _canvasSize.value
        if (size.x > 0f && size.y > 0f) {
            regenerateDrops()
        }
    }
    
    fun selectImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                println("DEBUG: Image selection started")
                val bitmap = loadBitmapFromUri(context, uri)
                println("DEBUG: Bitmap loaded, size: ${bitmap.width}x${bitmap.height}")
                val pixels = processImageToPixels(bitmap)
                println("DEBUG: Processed ${pixels.size} pixels")
                
                _animationState.value = _animationState.value.copy(
                    selectedImageUri = uri.toString(),
                    imagePixels = pixels
                )
                
                println("DEBUG: Starting morph sequence")
                // Start the morph sequence
                startMorphSequence()
                
            } catch (e: Exception) {
                // Handle error
                println("Error loading image: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    suspend fun startMorphSequence() {
        if (morphSequenceActive) return
        println("DEBUG: Morph sequence started")
        morphSequenceActive = true
        
        // Continue rain for 2 seconds
        println("DEBUG: Continuing rain for 2 seconds")
        delay(2000)
        
        // Morph to image
        println("DEBUG: Morphing to image")
        morphToImage()
        
        // Display morphed image for 8 seconds (longer display)
        println("DEBUG: Displaying morphed image for 8 seconds")
        delay(8000)
        
        // Return to continuous rain
        println("DEBUG: Returning to rain")
        resetToFreeFall()
        morphSequenceActive = false
        println("DEBUG: Morph sequence completed")
    }
    
    fun updateAnimation(deltaTime: Float) {
        val size = _canvasSize.value
        if (size.x <= 0f || size.y <= 0f) return
        
        val currentTime = System.currentTimeMillis()
        
        // Only spawn new drops if not morphing
        if (!morphSequenceActive && currentTime - lastDropSpawnTime > spawnInterval) {
            spawnNewDrop(size.x, size.y)
            lastDropSpawnTime = currentTime
        }
        
        val updatedDrops = _waterDrops.value.map { drop ->
            drop.update(
                deltaTime = deltaTime * _animationState.value.dropSpeed,
                width = size.x,
                height = size.y
            )
            drop
        }
        
        // Only maintain drop count if not morphing
        if (!morphSequenceActive) {
            val targetDropCount = (500 * _animationState.value.particleDensity).toInt()
            if (updatedDrops.size < targetDropCount) {
                val newDrops = (targetDropCount - updatedDrops.size).let { count ->
                    List(count) { WaterDrop.createRandom(size.x, size.y) }
                }
                _waterDrops.value = updatedDrops + newDrops
            } else {
                _waterDrops.value = updatedDrops
            }
        } else {
            // During morphing, just update the existing drops
            _waterDrops.value = updatedDrops
            // Debug: Log morphing drops
            val morphingDrops = updatedDrops.filter { it.isMorphing }
            if (morphingDrops.isNotEmpty()) {
                println("DEBUG: Updating ${morphingDrops.size} morphing drops")
                val firstDrop = morphingDrops.first()
                println("DEBUG: First morphing drop position: (${firstDrop.position.x}, ${firstDrop.position.y})")
            }
        }
        
        // Ensure we always have some drops when not morphing
        if (_waterDrops.value.isEmpty() && !morphSequenceActive) {
            generateInitialDrops(size.x, size.y)
        }
    }
    
    private fun spawnNewDrop(width: Float, height: Float) {
        val newDrop = WaterDrop.createRandom(width, height)
        _waterDrops.value = _waterDrops.value + newDrop
    }
    
    private fun generateInitialDrops(width: Float, height: Float) {
        val dropCount = (400 * _animationState.value.particleDensity).toInt() // Much more drops for full screen
        val drops = List(dropCount) { WaterDrop.createRandom(width, height) }
        
        _waterDrops.value = drops
        lastDropSpawnTime = System.currentTimeMillis()
    }
    
    private fun regenerateDrops() {
        val size = _canvasSize.value
        if (size.x > 0f && size.y > 0f) {
            generateInitialDrops(size.x, size.y)
        }
    }
    
    private fun morphToImage() {
        val pixels = _animationState.value.imagePixels
        val size = _canvasSize.value
        
        println("DEBUG: morphToImage called, pixels: ${pixels.size}, canvas: ${size.x}x${size.y}")
        
        if (pixels.isEmpty() || size.x <= 0f || size.y <= 0f) {
            println("DEBUG: morphToImage failed - pixels empty or canvas size invalid")
            return
        }
        
        // Calculate center position for 200x200 image
        val imageSize = 200f
        val centerX = (size.x - imageSize) / 2f
        val centerY = (size.y - imageSize) / 2f
        
        println("DEBUG: Creating test drops for morphing")
        
        // Create simple test drops that are definitely visible
        val testDrops = listOf(
            WaterDrop(
                id = 9999,
                position = Offset(100f, 100f),
                velocity = Offset.Zero,
                size = 50f, // Much larger size
                color = Color.Red,
                alpha = 1f,
                life = 1f,
                targetPosition = Offset(200f, 200f),
                isMorphing = true,
                morphProgress = 0f,
                originalPosition = Offset(100f, 100f),
                drift = 0f
            ),
            WaterDrop(
                id = 9998,
                position = Offset(300f, 300f),
                velocity = Offset.Zero,
                size = 50f, // Much larger size
                color = Color.Green,
                alpha = 1f,
                life = 1f,
                targetPosition = Offset(400f, 400f),
                isMorphing = true,
                morphProgress = 0f,
                originalPosition = Offset(300f, 300f),
                drift = 0f
            ),
            WaterDrop(
                id = 9997,
                position = Offset(500f, 500f),
                velocity = Offset.Zero,
                size = 50f, // Much larger size
                color = Color.Blue,
                alpha = 1f,
                life = 1f,
                targetPosition = Offset(600f, 600f),
                isMorphing = true,
                morphProgress = 0f,
                originalPosition = Offset(500f, 500f),
                drift = 0f
            )
        )
        
        // Clear out all regular drops and only show test drops
        _waterDrops.value = testDrops
        _animationState.value = _animationState.value.copy(isMorphing = true)
        println("DEBUG: Test morphing started, drops: ${testDrops.size}")
    }
    
    private fun resetToFreeFall() {
        val size = _canvasSize.value
        if (size.x > 0f && size.y > 0f) {
            generateInitialDrops(size.x, size.y)
        }
        _animationState.value = _animationState.value.copy(isMorphing = false)
    }
    
    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }
    
    private fun processImageToPixels(bitmap: Bitmap): List<ImagePixel> {
        val maxPixels = 3000 // More pixels for better shape preservation
        val pixels = mutableListOf<ImagePixel>()
        
        val width = bitmap.width
        val height = bitmap.height
        val stepX = kotlin.math.max(1, width / 150) // Sample every 150th pixel horizontally
        val stepY = kotlin.math.max(1, height / 150) // Sample every 150th pixel vertically
        
        for (y in 0 until height step stepY) {
            for (x in 0 until width step stepX) {
                val pixel = bitmap.getPixel(x, y)
                val r = android.graphics.Color.red(pixel)
                val g = android.graphics.Color.green(pixel)
                val b = android.graphics.Color.blue(pixel)
                val brightness = (r + g + b) / 765f // Normalized brightness
                
                // Only add pixels with sufficient brightness to be visible
                // Lower threshold for better shape preservation
                if (brightness > 0.05f) {
                    pixels.add(
                        ImagePixel(
                            position = Offset(
                                x.toFloat() / width,
                                y.toFloat() / height
                            ),
                            color = Color(r, g, b),
                            brightness = brightness
                        )
                    )
                }
            }
        }
        
        // Limit to maxPixels for performance, but preserve more pixels
        return if (pixels.size > maxPixels) {
            pixels.shuffled().take(maxPixels)
        } else {
            pixels
        }
    }
} 