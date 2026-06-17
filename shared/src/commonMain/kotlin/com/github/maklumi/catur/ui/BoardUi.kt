package com.github.maklumi.catur.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.board.isLightSquare
import com.github.maklumi.catur.model.game.state.Game
import com.github.maklumi.catur.model.game.state.GameSnapshotState
import com.github.maklumi.catur.model.game.state.GameStatus
import com.github.maklumi.catur.model.move.BoardMove

@Composable
fun ChessBoard(
    game: Game,
    onPositionClick: (Position) -> Unit = {},
    onPromotionChoice: (BoardMove) -> Unit = {},
    onBack: () -> Unit = {},
    onForward: () -> Unit = {}
) {
    val state = game.currentSnapshot
    Box {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Turn: ${state.activeColor}",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 20.sp
                )
                if (state.status != GameStatus.ONGOING) {
                    Text(
                        text = " - ${state.status}",
                        color = Color.Red,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

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

            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onBack, enabled = game.canGoBack()) {
                    Text("Back")
                }
                Text(
                    text = "${game.currentIndex} / ${game.snapshots.size - 1}",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Button(onClick = onForward, enabled = game.canGoForward()) {
                    Text("Forward")
                }
            }
        }

        state.pendingPromotion?.let { moves ->
            PromotionDialog(
                moves = moves,
                onChoice = onPromotionChoice
            )
        }
    }
}

@Composable
fun PromotionDialog(
    moves: List<BoardMove>,
    onChoice: (BoardMove) -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moves.forEach { move ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.LightGray)
                            .clickable { onChoice(move) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = move.move.piece.symbol,
                            fontSize = 40.sp,
                            color = Color.Black
                        )
                    }
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
