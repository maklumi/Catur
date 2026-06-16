package com.github.maklumi.catur.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.board.isLightSquare
import com.github.maklumi.catur.model.game.state.GameSnapshotState

@Composable
fun ChessBoard(
    state: GameSnapshotState,
    onPositionClick: (Position) -> Unit = {}
) {
    Column {
        Text(
            text = "Turn: ${state.activeColor}",
            modifier = Modifier.padding(8.dp),
            fontSize = 20.sp
        )
        for (rank in 8 downTo 1) {
            Row {
                for (file in 1..8) {
                    val position = Position.from(file, rank)
                    SquareView(
                        position = position,
                        state = state,
                        onClick = { onPositionClick(position) }
                    )
                }
            }
        }
    }
}

@Composable
fun SquareView(
    position: Position,
    state: GameSnapshotState,
    onClick: () -> Unit
) {
    val square = state.board[position]
    val isSelected = state.selectedPosition == position
    val isLegalMove = state.isLegalMove(position)

    val backgroundColor = when {
        isSelected -> Color.Yellow
        isLegalMove -> Color.Green.copy(alpha = 0.5f)
        position.isLightSquare() -> Color(0xFFEEEED2)
        else -> Color(0xFF769656)
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        square.piece?.let { piece ->
            Text(
                text = piece.symbol,
                fontSize = 32.sp,
                color = Color.Black
            )
        }
    }
}
