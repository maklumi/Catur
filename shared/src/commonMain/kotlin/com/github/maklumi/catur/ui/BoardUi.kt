package com.github.maklumi.catur.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.model.game.state.GameState
import com.github.maklumi.catur.model.game.state.GameStatus
import com.github.maklumi.catur.model.piece.PieceColor

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

@Composable
fun ChessBoard(
    state: GameState,
    onAction: (GameAction) -> Unit
) {
    val snapshot = state.currentSnapshot
    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeName =
                        if (snapshot.activeColor == PieceColor.WHITE) state.whiteName else state.blackName
                    Text(
                        text = "Turn: $activeName",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 20.sp
                    )
                    if (state.isEngineThinking) {
                        Text(
                            text = "(Thinking...)",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    if (snapshot.status != GameStatus.ONGOING) {
                        Text(
                            text = " - ${snapshot.status}",
                            color = Color.Red,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Button(onClick = { onAction(GameAction.ReverseSides) }) { Text("Reverse Sides") }
                    if (snapshot.status == GameStatus.ONGOING) {
                        Button(onClick = { onAction(GameAction.Resign) }) { Text("Resign") }
                        if (snapshot.drawOfferedBy == null) {
                            Button(onClick = { onAction(GameAction.OfferDraw) }) { Text("Offer Draw") }
                        }
                    }
                }

                if (snapshot.status == GameStatus.ONGOING && snapshot.drawOfferedBy != null && snapshot.drawOfferedBy != snapshot.activeColor) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Draw offered by ${snapshot.drawOfferedBy}")
                        Button(onClick = { onAction(GameAction.AcceptDraw) }) { Text("Accept") }
                        Button(onClick = { onAction(GameAction.DeclineDraw) }) { Text("Decline") }
                    }
                }

                val ranks = if (state.isBoardFlipped) 1..8 else 8 downTo 1
                val files = if (state.isBoardFlipped) 8 downTo 1 else 1..8
                val lastMoveToRank = snapshot.lastMove?.move?.to?.rank

                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.fillMaxHeight().aspectRatio(1f)) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            for (rank in ranks) {
                                val isDestinationRank = rank == lastMoveToRank
                                Row(
                                    modifier = Modifier.weight(1f)
                                        .zIndex(if (isDestinationRank) 5f else 0f)
                                ) {
                                    for (file in files) {
                                        val position = Position.from(file, rank)
                                        val isLeftmost =
                                            file == (if (state.isBoardFlipped) 8 else 1)
                                        val isBottom = rank == (if (state.isBoardFlipped) 8 else 1)

                                        SquareView(
                                            position = position,
                                            gameState = state,
                                            modifier = Modifier.weight(1f).fillMaxHeight(),
                                            showRank = isLeftmost,
                                            showFile = isBottom,
                                            onAction = { onAction(it) }
                                        )
                                    }
                                }
                            }
                        }

                        state.bestMoveArrow?.let { arrow ->
                            ChessBoardOverlay(
                                from = arrow.first,
                                to = arrow.second,
                                isBoardFlipped = state.isBoardFlipped,
                                color = Color.Green.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { onAction(GameAction.StepBack) },
                        enabled = state.canGoBack()
                    ) { Text("Back") }
                    Text(
                        text = "${state.currentIndex} / ${state.snapshots.size - 1}",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Button(
                        onClick = { onAction(GameAction.StepForward) },
                        enabled = state.canGoForward()
                    ) { Text("Forward") }
                }
            }

            Spacer(modifier = Modifier.width(32.dp))

            Column(modifier = Modifier.width(200.dp).fillMaxHeight()) {
                val topName = if (state.isBoardFlipped) state.whiteName else state.blackName
                val topCaptured =
                    if (state.isBoardFlipped) snapshot.capturedBlack else snapshot.capturedWhite
                val topTime =
                    if (state.isBoardFlipped) state.whiteTimeMillis else state.blackTimeMillis

                val bottomName = if (state.isBoardFlipped) state.blackName else state.whiteName
                val bottomCaptured =
                    if (state.isBoardFlipped) snapshot.capturedWhite else snapshot.capturedBlack
                val bottomTime =
                    if (state.isBoardFlipped) state.blackTimeMillis else state.whiteTimeMillis

                val topImbalance = if (state.isBoardFlipped) {
                    if (snapshot.materialImbalance > 0) snapshot.materialImbalance else 0
                } else {
                    if (snapshot.materialImbalance < 0) -snapshot.materialImbalance else 0
                }

                val bottomImbalance = if (state.isBoardFlipped) {
                    if (snapshot.materialImbalance < 0) -snapshot.materialImbalance else 0
                } else {
                    if (snapshot.materialImbalance > 0) snapshot.materialImbalance else 0
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = topName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = formatTime(topTime),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (topTime < 30000) Color.Red else Color.Unspecified
                    )
                }
                CapturedPiecesView(pieces = topCaptured, imbalance = topImbalance)

                Spacer(modifier = Modifier.height(8.dp))
                MoveHistoryList(state = state, onAction = onAction, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = bottomName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = formatTime(bottomTime),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (bottomTime < 30000) Color.Red else Color.Unspecified
                    )
                }
                CapturedPiecesView(pieces = bottomCaptured, imbalance = bottomImbalance)

                Spacer(modifier = Modifier.height(16.dp))
                EngineLevelSelector(
                    currentModel = state.engineModel,
                    onModelChange = { onAction(GameAction.ChangeEngineLevel(it)) })
            }
        }

        snapshot.pendingPromotion?.let { moves ->
            PromotionDialog(moves = moves, onChoice = { onAction(GameAction.PromotionChoice(it)) })
        }
    }
}