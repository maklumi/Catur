package com.github.maklumi.catur

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    println("Initializing Compose Wasm...")
    ComposeViewport(viewportContainerId = "compose-target") {
        App()
    }
}
