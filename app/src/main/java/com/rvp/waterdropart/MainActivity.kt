package com.rvp.waterdropart

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rvp.waterdropart.service.VolumeButtonService
import com.rvp.waterdropart.ui.theme.WaterDropArtTheme
import com.rvp.waterdropart.ui.screens.MainScreen

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start the volume button service
        startService(Intent(this, VolumeButtonService::class.java))
        
        setContent {
            WaterDropArtTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Stop the service when activity is destroyed
        stopService(Intent(this, VolumeButtonService::class.java))
    }
} 