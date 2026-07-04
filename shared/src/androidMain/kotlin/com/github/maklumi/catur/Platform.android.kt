package com.github.maklumi.catur

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.github.maklumi.catur.data.persistence.PersistenceManager
import com.github.maklumi.catur.domain.audio.SoundType
import com.github.maklumi.catur.domain.chess.GameRecord
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.domain.engine.RemoteChessEngine
import kotlinx.coroutines.CoroutineScope
import androidx.core.content.edit
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidPersistenceManager(private val context: Context) : PersistenceManager {
    private val prefs: SharedPreferences = context.getSharedPreferences("catur_prefs", Context.MODE_PRIVATE)
    private val historyDir = File(context.filesDir, "history")

    init {
        if (!historyDir.exists()) {
            historyDir.mkdirs()
        }
    }

    override fun saveCompletedPuzzles(indices: Set<Int>) {
        prefs.edit { putString("completed_puzzles", indices.joinToString(",")) }
    }

    override fun loadCompletedPuzzles(): Set<Int> {
        val s = prefs.getString("completed_puzzles", "") ?: ""
        if (s.isEmpty()) return emptySet()
        return s.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    override fun saveGame(record: GameRecord) {
        val file = File(historyDir, "${record.id}.pgn")
        try {
            file.writeText(record.pgn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun loadGames(): List<GameRecord> {
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

class AndroidPlatform(private val context: Context) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val persistenceManager: PersistenceManager = AndroidPersistenceManager(context)

    override fun createGameController(scope: CoroutineScope): GameController {
        return GameController(
            engine = RemoteChessEngine("http://10.0.2.2:8000"),
            scope = scope,
            platform = this
        )
    }

    override fun playSound(type: SoundType) {
        // Implement Android sound playing
    }

    override fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy.MM.dd", Locale.US).format(Date())
    }

    override fun generateId(): String {
        return SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
    }

    override fun setClipboardText(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Catur PGN", text)
        clipboard.setPrimaryClip(clip)
    }
}

private var androidPlatform: Platform? = null

fun initPlatform(context: Context) {
    androidPlatform = AndroidPlatform(context)
}

actual fun getPlatform(): Platform = androidPlatform ?: throw IllegalStateException("Platform not initialized. Call initPlatform(context) first.")
