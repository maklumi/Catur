package com.github.maklumi.catur

import com.github.maklumi.catur.model.game.controller.GameController
import com.github.maklumi.catur.model.game.engine.MaiaChessEngine
import kotlinx.coroutines.CoroutineScope

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"

    override fun createGameController(scope: CoroutineScope): GameController {
        return GameController(
            engine = MaiaChessEngine(),
            scope = scope
        )
    }
}

actual fun getPlatform(): Platform = JVMPlatform()