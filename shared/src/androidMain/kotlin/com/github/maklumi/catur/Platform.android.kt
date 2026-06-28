package com.github.maklumi.catur

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.github.maklumi.catur.model.PersistenceManager
import com.github.maklumi.catur.model.game.audio.SoundType
import com.github.maklumi.catur.model.game.controller.GameController
import com.github.maklumi.catur.model.game.engine.RemoteChessEngine
import kotlinx.coroutines.CoroutineScope
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidPersistenceManager(context: Context) : PersistenceManager {
    private val prefs: SharedPreferences = context.getSharedPreferences("catur_prefs", Context.MODE_PRIVATE)

    override fun saveCompletedPuzzles(indices: Set<Int>) {
        prefs.edit { putString("completed_puzzles", indices.joinToString(",")) }
    }

    override fun loadCompletedPuzzles(): Set<Int> {
        val s = prefs.getString("completed_puzzles", "") ?: ""
        if (s.isEmpty()) return emptySet()
        return s.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }
}

class AndroidPlatform(context: Context) : Platform {
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
}

private var androidPlatform: Platform? = null

fun initPlatform(context: Context) {
    androidPlatform = AndroidPlatform(context)
}

actual fun getPlatform(): Platform = androidPlatform ?: throw IllegalStateException("Platform not initialized. Call initPlatform(context) first.")
