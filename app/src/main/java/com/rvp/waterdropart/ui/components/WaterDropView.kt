package com.rvp.waterdropart.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.rvp.waterdropart.data.model.WaterDrop
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

interface ImageProcessingCallback {
    fun onProcessingStarted()
    fun onProcessingCompleted(bitmap: Bitmap, config: MorphingConfig)
    fun onProcessingError(error: String)
}

enum class MorphingStyle {
    SMOOTH, PIXELATED, ARTISTIC, EDGE_DETECTION
}

enum class FilterType {
    NONE, GRAYSCALE, SEPIA, NEON
}

enum class SpecialEffect {
    NONE, FIREWORKS, GALAXY, UNDERWATER, STORM
}

data class MorphingConfig(
    val style: MorphingStyle = MorphingStyle.SMOOTH,
    val filter: FilterType = FilterType.NONE,
    val transitionEffect: TransitionEffect = TransitionEffect.FADE,
    val specialEffect: SpecialEffect = SpecialEffect.NONE
)

enum class TransitionEffect {
    FADE, ZOOM, ROTATE, EXPLOSION
}

data class Ripple(
    val position: Offset,
    val maxRadius: Float,
    var currentRadius: Float = 0f,
    var alpha: Float = 1f,
    var life: Float = 0f
) {
    fun update(deltaTime: Float): Boolean {
        currentRadius += 50f * deltaTime
        alpha -= 0.5f * deltaTime
        life += deltaTime
        return alpha > 0f && life < 2f
    }
}

data class Explosion(
    val position: Offset,
    val particles: MutableList<ExplosionParticle> = mutableListOf(),
    var life: Float = 0f
) {
    fun update(deltaTime: Float): Boolean {
        life += deltaTime
        particles.forEach { it.update(deltaTime) }
        particles.removeAll { it.alpha <= 0f }
        return life < 3f && particles.isNotEmpty()
    }
}

data class ExplosionParticle(
    var position: Offset,
    var velocity: Offset,
    var color: androidx.compose.ui.graphics.Color,
    var size: Float,
    var alpha: Float = 1f
) {
    fun update(deltaTime: Float) {
        position += velocity * deltaTime
        velocity *= 0.98f // Friction
        alpha -= 0.5f * deltaTime
    }
}

data class FireworkParticle(
    var position: Offset,
    var velocity: Offset,
    var color: androidx.compose.ui.graphics.Color,
    var size: Float,
    var alpha: Float = 1f,
    var life: Float = 0f
) {
    fun update(deltaTime: Float): Boolean {
        position += velocity * deltaTime
        velocity *= 0.95f
        alpha -= 0.3f * deltaTime
        life += deltaTime
        return alpha > 0f && life < 4f
    }
}

data class GalaxyParticle(
    var position: Offset,
    var velocity: Offset,
    var color: androidx.compose.ui.graphics.Color,
    var size: Float,
    var alpha: Float = 1f,
    var rotation: Float = 0f
) {
    fun update(deltaTime: Float) {
        position += velocity * deltaTime
        rotation += 2f * deltaTime
        alpha = (sin(System.currentTimeMillis() * 0.001f) * 0.3f + 0.7f)
    }
}

data class Bubble(
    var position: Offset,
    var velocity: Offset,
    var size: Float,
    var alpha: Float = 1f
) {
    fun update(deltaTime: Float): Boolean {
        position += velocity * deltaTime
        velocity *= 0.99f
        alpha -= 0.1f * deltaTime
        return alpha > 0f && position.y > -50f
    }
}

data class Lightning(
    var start: Offset,
    var end: Offset,
    var alpha: Float = 1f,
    var life: Float = 0f
) {
    fun update(deltaTime: Float): Boolean {
        alpha -= 0.8f * deltaTime
        life += deltaTime
        return alpha > 0f && life < 0.5f
    }
}

class WaterDropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val glowPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }
    
    private val highlightPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = android.graphics.Color.WHITE
        alpha = 100
    }
    
    private val drops = mutableListOf<WaterDrop>()
    private val ripples = mutableListOf<Ripple>()
    private val explosions = mutableListOf<Explosion>()
    private val fireworkParticles = mutableListOf<FireworkParticle>()
    private val galaxyParticles = mutableListOf<GalaxyParticle>()
    private val bubbles = mutableListOf<Bubble>()
    private val lightningBolts = mutableListOf<Lightning>()
    private var animationJob: Job? = null
    private var isMorphing = false
    private var morphTarget: Bitmap? = null
    private var currentTransitionEffect: TransitionEffect = TransitionEffect.FADE
    private var transitionProgress: Float = 0f
    private var currentSpecialEffect: SpecialEffect = SpecialEffect.NONE
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw background based on special effect
        when (currentSpecialEffect) {
            SpecialEffect.GALAXY -> canvas.drawColor(android.graphics.Color.rgb(10, 10, 40))
            SpecialEffect.UNDERWATER -> canvas.drawColor(android.graphics.Color.rgb(0, 50, 100))
            SpecialEffect.STORM -> canvas.drawColor(android.graphics.Color.rgb(20, 20, 30))
            else -> canvas.drawColor(Color.Black.toArgb())
        }
        
        // Draw special effects
        when (currentSpecialEffect) {
            SpecialEffect.FIREWORKS -> {
                fireworkParticles.forEach { particle ->
                    drawFireworkParticle(canvas, particle)
                }
            }
            SpecialEffect.GALAXY -> {
                galaxyParticles.forEach { particle ->
                    drawGalaxyParticle(canvas, particle)
                }
            }
            SpecialEffect.UNDERWATER -> {
                bubbles.forEach { bubble ->
                    drawBubble(canvas, bubble)
                }
            }
            SpecialEffect.STORM -> {
                lightningBolts.forEach { lightning ->
                    drawLightning(canvas, lightning)
                }
            }
            else -> {
                // Draw explosions
                explosions.forEach { explosion ->
                    drawExplosion(canvas, explosion)
                }
            }
        }
        
        // Draw ripples
        ripples.forEach { ripple ->
            drawRipple(canvas, ripple)
        }
        
        // Draw all drops
        drops.forEach { drop ->
            drawDrop(canvas, drop)
        }
    }
    
    private fun drawDrop(canvas: Canvas, drop: WaterDrop) {
        val x = drop.position.x
        val y = drop.position.y
        val radius = drop.size
        
        // Only draw trail for morphing drops to save performance
        if (drop.isMorphing) {
            drop.trailPositions.forEachIndexed { index, trailPos ->
                val trailAlpha = (1f - index.toFloat() / drop.trailPositions.size) * 0.2f
                val trailSize = radius * (1f - index.toFloat() / drop.trailPositions.size * 0.5f)
                
                paint.color = drop.color.toArgb()
                paint.alpha = (trailAlpha * 255).toInt()
                canvas.drawCircle(trailPos.x, trailPos.y, trailSize, paint)
            }
        }
        
        // Simplified glow effect
        glowPaint.color = drop.color.toArgb()
        glowPaint.alpha = 30
        canvas.drawCircle(x, y, radius * 1.5f, glowPaint)
        
        // Draw main drop
        paint.color = drop.color.toArgb()
        paint.alpha = (drop.alpha * 255).toInt()
        canvas.drawCircle(x, y, radius, paint)
        
        // Simplified highlight
        highlightPaint.alpha = 80
        canvas.drawCircle(x - radius * 0.3f, y - radius * 0.3f, radius * 0.4f, highlightPaint)
        
        // Only add morphing glow for important drops
        if (drop.isMorphing && drop.morphProgress > 0.5f) {
            val morphGlow = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                color = android.graphics.Color.CYAN
                alpha = 40
                maskFilter = BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawCircle(x, y, radius * 1.8f, morphGlow)
        }
    }
    
    private fun drawRipple(canvas: Canvas, ripple: Ripple) {
        val ripplePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = android.graphics.Color.CYAN
            alpha = (ripple.alpha * 255).toInt()
        }
        
        canvas.drawCircle(ripple.position.x, ripple.position.y, ripple.currentRadius, ripplePaint)
    }
    
    private fun drawExplosion(canvas: Canvas, explosion: Explosion) {
        explosion.particles.forEach { particle ->
            val particlePaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                color = particle.color.toArgb()
                alpha = (particle.alpha * 255).toInt()
            }
            canvas.drawCircle(particle.position.x, particle.position.y, particle.size, particlePaint)
        }
    }
    
    private fun drawFireworkParticle(canvas: Canvas, particle: FireworkParticle) {
        val particlePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = particle.color.toArgb()
            alpha = (particle.alpha * 255).toInt()
        }
        canvas.drawCircle(particle.position.x, particle.position.y, particle.size, particlePaint)
        
        // Add sparkle effect
        val sparklePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
            alpha = (particle.alpha * 100).toInt()
        }
        canvas.drawCircle(particle.position.x, particle.position.y, particle.size * 0.3f, sparklePaint)
    }
    
    private fun drawGalaxyParticle(canvas: Canvas, particle: GalaxyParticle) {
        val particlePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = particle.color.toArgb()
            alpha = (particle.alpha * 255).toInt()
        }
        
        // Draw rotating star
        canvas.save()
        canvas.rotate(particle.rotation, particle.position.x, particle.position.y)
        canvas.drawCircle(particle.position.x, particle.position.y, particle.size, particlePaint)
        canvas.restore()
        
        // Add glow effect
        val glowPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = particle.color.toArgb()
            alpha = (particle.alpha * 50).toInt()
            maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(particle.position.x, particle.position.y, particle.size * 2f, glowPaint)
    }
    
    private fun drawBubble(canvas: Canvas, bubble: Bubble) {
        val bubblePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = android.graphics.Color.rgb(100, 200, 255)
            alpha = (bubble.alpha * 255).toInt()
        }
        
        // Draw bubble outline
        canvas.drawCircle(bubble.position.x, bubble.position.y, bubble.size, bubblePaint)
        
        // Draw bubble highlight
        val highlightPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
            alpha = (bubble.alpha * 100).toInt()
        }
        canvas.drawCircle(
            bubble.position.x - bubble.size * 0.3f,
            bubble.position.y - bubble.size * 0.3f,
            bubble.size * 0.2f,
            highlightPaint
        )
    }
    
    private fun drawLightning(canvas: Canvas, lightning: Lightning) {
        val lightningPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = android.graphics.Color.YELLOW
            alpha = (lightning.alpha * 255).toInt()
            maskFilter = BlurMaskFilter(2f, BlurMaskFilter.Blur.NORMAL)
        }
        
        // Draw lightning bolt
        val path = android.graphics.Path()
        path.moveTo(lightning.start.x, lightning.start.y)
        path.lineTo(lightning.end.x, lightning.end.y)
        canvas.drawPath(path, lightningPaint)
        
        // Add glow effect
        val glowPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 8f
            color = android.graphics.Color.WHITE
            alpha = (lightning.alpha * 100).toInt()
            maskFilter = BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(path, glowPaint)
    }
    
    fun startRainAnimation() {
        animationJob?.cancel()
        
        // Generate initial drops
        generateInitialDrops()
        
        animationJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                try {
                    updateDrops()
                    invalidate()
                    delay(16) // ~60 FPS
                } catch (e: Exception) {
                    // Handle any errors gracefully
                    println("Animation error: ${e.message}")
                    delay(100) // Wait a bit before retrying
                }
            }
        }
    }
    
    private fun generateInitialDrops() {
        drops.clear()
        val dropCount = 50 // Reduced for better performance
        
        repeat(dropCount) { i ->
            drops.add(
                WaterDrop(
                    id = i,
                    position = Offset(
                        Random.nextFloat() * width,
                        Random.nextFloat() * height
                    ),
                    size = Random.nextFloat() * 6f + 4f,
                    color = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    }
    
    private var processingCallback: ImageProcessingCallback? = null
    
    fun setProcessingCallback(callback: ImageProcessingCallback) {
        processingCallback = callback
    }
    
    fun setSpecialEffect(effect: SpecialEffect) {
        currentSpecialEffect = effect
        
        // Clear existing effects when switching
        when (effect) {
            SpecialEffect.FIREWORKS -> {
                fireworkParticles.clear()
                explosions.clear()
            }
            SpecialEffect.GALAXY -> {
                galaxyParticles.clear()
                explosions.clear()
            }
            SpecialEffect.UNDERWATER -> {
                bubbles.clear()
                explosions.clear()
            }
            SpecialEffect.STORM -> {
                lightningBolts.clear()
                explosions.clear()
            }
            else -> {
                fireworkParticles.clear()
                galaxyParticles.clear()
                bubbles.clear()
                lightningBolts.clear()
            }
        }
    }
    
    fun morphToImage(bitmap: Bitmap, config: MorphingConfig = MorphingConfig()) {
        // Start background processing
        processingCallback?.onProcessingStarted()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Process image in background
                val processedBitmap = when (config.style) {
                    MorphingStyle.EDGE_DETECTION -> detectEdges(bitmap)
                    MorphingStyle.PIXELATED -> pixelateImage(bitmap)
                    MorphingStyle.ARTISTIC -> applyArtisticEffect(bitmap)
                    else -> bitmap
                }
                
                // Apply filter in background
                val filteredBitmap = applyFilter(processedBitmap, config.filter)
                
                // Switch to main thread for UI updates
                withContext(Dispatchers.Main) {
                    applyMorphing(filteredBitmap, config)
                    processingCallback?.onProcessingCompleted(bitmap, config)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    processingCallback?.onProcessingError("Processing failed: ${e.message}")
                }
            }
        }
    }
    
    private fun applyMorphing(processedBitmap: Bitmap, config: MorphingConfig) {
        println("DEBUG: applyMorphing called, clearing old drops")
        
        // Stop any ongoing morphing immediately
        isMorphing = false
        
        // Create explosion effects for existing morphing drops before clearing
        val oldMorphingDrops = drops.filter { it.isMorphing }
        println("DEBUG: Found ${oldMorphingDrops.size} old morphing drops to explode")
        
        oldMorphingDrops.forEach { drop ->
            createExplosion(drop.position, drop.color)
        }
        
        // Clear all existing drops and effects immediately
        drops.clear()
        explosions.clear()
        ripples.clear()
        
        // Also clear all special effect particles
        fireworkParticles.clear()
        galaxyParticles.clear()
        bubbles.clear()
        lightningBolts.clear()
        
        println("DEBUG: Cleared all drops and effects, total drops: ${drops.size}")
        
        // Small delay to show explosion effects, then start new morphing
        CoroutineScope(Dispatchers.Main).launch {
            delay(300) // Longer pause to ensure explosions are visible
            
            println("DEBUG: Starting new morphing process")
            isMorphing = true
            morphTarget = processedBitmap
            
            // --- 1. Scale and sample pixels based on style ---
            val spacing = when (config.style) {
                MorphingStyle.PIXELATED -> 20
                MorphingStyle.EDGE_DETECTION -> 8
                else -> 12
            }
            
            // Limit maximum size for better performance
            val maxWidth = minOf((width / spacing / 2).toInt(), 100)
            val maxHeight = minOf((height / spacing / 2).toInt(), 100)
            val scaled = Bitmap.createScaledBitmap(processedBitmap, maxWidth, maxHeight, true)
            val shapePixels = mutableListOf<Triple<Float, Float, Int>>() // x, y, color
            
            for (x in 0 until scaled.width) {
                for (y in 0 until scaled.height) {
                    val pixel = scaled.getPixel(x, y)
                    if (shouldIncludePixel(pixel, config.style)) {
                        shapePixels.add(Triple(x.toFloat(), y.toFloat(), pixel))
                    }
                }
            }
            
            if (shapePixels.isEmpty()) {
                println("DEBUG: No shape pixels found, returning to rain mode")
                isMorphing = false
                return@launch
            }
            
            println("DEBUG: Found ${shapePixels.size} shape pixels to morph")
            
            // --- 2. Center the shape in the view ---
            val minX = shapePixels.minOf { it.first }
            val maxX = shapePixels.maxOf { it.first }
            val minY = shapePixels.minOf { it.second }
            val maxY = shapePixels.maxOf { it.second }
            val shapeWidth = maxX - minX
            val shapeHeight = maxY - minY
            val offsetX = (width - shapeWidth * spacing) / 2f - minX * spacing
            val offsetY = (height - shapeHeight * spacing) / 2f - minY * spacing
            
            // --- 3. Create new drops for morphing (don't reuse existing ones) ---
            val newDrops = mutableListOf<WaterDrop>()
            
            for (i in shapePixels.indices) {
                val (sx, sy, color) = shapePixels[i]
                val drop = WaterDrop(
                    id = i,
                    position = Offset(
                        (Math.random() * width).toFloat(),
                        (Math.random() * height).toFloat()
                    ),
                    targetPosition = Offset(sx * spacing + offsetX, sy * spacing + offsetY),
                    color = androidx.compose.ui.graphics.Color(color),
                    size = 8f,
                    isMorphing = true
                )
                newDrops.add(drop)
            }
            
            // Replace all drops with new morphing drops
            drops.clear()
            drops.addAll(newDrops)
            
            println("DEBUG: Created ${drops.size} new morphing drops")
        }
    }
    
    private fun updateDrops() {
        val deltaTime = 0.016f
        
        // Update special effects
        when (currentSpecialEffect) {
            SpecialEffect.FIREWORKS -> {
                fireworkParticles.removeAll { !it.update(deltaTime) }
                // Spawn new firework particles
                if (Random.nextFloat() < 0.01f && fireworkParticles.size < 20) {
                    createFireworkParticle()
                }
            }
            SpecialEffect.GALAXY -> {
                galaxyParticles.forEach { it.update(deltaTime) }
                // Spawn new galaxy particles
                if (Random.nextFloat() < 0.02f && galaxyParticles.size < 15) {
                    createGalaxyParticle()
                }
            }
            SpecialEffect.UNDERWATER -> {
                bubbles.removeAll { !it.update(deltaTime) }
                // Spawn new bubbles
                if (Random.nextFloat() < 0.05f && bubbles.size < 20) {
                    createBubble()
                }
            }
            SpecialEffect.STORM -> {
                lightningBolts.removeAll { !it.update(deltaTime) }
                // Spawn lightning
                if (Random.nextFloat() < 0.005f && lightningBolts.size < 3) {
                    createLightning()
                }
            }
            else -> {
                // Update explosions (limit to prevent lag)
                if (explosions.size < 5) {
                    explosions.removeAll { !it.update(deltaTime) }
                }
            }
        }
        
        // Update ripples (limit to prevent lag)
        if (ripples.size < 10) {
            ripples.removeAll { !it.update(deltaTime) }
        }
        
        // Spawn new drops less frequently
        if (!isMorphing && Random.nextFloat() < 0.02f && drops.size < 150) { // 2% chance, max 150 drops
            drops.add(
                WaterDrop(
                    id = drops.size,
                    position = Offset(
                        Random.nextFloat() * width,
                        -Random.nextFloat() * 50f - 20f
                    ),
                    size = Random.nextFloat() * 6f + 4f,
                    color = androidx.compose.ui.graphics.Color.White
                )
            )
        }
        
        // Process drops in batches for better performance
        val dropsToRemove = mutableListOf<WaterDrop>()
        
        drops.forEach { drop ->
            if (drop.isMorphing && drop.targetPosition != null) {
                // Optimized fluid morphing animation
                val distance = sqrt(
                    (drop.targetPosition!!.x - drop.position.x).pow(2) +
                    (drop.targetPosition!!.y - drop.position.y).pow(2)
                )
                
                if (distance > 5f) {
                    val directionX = (drop.targetPosition!!.x - drop.position.x) / distance
                    val directionY = (drop.targetPosition!!.y - drop.position.y) / distance
                    val speed = minOf(200f, distance * 2f) // Variable speed based on distance
                    
                    drop.position = Offset(
                        drop.position.x + directionX * speed * deltaTime,
                        drop.position.y + directionY * speed * deltaTime
                    )
                } else {
                    // Drop has reached target, add gentle oscillation
                    drop.position = Offset(
                        drop.targetPosition!!.x + sin(drop.life * 2f) * 2f,
                        drop.targetPosition!!.y + cos(drop.life * 2f) * 2f
                    )
                }
                
                drop.life += deltaTime
            } else if (!drop.isMorphing) {
                // Normal falling animation
                drop.velocity = Offset(
                    drop.velocity.x + drop.drift * deltaTime * 10f,
                    drop.velocity.y + 300f * deltaTime // Gravity
                )
                
                drop.position = Offset(
                    drop.position.x + drop.velocity.x * deltaTime,
                    drop.position.y + drop.velocity.y * deltaTime
                )
                
                // Reset drop when it goes off screen
                if (drop.position.y > height + 100f) {
                    drop.reset(width.toFloat(), height.toFloat())
                }
                
                drop.life += deltaTime
            }
            
            // Remove drops that are too old or have issues
            if (drop.life > 30f || drop.position.y < -100f) {
                dropsToRemove.add(drop)
            }
        }
        
        // Remove old drops
        drops.removeAll(dropsToRemove)
    }
    
    // Image processing methods
    private fun detectEdges(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        
        for (x in 1 until width - 1) {
            for (y in 1 until height - 1) {
                val center = bitmap.getPixel(x, y)
                val left = bitmap.getPixel(x - 1, y)
                val right = bitmap.getPixel(x + 1, y)
                val top = bitmap.getPixel(x, y - 1)
                val bottom = bitmap.getPixel(x, y + 1)
                
                val edgeStrength = calculateEdgeStrength(center, left, right, top, bottom)
                val edgeColor = if (edgeStrength > 50) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                result.setPixel(x, y, edgeColor)
            }
        }
        return result
    }
    
    private fun calculateEdgeStrength(center: Int, left: Int, right: Int, top: Int, bottom: Int): Int {
        val centerGray = (android.graphics.Color.red(center) + android.graphics.Color.green(center) + android.graphics.Color.blue(center)) / 3
        val leftGray = (android.graphics.Color.red(left) + android.graphics.Color.green(left) + android.graphics.Color.blue(left)) / 3
        val rightGray = (android.graphics.Color.red(right) + android.graphics.Color.green(right) + android.graphics.Color.blue(right)) / 3
        val topGray = (android.graphics.Color.red(top) + android.graphics.Color.green(top) + android.graphics.Color.blue(top)) / 3
        val bottomGray = (android.graphics.Color.red(bottom) + android.graphics.Color.green(bottom) + android.graphics.Color.blue(bottom)) / 3
        
        val horizontalGradient = abs(leftGray - rightGray)
        val verticalGradient = abs(topGray - bottomGray)
        return sqrt((horizontalGradient * horizontalGradient + verticalGradient * verticalGradient).toFloat()).toInt()
    }
    
    private fun pixelateImage(bitmap: Bitmap): Bitmap {
        val pixelSize = 8
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        
        for (x in 0 until width step pixelSize) {
            for (y in 0 until height step pixelSize) {
                val avgColor = getAverageColor(bitmap, x, y, pixelSize)
                for (dx in 0 until pixelSize) {
                    for (dy in 0 until pixelSize) {
                        if (x + dx < width && y + dy < height) {
                            result.setPixel(x + dx, y + dy, avgColor)
                        }
                    }
                }
            }
        }
        return result
    }
    
    private fun getAverageColor(bitmap: Bitmap, startX: Int, startY: Int, size: Int): Int {
        var totalR = 0
        var totalG = 0
        var totalB = 0
        var count = 0
        
        for (x in startX until minOf(startX + size, bitmap.width)) {
            for (y in startY until minOf(startY + size, bitmap.height)) {
                val pixel = bitmap.getPixel(x, y)
                totalR += android.graphics.Color.red(pixel)
                totalG += android.graphics.Color.green(pixel)
                totalB += android.graphics.Color.blue(pixel)
                count++
            }
        }
        
        return if (count > 0) {
            android.graphics.Color.rgb(totalR / count, totalG / count, totalB / count)
        } else {
            android.graphics.Color.BLACK
        }
    }
    
    private fun applyArtisticEffect(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val r = android.graphics.Color.red(pixel)
                val g = android.graphics.Color.green(pixel)
                val b = android.graphics.Color.blue(pixel)
                
                // Apply artistic color transformation
                val newR = (r * 0.8f + g * 0.2f).toInt().coerceIn(0, 255)
                val newG = (g * 0.9f + b * 0.1f).toInt().coerceIn(0, 255)
                val newB = (b * 0.7f + r * 0.3f).toInt().coerceIn(0, 255)
                
                result.setPixel(x, y, android.graphics.Color.rgb(newR, newG, newB))
            }
        }
        return result
    }
    
    private fun applyFilter(bitmap: Bitmap, filter: FilterType): Bitmap {
        return when (filter) {
            FilterType.GRAYSCALE -> applyGrayscaleFilter(bitmap)
            FilterType.SEPIA -> applySepiaFilter(bitmap)
            FilterType.NEON -> applyNeonFilter(bitmap)
            else -> bitmap
        }
    }
    
    private fun applyGrayscaleFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val gray = (android.graphics.Color.red(pixel) * 0.299f + 
                           android.graphics.Color.green(pixel) * 0.587f + 
                           android.graphics.Color.blue(pixel) * 0.114f).toInt()
                result.setPixel(x, y, android.graphics.Color.rgb(gray, gray, gray))
            }
        }
        return result
    }
    
    private fun applySepiaFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val r = android.graphics.Color.red(pixel)
                val g = android.graphics.Color.green(pixel)
                val b = android.graphics.Color.blue(pixel)
                
                val newR = (r * 0.393f + g * 0.769f + b * 0.189f).toInt().coerceIn(0, 255)
                val newG = (r * 0.349f + g * 0.686f + b * 0.168f).toInt().coerceIn(0, 255)
                val newB = (r * 0.272f + g * 0.534f + b * 0.131f).toInt().coerceIn(0, 255)
                
                result.setPixel(x, y, android.graphics.Color.rgb(newR, newG, newB))
            }
        }
        return result
    }
    
    private fun applyNeonFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val brightness = (android.graphics.Color.red(pixel) + 
                                android.graphics.Color.green(pixel) + 
                                android.graphics.Color.blue(pixel)) / 3
                
                val neonColor = when {
                    brightness > 200 -> android.graphics.Color.CYAN
                    brightness > 150 -> android.graphics.Color.MAGENTA
                    brightness > 100 -> android.graphics.Color.YELLOW
                    else -> android.graphics.Color.BLACK
                }
                result.setPixel(x, y, neonColor)
            }
        }
        return result
    }
    
    private fun shouldIncludePixel(pixel: Int, style: MorphingStyle): Boolean {
        return when (style) {
            MorphingStyle.EDGE_DETECTION -> android.graphics.Color.red(pixel) > 128
            MorphingStyle.PIXELATED -> android.graphics.Color.alpha(pixel) > 128
            else -> android.graphics.Color.alpha(pixel) > 128
        }
    }
    
    private fun createExplosion(position: Offset, color: androidx.compose.ui.graphics.Color) {
        val explosion = Explosion(position)
        repeat(20) { // Create 20 particles per explosion
            explosion.particles.add(
                ExplosionParticle(
                    position = position,
                    velocity = Offset(
                        (Random.nextFloat() - 0.5f) * 300f,
                        (Random.nextFloat() - 0.5f) * 300f
                    ),
                    color = color,
                    size = Random.nextFloat() * 4f + 2f
                )
            )
        }
        explosions.add(explosion)
    }
    
    private fun createFireworkParticle() {
        val colors = listOf(
            androidx.compose.ui.graphics.Color.Red,
            androidx.compose.ui.graphics.Color.Yellow,
            androidx.compose.ui.graphics.Color.Blue,
            androidx.compose.ui.graphics.Color.Green,
            androidx.compose.ui.graphics.Color.Magenta,
            androidx.compose.ui.graphics.Color.Cyan
        )
        
        fireworkParticles.add(
            FireworkParticle(
                position = Offset(
                    Random.nextFloat() * width,
                    height + Random.nextFloat() * 50f
                ),
                velocity = Offset(
                    (Random.nextFloat() - 0.5f) * 200f,
                    -Random.nextFloat() * 400f - 200f
                ),
                color = colors.random(),
                size = Random.nextFloat() * 6f + 3f
            )
        )
    }
    
    private fun createGalaxyParticle() {
        val colors = listOf(
            androidx.compose.ui.graphics.Color(0xFFFFFFFF.toInt()), // White stars
            androidx.compose.ui.graphics.Color(0xFFFFC864.toInt()), // Yellow stars
            androidx.compose.ui.graphics.Color(0xFF6496FF.toInt()), // Blue stars
            androidx.compose.ui.graphics.Color(0xFFFF6496.toInt())  // Pink stars
        )
        
        galaxyParticles.add(
            GalaxyParticle(
                position = Offset(
                    Random.nextFloat() * width,
                    Random.nextFloat() * height
                ),
                velocity = Offset(
                    (Random.nextFloat() - 0.5f) * 50f,
                    (Random.nextFloat() - 0.5f) * 50f
                ),
                color = colors.random(),
                size = Random.nextFloat() * 4f + 2f
            )
        )
    }
    
    private fun createBubble() {
        bubbles.add(
            Bubble(
                position = Offset(
                    Random.nextFloat() * width,
                    height + Random.nextFloat() * 20f
                ),
                velocity = Offset(
                    (Random.nextFloat() - 0.5f) * 30f,
                    -Random.nextFloat() * 100f - 50f
                ),
                size = Random.nextFloat() * 15f + 10f
            )
        )
    }
    
    private fun createLightning() {
        val startX = Random.nextFloat() * width
        val endX = startX + (Random.nextFloat() - 0.5f) * 200f
        
        lightningBolts.add(
            Lightning(
                start = Offset(startX, 0f),
                end = Offset(endX, height * 0.7f)
            )
        )
    }
    
    fun clearAllMorphingDrops() {
        println("DEBUG: Force clearing all morphing drops")
        
        // Create explosion effects for all morphing drops
        val morphingDrops = drops.filter { it.isMorphing }
        println("DEBUG: Found ${morphingDrops.size} morphing drops to explode")
        
        morphingDrops.forEach { drop ->
            createExplosion(drop.position, drop.color)
        }
        
        // Clear all drops and effects
        drops.clear()
        explosions.clear()
        ripples.clear()
        fireworkParticles.clear()
        galaxyParticles.clear()
        bubbles.clear()
        lightningBolts.clear()
        
        // Reset morphing state
        isMorphing = false
        morphTarget = null
        
        println("DEBUG: All morphing drops cleared, total drops: ${drops.size}")
    }
    
    fun reverseMorph() {
        println("DEBUG: reverseMorph called, isMorphing: $isMorphing")
        
        // Create explosion effect for all morphing drops
        val morphingDrops = drops.filter { it.isMorphing }
        println("DEBUG: Found ${morphingDrops.size} morphing drops")
        
        morphingDrops.forEach { drop ->
            createExplosion(drop.position, drop.color)
        }
        
        // Reset to rain mode
        isMorphing = false
        morphTarget = null
        
        // Clear all drops and regenerate for rain
        drops.clear()
        
        // Generate new drops for rain animation
        repeat(50) { i ->
            drops.add(
                WaterDrop(
                    id = i,
                    position = Offset(
                        Random.nextFloat() * width,
                        Random.nextFloat() * height
                    ),
                    size = Random.nextFloat() * 6f + 4f,
                    color = androidx.compose.ui.graphics.Color.White,
                    isMorphing = false,
                    targetPosition = null
                )
            )
        }
        
        println("DEBUG: reverseMorph completed, new drop count: ${drops.size}")
    }
    
    fun stopAnimation() {
        animationJob?.cancel()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
} 