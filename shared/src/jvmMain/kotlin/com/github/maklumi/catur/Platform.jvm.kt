package com.github.maklumi.catur

import com.github.maklumi.catur.model.PersistenceManager
import com.github.maklumi.catur.model.game.audio.SoundType
import com.github.maklumi.catur.model.game.controller.GameController
import com.github.maklumi.catur.model.game.engine.RemoteChessEngine
import kotlinx.coroutines.CoroutineScope
import java.awt.Toolkit
import javax.sound.sampled.AudioSystem
import java.io.BufferedInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.prefs.Preferences

class JVMPersistenceManager : PersistenceManager {
    private val prefs = Preferences.userRoot().node("com/github/maklumi/catur")

    override fun saveCompletedPuzzles(indices: Set<Int>) {
        prefs.put("completed_puzzles", indices.joinToString(","))
        prefs.flush()
    }

    override fun loadCompletedPuzzles(): Set<Int> {
        val s = prefs.get("completed_puzzles", "") ?: ""
        if (s.isEmpty()) return emptySet()
        return s.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }
}

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val persistenceManager: PersistenceManager = JVMPersistenceManager()

    override fun createGameController(scope: CoroutineScope): GameController {
        return GameController(
            engine = RemoteChessEngine("http://127.0.0.1:8000"),
            scope = scope,
            platform = this
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

    override fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy.MM.dd", Locale.US).format(Date())
    }

    override fun setClipboardText(text: String) {
        val stringSelection = java.awt.datatransfer.StringSelection(text)
        val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(stringSelection, null)
    }
}

actual fun getPlatform(): Platform = JVMPlatform()
