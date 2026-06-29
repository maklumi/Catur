package com.github.maklumi.catur

import com.github.maklumi.catur.model.PersistenceManager
import com.github.maklumi.catur.model.game.audio.SoundType
import com.github.maklumi.catur.model.game.controller.GameController
import kotlinx.coroutines.CoroutineScope

interface Platform {
    val name: String
    val persistenceManager: PersistenceManager
    fun createGameController(scope: CoroutineScope): GameController
    fun playSound(type: SoundType)
    fun getCurrentDate(): String
    fun setClipboardText(text: String)
}

expect fun getPlatform(): Platform