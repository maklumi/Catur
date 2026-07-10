package com.github.maklumi.catur

import com.github.maklumi.catur.data.persistence.PersistenceManager
import com.github.maklumi.catur.domain.audio.SoundType
import com.github.maklumi.catur.domain.chess.GameRecord
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.domain.engine.RemoteChessEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.browser.window

class WebPersistenceManager : PersistenceManager {
    override fun saveCompletedPuzzles(indices: Set<Int>) {
        window.localStorage.setItem("completed_puzzles", indices.joinToString(","))
    }

    override fun loadCompletedPuzzles(): Set<Int> {
        val s = window.localStorage.getItem("completed_puzzles") ?: ""
        if (s.isEmpty()) return emptySet()
        return s.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    override fun saveGame(record: GameRecord) {
        val games = loadGamesIds().toMutableSet()
        games.add(record.id)
        window.localStorage.setItem("game_ids", games.joinToString(","))
        window.localStorage.setItem("game_${record.id}", record.pgn)
    }

    override fun loadGames(): List<GameRecord> {
        val ids = loadGamesIds()
        return ids.mapNotNull { id ->
            val pgn = window.localStorage.getItem("game_$id") ?: return@mapNotNull null
            parseRecordFromPgn(id, pgn)
        }.sortedByDescending { it.date }
    }

    override fun saveSettings(theme: String, soundEnabled: Boolean, engineModel: String) {
        window.localStorage.setItem("board_theme", theme)
        window.localStorage.setItem("sound_enabled", soundEnabled.toString())
        window.localStorage.setItem("engine_model", engineModel)
    }

    override fun loadSettings(): Triple<String, Boolean, String>? {
        val theme = window.localStorage.getItem("board_theme") ?: return null
        val sound = window.localStorage.getItem("sound_enabled")?.toBoolean() ?: true
        val engine = window.localStorage.getItem("engine_model") ?: "maia3-5m"
        return Triple(theme, sound, engine)
    }

    private fun loadGamesIds(): Set<String> {
        val s = window.localStorage.getItem("game_ids") ?: ""
        if (s.isEmpty()) return emptySet()
        return s.split(",").toSet()
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

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => new Date().toISOString().split('T')[0].replace(/-/g, '.')")
external fun jsGetCurrentDate(): String

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { const d = new Date(); const pad = (n) => n.toString().padStart(2, '0'); return d.getFullYear() + pad(d.getMonth()+1) + pad(d.getDate()) + '-' + pad(d.getHours()) + pad(d.getMinutes()) + pad(d.getSeconds()); }")
external fun jsGenerateId(): String

class WebPlatform : Platform {
    override val name: String = "Web (Wasm)"
    override val isMobile: Boolean = false
    override val persistenceManager: PersistenceManager = WebPersistenceManager()

    override fun createGameController(scope: CoroutineScope): GameController {
        return GameController(
            engine = RemoteChessEngine("http://localhost:8000"), // Might need adjustment for CORS
            scope = scope,
            platform = this
        )
    }

    override fun playSound(type: SoundType) {
        // Simple Web audio can be added here using HTML5 Audio
    }

    override fun getCurrentDate(): String = jsGetCurrentDate()

    override fun generateId(): String = jsGenerateId()

    @OptIn(ExperimentalWasmJsInterop::class)
    override fun setClipboardText(text: String) {
        window.navigator.clipboard.writeText(text)
    }
}

actual fun getPlatform(): Platform = WebPlatform()
