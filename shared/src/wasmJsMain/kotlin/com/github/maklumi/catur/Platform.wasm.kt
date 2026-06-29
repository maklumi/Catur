package com.github.maklumi.catur

import com.github.maklumi.catur.model.PersistenceManager
import com.github.maklumi.catur.model.game.audio.SoundType
import com.github.maklumi.catur.model.game.controller.GameController
import com.github.maklumi.catur.model.game.engine.RemoteChessEngine
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
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => new Date().toISOString().split('T')[0].replace(/-/g, '.')")
external fun jsGetCurrentDate(): String

class WebPlatform : Platform {
    override val name: String = "Web (Wasm)"
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

    @OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
    override fun setClipboardText(text: String) {
        window.navigator.clipboard.writeText(text)
    }
}

actual fun getPlatform(): Platform = WebPlatform()
