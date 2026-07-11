package com.github.maklumi.catur

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.Preview
import com.github.maklumi.catur.state.model.GameAction
import com.github.maklumi.catur.state.model.Screen
import com.github.maklumi.catur.ui.screens.*
import com.github.maklumi.catur.ui.theme.CaturTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.github.maklumi.catur.state.model.UiVisualState

@Composable
@Preview
fun App() {
    val scope = rememberCoroutineScope()
    val platform = remember { getPlatform() }
    val controller = remember { platform.createGameController(scope) }
    val uiVisualState by controller.uiVisualState.collectAsState(UiVisualState())

    val focusRequester = remember { FocusRequester() }

    // This observer helps stop engines when the app is no longer visible
    DisposableEffect(Unit) {
        onDispose {
            controller.dispose()
        }
    }

    LaunchedEffect(uiVisualState.currentScreen) {
        if (uiVisualState.currentScreen == Screen.GAME || uiVisualState.currentScreen == Screen.ANALYSIS) {
            focusRequester.requestFocus()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.dispose()
        }
    }

    CaturTheme(boardTheme = uiVisualState.boardTheme) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (platform.isMobile) {
                        Modifier
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    } else Modifier
                ),
            color = MaterialTheme.colorScheme.background
        ) {
            when (uiVisualState.currentScreen) {
                Screen.MENU -> {
                    MainMenuView(onAction = { controller.dispatch(it) })
                }
                Screen.PLAY_SELECTION -> {
                    PlaySelectionView(onAction = { controller.dispatch(it) })
                }
                Screen.GAME, Screen.ANALYSIS -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.key) {
                                        Key.DirectionLeft -> {
                                            controller.dispatch(GameAction.Nav.StepBack)
                                            true
                                        }
                                        Key.DirectionRight -> {
                                            controller.dispatch(GameAction.Nav.StepForward)
                                            true
                                        }
                                        else -> false
                                    }
                                } else false
                            }
                            .focusRequester(focusRequester)
                            .focusable()
                    ) {
                        ChessBoard(controller = controller)
                    }
                }
                Screen.PUZZLES -> {
                    PuzzleListView(controller = controller)
                }
                Screen.SETTINGS -> {
                    SettingsView(controller = controller)
                }
                Screen.HISTORY -> {
                    HistoryView(controller = controller)
                }
            }
        }
    }
}
