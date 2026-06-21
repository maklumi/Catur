package com.github.maklumi.catur

import com.github.maklumi.catur.model.game.audio.SoundType
import com.github.maklumi.catur.model.game.controller.GameController
import com.github.maklumi.catur.model.game.engine.MaiaChessEngine
import kotlinx.coroutines.CoroutineScope
import java.awt.Toolkit
import javax.sound.sampled.AudioSystem
import java.io.BufferedInputStream

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"

    override fun createGameController(scope: CoroutineScope): GameController {
        return GameController(
            engine = MaiaChessEngine(),
            scope = scope
        )
    }

    override fun playSound(type: SoundType) {
        val fileName = when (type) {
            SoundType.MOVE -> "Move.wav"
            SoundType.CAPTURE -> "Capture.wav"
            SoundType.CHECK -> "Check.wav"
            SoundType.GAME_END -> "Victory.wav"
        }
        
        try {
            val resourceStream = this::class.java.classLoader.getResourceAsStream("composeResources/catur.shared.generated.resources/files/$fileName")
            if (resourceStream != null) {
                val audioStream = AudioSystem.getAudioInputStream(BufferedInputStream(resourceStream))
                val clip = AudioSystem.getClip()
                clip.open(audioStream)
                clip.start()
            } else {
                // Fallback to system beep if file not found
                Toolkit.getDefaultToolkit().beep()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

actual fun getPlatform(): Platform = JVMPlatform()