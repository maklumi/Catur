package com.github.maklumi.catur.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.board.isLightSquare
import com.github.maklumi.catur.model.game.state.Game
import com.github.maklumi.catur.model.game.state.GameSnapshotState
import com.github.maklumi.catur.model.game.state.GameStatus
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.piece.Piece

@Composable
fun ChessBoard(
    game: Game,
    onPositionClick: (Position) -> Unit = {},
    onPromotionChoice: (BoardMove) -> Unit = {},
    onBack: () -> Unit = {},
    onForward: () -> Unit = {},
    onHistoryClick: (Int) -> Unit = {}
) {
    val state = game.currentSnapshot
    Box {
        Row(modifier = Modifier.padding(16.dp)) {
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

            Spacer(modifier = Modifier.width(32.dp))

            Column(modifier = Modifier.width(200.dp).fillMaxHeight()) {
                CapturedPiecesView(pieces = state.capturedWhite)
                Spacer(modifier = Modifier.height(8.dp))
                MoveHistoryList(
                    game = game,
                    onHistoryClick = onHistoryClick,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                CapturedPiecesView(pieces = state.capturedBlack)
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
fun CapturedPiecesView(pieces: List<Piece>) {
    val sortedPieces = pieces.sortedBy { it.value }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        sortedPieces.forEach { piece ->
            Text(
                text = piece.symbol,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 1.dp)
            )
        }
    }
}

@Composable
fun MoveHistoryList(
    game: Game,
    onHistoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Move History",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Starting position
        Text(
            text = "Start",
            fontWeight = if (game.currentIndex == 0) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier
                .clickable { onHistoryClick(0) }
                .padding(vertical = 4.dp)
        )

        val historySnapshots = game.snapshots.drop(1)
        for (i in historySnapshots.indices step 2) {
            val turnNumber = i / 2 + 1
            val whiteMoveIdx = i + 1
            val blackMoveIdx = i + 2
            
            val whiteMove = historySnapshots[i].notation ?: ""
            val blackMove = historySnapshots.getOrNull(i + 1)?.notation ?: ""

            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = "$turnNumber. ",
                    color = Color.Gray,
                    modifier = Modifier.width(32.dp)
                )
                Text(
                    text = whiteMove,
                    fontWeight = if (game.currentIndex == whiteMoveIdx) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .width(64.dp)
                        .clickable { onHistoryClick(whiteMoveIdx) }
                )
                if (blackMove.isNotEmpty()) {
                    Text(
                        text = blackMove,
                        fontWeight = if (game.currentIndex == blackMoveIdx) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .width(64.dp)
                            .clickable { onHistoryClick(blackMoveIdx) }
                    )
                }
            }
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
