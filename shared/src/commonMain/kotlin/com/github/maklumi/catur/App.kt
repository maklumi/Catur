package com.github.maklumi.catur

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.game.state.GameSnapshotState
import com.github.maklumi.catur.model.piece.King
import com.github.maklumi.catur.model.piece.PieceColor
import com.github.maklumi.catur.model.piece.Rook
import com.github.maklumi.catur.ui.ChessBoard

@Composable
@Preview
fun App() {
    var state by remember {
        mutableStateOf(
            GameSnapshotState(
                board = Board()
                    .withPiece(Position.e1, King(PieceColor.WHITE))
                    .withPiece(Position.h1, Rook(PieceColor.WHITE))
                    .withPiece(Position.a1, Rook(PieceColor.WHITE))
                    .withPiece(Position.e8, King(PieceColor.BLACK))
                    .withPiece(Position.h8, Rook(PieceColor.BLACK)),
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
