package com.rvp.waterdropart.ui.components

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WaterDropViewComposable(
    modifier: Modifier = Modifier,
    onViewCreated: (WaterDropView) -> Unit = {}
) {
    var waterDropView by remember { mutableStateOf<WaterDropView?>(null) }
    
    AndroidView(
        factory = { context ->
            WaterDropView(context).also { view ->
                waterDropView = view
                onViewCreated(view)
            }
        },
        modifier = modifier
    )
    
    // Start animation when view is created
    LaunchedEffect(waterDropView) {
        waterDropView?.startRainAnimation()
    }
}

fun WaterDropView.morphToImageFromUri(context: android.content.Context, uri: android.net.Uri) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        morphToImage(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
    }
} 