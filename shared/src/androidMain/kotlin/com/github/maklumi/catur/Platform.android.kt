package com.github.maklumi.catur

import android.os.Build
import com.github.maklumi.catur.model.game.audio.SoundType
import com.github.maklumi.catur.model.game.controller.GameController
import com.github.maklumi.catur.model.game.engine.RemoteChessEngine
import kotlinx.coroutines.CoroutineScope

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override fun createGameController(scope: CoroutineScope): GameController {
        return GameController(
            engine = RemoteChessEngine("http://10.0.2.2:8000"),
            scope = scope
        )
    }

    override fun playSound(type: SoundType) {
        // Implement Android sound playing
    }
}

actual fun getPlatform(): Platform = AndroidPlatform()