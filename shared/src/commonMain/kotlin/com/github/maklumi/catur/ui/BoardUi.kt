package com.github.maklumi.catur.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.game.controller.GameController
import com.github.maklumi.catur.model.game.state.*
import com.github.maklumi.catur.model.piece.PieceColor
import com.github.maklumi.catur.ui.theme.CaturTheme

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

@Composable
fun ChessBoard(
    controller: GameController,
) {
    val boardState by controller.boardState.collectAsState(BoardState())
    val matchState by controller.matchState.collectAsState(MatchState())
    val clockState by controller.clockState.collectAsState(ClockState())
    val engineState by controller.engineState.collectAsState(EngineState())
    val uiVisualState by controller.uiVisualState.collectAsState(UiVisualState())
    
    val snapshot = boardState.currentSnapshot
    val colorScheme = MaterialTheme.colorScheme
    
    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeName = if (snapshot.activeColor == PieceColor.WHITE) matchState.whiteName else matchState.blackName
                    Text(
                        text = "Turn: $activeName",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 20.sp,
                        color = colorScheme.onBackground
                    )
                    if (engineState.isThinking) {
                        Text(
                            text = "(Thinking...)",
                            color = colorScheme.outline,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    if (snapshot.status != GameStatus.ONGOING) {
                        Text(
                            text = " - ${snapshot.status}",
                            color = colorScheme.error,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Button(onClick = { controller.dispatch(GameAction.ReverseSides) }) { Text("Reverse Sides") }
                    if (snapshot.status == GameStatus.ONGOING) {
                        Button(onClick = { controller.dispatch(GameAction.Resign) }) { Text("Resign") }
                        if (snapshot.drawOfferedBy == null) {
                            Button(onClick = { controller.dispatch(GameAction.OfferDraw) }) { Text("Offer Draw") }
                        }
                    }
                }

                if (snapshot.status == GameStatus.ONGOING && snapshot.drawOfferedBy != null && snapshot.drawOfferedBy != snapshot.activeColor) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Draw offered by ${snapshot.drawOfferedBy}", color = colorScheme.onBackground)
                        Button(onClick = { controller.dispatch(GameAction.AcceptDraw) }) { Text("Accept") }
                        Button(onClick = { controller.dispatch(GameAction.DeclineDraw) }) { Text("Decline") }
                    }
                }

                val ranks = if (boardState.isBoardFlipped) 1..8 else 8 downTo 1
                val files = if (boardState.isBoardFlipped) 8 downTo 1 else 1..8
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
                                        val isLeftmost = file == (if (boardState.isBoardFlipped) 8 else 1)
                                        val isBottom = rank == (if (boardState.isBoardFlipped) 8 else 1)

                                        SquareView(
                                            position = position,
                                            boardState = boardState,
                                            uiVisualState = uiVisualState,
                                            modifier = Modifier.weight(1f).fillMaxHeight(),
                                            showRank = isLeftmost,
                                            showFile = isBottom,
                                            onAction = { controller.dispatch(it) }
                                        )
                                    }
                                }
                            }
                        }

                        uiVisualState.bestMoveArrow?.let { arrow ->
                            ChessBoardOverlay(
                                from = arrow.first,
                                to = arrow.second,
                                isBoardFlipped = boardState.isBoardFlipped,
                                color = colorScheme.primary.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { controller.dispatch(GameAction.StepBack) },
                        enabled = boardState.canGoBack()
                    ) { Text("Back") }
                    Text(
                        text = "${boardState.currentIndex} / ${boardState.snapshots.size - 1}",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        color = colorScheme.onBackground
                    )
                    Button(
                        onClick = { controller.dispatch(GameAction.StepForward) },
                        enabled = boardState.canGoForward()
                    ) { Text("Forward") }
                }
            }

            Spacer(modifier = Modifier.width(32.dp))

            Column(modifier = Modifier.width(200.dp).fillMaxHeight()) {
                val topName = if (boardState.isBoardFlipped) matchState.whiteName else matchState.blackName
                val topCaptured = if (boardState.isBoardFlipped) snapshot.capturedBlack else snapshot.capturedWhite
                val topTime = if (boardState.isBoardFlipped) clockState.whiteTimeMillis else clockState.blackTimeMillis

                val bottomName = if (boardState.isBoardFlipped) matchState.blackName else matchState.whiteName
                val bottomCaptured = if (boardState.isBoardFlipped) snapshot.capturedWhite else snapshot.capturedBlack
                val bottomTime = if (boardState.isBoardFlipped) clockState.blackTimeMillis else clockState.whiteTimeMillis

                val topImbalance = if (boardState.isBoardFlipped) {
                    if (snapshot.materialImbalance > 0) snapshot.materialImbalance else 0
                } else {
                    if (snapshot.materialImbalance < 0) -snapshot.materialImbalance else 0
                }

                val bottomImbalance = if (boardState.isBoardFlipped) {
                    if (snapshot.materialImbalance < 0) -snapshot.materialImbalance else 0
                } else {
                    if (snapshot.materialImbalance > 0) snapshot.materialImbalance else 0
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = topName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onBackground)
                    Text(
                        text = formatTime(topTime),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (topTime < 30000) colorScheme.error else colorScheme.onBackground
                    )
                }
                CapturedPiecesView(pieces = topCaptured, imbalance = topImbalance)

                Spacer(modifier = Modifier.height(8.dp))
                MoveHistoryList(controller = controller, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = bottomName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onBackground)
                    Text(
                        text = formatTime(bottomTime),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (bottomTime < 30000) colorScheme.error else colorScheme.onBackground
                    )
                }
                CapturedPiecesView(pieces = bottomCaptured, imbalance = bottomImbalance)

                Spacer(modifier = Modifier.height(16.dp))
                EngineLevelSelector(
                    currentModel = engineState.model,
                    onModelChange = { controller.dispatch(GameAction.ChangeEngineLevel(it)) })
            }

            Spacer(modifier = Modifier.width(32.dp))

            Column(modifier = Modifier.width(200.dp).fillMaxHeight()) {
                PuzzleList(
                    controller = controller,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        snapshot.pendingPromotion?.let { moves ->
            PromotionDialog(moves = moves, onChoice = { controller.dispatch(GameAction.PromotionChoice(it)) })
        }
    }
}
