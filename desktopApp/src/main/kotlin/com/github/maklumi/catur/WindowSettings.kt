package com.github.maklumi.catur

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import java.io.File
import java.util.Properties

object WindowSettings {
    private val file = File("window.properties")
    private val properties = Properties()

    data class InitialSettings(
        val placement: WindowPlacement,
        val position: WindowPosition,
        val size: DpSize
    )

    fun getInitial(): InitialSettings {
        if (file.exists()) {
            try {
                file.inputStream().use { properties.load(it) }
                
                val placement = properties.getProperty("placement")?.let { 
                    WindowPlacement.valueOf(it) 
                } ?: WindowPlacement.Floating
                
                val width = properties.getProperty("width")?.toFloatOrNull() ?: 1200f
                val height = properties.getProperty("height")?.toFloatOrNull() ?: 1000f
                
                val x = properties.getProperty("x")?.toFloatOrNull()
                val y = properties.getProperty("y")?.toFloatOrNull()
                val position = if (x != null && y != null) {
                    WindowPosition.Absolute(x.dp, y.dp)
                } else {
                    WindowPosition.Aligned(Alignment.Center)
                }
                
                return InitialSettings(placement, position, DpSize(width.dp, height.dp))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return InitialSettings(
            WindowPlacement.Floating,
            WindowPosition.Aligned(Alignment.Center),
            DpSize(1200.dp, 1000.dp)
        )
    }

    fun save(state: WindowState) {
        try {
            properties.setProperty("placement", state.placement.name)
            
            properties.setProperty("width", state.size.width.value.toString())
            properties.setProperty("height", state.size.height.value.toString())
            
            val position = state.position
            if (position is WindowPosition.Absolute) {
                properties.setProperty("x", position.x.value.toString())
                properties.setProperty("y", position.y.value.toString())
            }
            
            file.outputStream().use { properties.store(it, null) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
