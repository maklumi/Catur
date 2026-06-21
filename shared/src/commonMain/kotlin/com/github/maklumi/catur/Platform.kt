package com.github.maklumi.catur

import com.github.maklumi.catur.model.game.audio.SoundType
import com.github.maklumi.catur.model.game.controller.GameController
import kotlinx.coroutines.CoroutineScope

interface Platform {
    val name: String
    fun createGameController(scope: CoroutineScope): GameController
    fun playSound(type: SoundType)
}

expect fun getPlatform(): Platform