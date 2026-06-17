package com.github.maklumi.catur

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.github.maklumi.catur.model.game.state.Game
import com.github.maklumi.catur.ui.ChessBoard

@Composable
@Preview
fun App() {
    var game by remember {
        mutableStateOf(Game())
    }

    MaterialTheme {
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
            }
        )
    }
}
