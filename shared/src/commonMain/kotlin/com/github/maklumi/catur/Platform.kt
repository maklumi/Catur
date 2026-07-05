package com.github.maklumi.catur

import com.github.maklumi.catur.data.persistence.PersistenceManager
import com.github.maklumi.catur.domain.audio.SoundType
import com.github.maklumi.catur.state.controller.GameController
import kotlinx.coroutines.CoroutineScope

interface Platform {
    val name: String
    val isMobile: Boolean
    val persistenceManager: PersistenceManager
    fun createGameController(scope: CoroutineScope): GameController
    fun playSound(type: SoundType)
    fun getCurrentDate(): String
    fun generateId(): String
    fun setClipboardText(text: String)
}

expect fun getPlatform(): Platform