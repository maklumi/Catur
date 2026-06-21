package com.github.maklumi.catur

import android.os.Build
import com.github.maklumi.catur.model.game.audio.SoundType
import com.github.maklumi.catur.model.game.controller.GameController
import kotlinx.coroutines.CoroutineScope

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override fun createGameController(scope: CoroutineScope): GameController {
        return GameController(
            engine = null,
            scope = scope
        )
    }

    override fun playSound(type: SoundType) {
        // TODO: Implement Android sound playing
    }
}

actual fun getPlatform(): Platform = AndroidPlatform()