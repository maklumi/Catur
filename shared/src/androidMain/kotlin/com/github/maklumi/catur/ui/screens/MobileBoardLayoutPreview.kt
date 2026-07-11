package com.github.maklumi.catur.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.maklumi.catur.Platform
import com.github.maklumi.catur.data.persistence.PersistenceManager
import com.github.maklumi.catur.domain.audio.SoundType
import com.github.maklumi.catur.domain.chess.GameRecord
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.state.model.*
import com.github.maklumi.catur.ui.screens.mobile.MobileAnalysisScreen
import com.github.maklumi.catur.ui.screens.mobile.MobileGameScreen
import com.github.maklumi.catur.ui.screens.mobile.MobilePuzzleScreen
import kotlinx.coroutines.CoroutineScope

@Preview(showBackground = true)
@Composable
internal fun MobileGameScreenPreview() {
    val controller = createPreviewController()
    MaterialTheme {
        MobileGameScreen(
            controller = controller,
            boardState = BoardState(),
            matchState = MatchState(),
            clockState = ClockState(),
            uiVisualState = UiVisualState()
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun MobileAnalysisScreenPreview() {
    val controller = createPreviewController()
    MaterialTheme {
        MobileAnalysisScreen(
            controller = controller,
            boardState = BoardState(),
            matchState = MatchState(),
            uiVisualState = UiVisualState(currentScreen = Screen.ANALYSIS)
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun MobilePuzzleScreenPreview() {
    val controller = createPreviewController()
    MaterialTheme {
        MobilePuzzleScreen(
            controller = controller,
            boardState = BoardState(),
            puzzleState = PuzzleState(currentPuzzleIndex = 0),
            uiVisualState = UiVisualState()
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun PuzzleListViewPreview() {
    val controller = createPreviewController()
    MaterialTheme {
        PuzzleListView(
            controller = controller
        )
    }
}

private fun createPreviewController(): GameController {
    val dummyPlatform = object : Platform {
        override val name: String = "Preview"
        override val isMobile: Boolean = true
        override val persistenceManager = object : PersistenceManager {
            override fun saveCompletedPuzzles(indices: Set<Int>) {}
            override fun loadCompletedPuzzles(): Set<Int> = emptySet()
            override fun saveGame(record: GameRecord) {}
            override fun loadGames(): List<GameRecord> = emptyList()
            override fun saveSettings(theme: String, soundEnabled: Boolean, engineModel: String) {}
            override fun loadSettings(): Triple<String, Boolean, String>? = null
        }
        override fun createGameController(scope: CoroutineScope): GameController = GameController(platform = this)
        override fun playSound(type: SoundType) {}
        override fun getCurrentDate(): String = "2024.01.01"
        override fun generateId(): String = "preview"
        override fun setClipboardText(text: String) {}
    }
    return GameController(platform = dummyPlatform)
}
