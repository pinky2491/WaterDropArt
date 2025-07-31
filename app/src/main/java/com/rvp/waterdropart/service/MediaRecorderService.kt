package com.rvp.waterdropart.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaRecorder
import android.view.View
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class MediaRecorderService(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = AtomicBoolean(false)
    private var recordingJob: Job? = null
    private var frameRate = 30 // FPS
    private var recordingDuration = 5000L // 5 seconds
    
    fun startVideoRecording(view: View, onComplete: (File?) -> Unit) {
        if (isRecording.get()) return
        
        isRecording.set(true)
        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val videoFile = createVideoFile()
                setupMediaRecorder(videoFile)
                mediaRecorder?.start()
                
                // Record frames
                val frameDelay = 1000L / frameRate
                val totalFrames = (recordingDuration / frameDelay).toInt()
                
                repeat(totalFrames) { frameIndex ->
                    if (!isRecording.get()) return@repeat
                    
                    val bitmap = captureViewFrame(view)
                    // In a real implementation, you'd encode the bitmap to video
                    // For now, we'll save as a sequence of images
                    saveFrame(bitmap, frameIndex)
                    
                    delay(frameDelay)
                }
                
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
                
                withContext(Dispatchers.Main) {
                    onComplete(videoFile)
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            } finally {
                isRecording.set(false)
            }
        }
    }
    
    fun createGIF(view: View, duration: Long = 3000L, onComplete: (File?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val gifFile = createGifFile()
                val frames = mutableListOf<Bitmap>()
                val frameCount = 30 // 30 frames for 3 seconds
                val frameDelay = duration / frameCount
                
                repeat(frameCount) { frameIndex ->
                    val bitmap = captureViewFrame(view)
                    frames.add(bitmap)
                    delay(frameDelay)
                }
                
                // Save as GIF (simplified - in real app you'd use a GIF library)
                saveFramesAsGIF(frames, gifFile)
                
                withContext(Dispatchers.Main) {
                    onComplete(gifFile)
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            }
        }
    }
    
    private fun captureViewFrame(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    
    private fun setupMediaRecorder(outputFile: File) {
        mediaRecorder = MediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(frameRate)
            setVideoSize(1080, 1920)
            setOutputFile(outputFile.absolutePath)
            prepare()
        }
    }
    
    private fun createVideoFile(): File {
        val fileName = "waterdrop_${System.currentTimeMillis()}.mp4"
        val dir = File(context.getExternalFilesDir(null), "videos")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, fileName)
    }
    
    private fun createGifFile(): File {
        val fileName = "waterdrop_${System.currentTimeMillis()}.gif"
        val dir = File(context.getExternalFilesDir(null), "gifs")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, fileName)
    }
    
    private fun saveFrame(bitmap: Bitmap, frameIndex: Int) {
        val fileName = "frame_${frameIndex.toString().padStart(4, '0')}.png"
        val dir = File(context.getExternalFilesDir(null), "frames")
        if (!dir.exists()) dir.mkdirs()
        
        val file = File(dir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
    
    private fun saveFramesAsGIF(frames: List<Bitmap>, outputFile: File) {
        // Simplified GIF creation - in a real app you'd use a proper GIF library
        // For now, we'll save as a sequence of PNG files
        frames.forEachIndexed { index, bitmap ->
            val fileName = "gif_frame_${index.toString().padStart(4, '0')}.png"
            val file = File(outputFile.parentFile, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }
    
    fun stopRecording() {
        isRecording.set(false)
        recordingJob?.cancel()
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
    }
    
    fun isRecording(): Boolean = isRecording.get()
} 