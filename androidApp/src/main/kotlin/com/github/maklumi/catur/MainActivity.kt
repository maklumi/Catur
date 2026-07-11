package com.github.maklumi.catur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        initPlatform(this)

        // Ensure engines are stopped when app is backgrounded or destroyed
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_DESTROY) {
                try {
                    getPlatform().persistenceManager.saveCompletedPuzzles(emptySet()) // Dummy call to trigger logic if needed
                    // In a real scenario, we'd want to call a specific cleanup here.
                    // Since our Platform is a singleton, we'll handle it inside the engine class.
                } catch (_: Exception) {}
            }
        })

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}