package com.github.maklumi.catur

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val initialSettings = WindowSettings.getInitial()
    val windowState = rememberWindowState(
        placement = initialSettings.placement,
        position = initialSettings.position,
        size = initialSettings.size
    )

    Window(
        onCloseRequest = {
            WindowSettings.save(windowState)
            exitApplication()
        },
        title = "Catur",
        state = windowState
    ) {
        App()
    }
}
