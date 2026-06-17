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
import com.github.maklumi.catur.model.game.state.Game
import com.github.maklumi.catur.ui.ChessBoard

@Composable
@Preview
fun App() {
    var game by remember {
        mutableStateOf(Game())
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.DirectionLeft -> {
                                game = game.goBack()
                                true
                            }
                            Key.DirectionRight -> {
                                game = game.goForward()
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
                game = game,
                onPositionClick = { position ->
                    game = game.move(position)
                },
                onPromotionChoice = { move ->
                    game = game.promote(move)
                },
                onBack = {
                    game = game.goBack()
                },
                onForward = {
                    game = game.goForward()
                },
                onHistoryClick = { index ->
                    game = game.jumpTo(index)
                }
            )
        }
    }
}
