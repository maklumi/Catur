package com.github.maklumi.catur

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.game.state.GameSnapshotState
import com.github.maklumi.catur.ui.ChessBoard

@Composable
@Preview
fun App() {
    var state by remember {
        mutableStateOf(
            GameSnapshotState(
                board = Board.initial
            )
        )
    }

    MaterialTheme {
        ChessBoard(
            state = state,
            onPositionClick = { position ->
                state = state.move(position)
            }
        )
    }
}
