package com.github.maklumi.catur

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.tooling.preview.Preview
import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.ui.ChessBoard
import com.github.maklumi.catur.ui.theme.CaturTheme

@Composable
@Preview
fun App() {
    val scope = rememberCoroutineScope()
    val controller = remember { getPlatform().createGameController(scope) }
    val state by controller.state.collectAsState()

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.dispose()
        }
    }

    CaturTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.DirectionLeft -> {
                                controller.dispatch(GameAction.StepBack)
                                true
                            }
                            Key.DirectionRight -> {
                                controller.dispatch(GameAction.StepForward)
                                true
                            }
                            else -> false
                        }
                    } else false
                }
                .focusRequester(focusRequester)
                .focusable()
        ) {
            ChessBoard(
                state = state,
                onAction = { controller.dispatch(it) }
            )
        }
    }
}
