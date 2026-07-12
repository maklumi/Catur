package com.github.maklumi.catur

import com.github.maklumi.catur.data.persistence.PersistenceManager
import com.github.maklumi.catur.domain.audio.SoundType
import com.github.maklumi.catur.domain.chess.GameRecord
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.domain.engine.JVMLocalChessEngine
import kotlinx.coroutines.CoroutineScope
import java.awt.Toolkit
import javax.sound.sampled.AudioSystem
import java.io.BufferedInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties

class JVMPersistenceManager : PersistenceManager {
    private val prefsFile = File("catur_prefs.properties")
    private val historyDir = File("history")

    init {
        if (!historyDir.exists()) {
            historyDir.mkdirs()
        }
    }

    override fun saveCompletedPuzzles(indices: Set<Int>) {
        val props = Properties()
        if (prefsFile.exists()) {
            try {
                prefsFile.inputStream().use { props.load(it) }
            } catch (_: Exception) { }
        }
        
        props.setProperty("completed_puzzles", indices.joinToString(","))
        
        try {
            prefsFile.outputStream().use { 
                props.store(it, "Catur Preferences")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun loadCompletedPuzzles(): Set<Int> {
        if (!prefsFile.exists()) return emptySet()
        val props = Properties()
        try {
            prefsFile.inputStream().use { 
                props.load(it)
            }
            val s = props.getProperty("completed_puzzles", "") ?: ""
            if (s.isEmpty()) return emptySet()
            return s.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        } catch (e: Exception) {
            e.printStackTrace()
            return emptySet()
        }
    }

    override fun saveGame(record: GameRecord) {
        val fileName = "${record.id}.pgn"
        val file = File(historyDir, fileName)
        try {
            file.writeText(record.pgn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun loadGames(): List<GameRecord> {
        if (!historyDir.exists()) return emptyList()
        val files = historyDir.listFiles { _, name -> name.endsWith(".pgn") } ?: return emptyList()
        
        return files.mapNotNull { file ->
            try {
                val content = file.readText()
                parseRecordFromPgn(file.nameWithoutExtension, content)
            } catch (_: Exception) {
                null
            }
        }.sortedByDescending { it.date }
    }

    override fun saveSettings(theme: String, soundEnabled: Boolean, engineModel: String) {
        val props = Properties()
        if (prefsFile.exists()) {
            try {
                prefsFile.inputStream().use { props.load(it) }
            } catch (_: Exception) { }
        }
        props.setProperty("board_theme", theme)
        props.setProperty("sound_enabled", soundEnabled.toString())
        props.setProperty("engine_model", engineModel)
        try {
            prefsFile.outputStream().use { props.store(it, "Catur Preferences") }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun loadSettings(): Triple<String, Boolean, String>? {
        if (!prefsFile.exists()) return null
        val props = Properties()
        try {
            prefsFile.inputStream().use { props.load(it) }
            val theme = props.getProperty("board_theme", "GREEN")
            val sound = props.getProperty("sound_enabled", "true").toBoolean()
            val engine = props.getProperty("engine_model", "maia-1500")
            return Triple(theme, sound, engine)
        } catch (_: Exception) {
            return null
        }
    }

    private fun parseRecordFromPgn(id: String, pgn: String): GameRecord {
        fun extractTag(tag: String): String {
            val pattern = "\\[$tag \"(.*?)\"]".toRegex()
            return pattern.find(pgn)?.groupValues?.get(1) ?: "Unknown"
        }

        return GameRecord(
            id = id,
            date = extractTag("Date"),
            white = extractTag("White"),
            black = extractTag("Black"),
            result = extractTag("Result"),
            opening = extractTag("Opening").takeIf { it != "Unknown" },
            pgn = pgn
        )
    }
}

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val isMobile: Boolean = false
    override val persistenceManager: PersistenceManager = JVMPersistenceManager()

    override fun createGameController(scope: CoroutineScope): GameController {
        return GameController(
            engine = JVMLocalChessEngine(),
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

    override fun generateId(): String {
        return SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
    }

    override fun setClipboardText(text: String) {
        val stringSelection = java.awt.datatransfer.StringSelection(text)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(stringSelection, null)
    }
}

actual fun getPlatform(): Platform = JVMPlatform()
