package com.github.maklumi.catur.ui

import androidx.compose.foundation.Image
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
import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.model.game.state.GameState
import com.github.maklumi.catur.model.game.state.GameSnapshotState
import com.github.maklumi.catur.model.game.state.GameStatus
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.piece.Piece
import com.github.maklumi.catur.model.piece.PieceColor
import org.jetbrains.compose.resources.painterResource
import catur.shared.generated.resources.*

@Composable
fun PieceImage(piece: Piece, modifier: Modifier = Modifier) {
    val resource = when (piece.resName) {
        "dubrovny_bb" -> Res.drawable.dubrovny_bb
        "dubrovny_bk" -> Res.drawable.dubrovny_bk
        "dubrovny_bn" -> Res.drawable.dubrovny_bn
        "dubrovny_bp" -> Res.drawable.dubrovny_bp
        "dubrovny_bq" -> Res.drawable.dubrovny_bq
        "dubrovny_br" -> Res.drawable.dubrovny_br
        "dubrovny_wb" -> Res.drawable.dubrovny_wb
        "dubrovny_wk" -> Res.drawable.dubrovny_wk
        "dubrovny_wn" -> Res.drawable.dubrovny_wn
        "dubrovny_wp" -> Res.drawable.dubrovny_wp
        "dubrovny_wq" -> Res.drawable.dubrovny_wq
        "dubrovny_wr" -> Res.drawable.dubrovny_wr
        else -> Res.drawable.compose_multiplatform
    }
    Image(
        painter = painterResource(resource),
        contentDescription = piece.symbol,
        modifier = modifier
    )
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
                    val activeName = if (snapshot.activeColor == PieceColor.WHITE) state.whiteName else state.blackName
                    Text(
                        text = "Turn: $activeName",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 20.sp
                    )
                    if (snapshot.status != GameStatus.ONGOING) {
                        Text(
                            text = " - ${snapshot.status}",
                            color = Color.Red,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Button(onClick = { onAction(GameAction.ReverseSides) }) {
                        Text("Reverse Sides")
                    }
                    if (snapshot.status == GameStatus.ONGOING) {
                        Button(onClick = { onAction(GameAction.Resign) }) {
                            Text("Resign")
                        }
                        if (snapshot.drawOfferedBy == null) {
                            Button(onClick = { onAction(GameAction.OfferDraw) }) {
                                Text("Offer Draw")
                            }
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
                        Button(onClick = { onAction(GameAction.AcceptDraw) }) {
                            Text("Accept")
                        }
                        Button(onClick = { onAction(GameAction.DeclineDraw) }) {
                            Text("Decline")
                        }
                    }
                }

                val ranks = if (state.isBoardFlipped) 1..8 else 8 downTo 1
                val files = if (state.isBoardFlipped) 8 downTo 1 else 1..8

                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                    ) {
                        for (rank in ranks) {
                            Row(modifier = Modifier.weight(1f)) {
                                for (file in files) {
                                    val position = Position.from(file, rank)
                                    SquareView(
                                        position = position,
                                        snapshot = snapshot,
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                        onClick = { onAction(GameAction.SquareClick(position)) }
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(onClick = { onAction(GameAction.StepBack) }, enabled = state.canGoBack()) {
                        Text("Back")
                    }
                    Text(
                        text = "${state.currentIndex} / ${state.snapshots.size - 1}",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Button(onClick = { onAction(GameAction.StepForward) }, enabled = state.canGoForward()) {
                        Text("Forward")
                    }
                }
            }

            Spacer(modifier = Modifier.width(32.dp))

            Column(modifier = Modifier.width(200.dp).fillMaxHeight()) {
                val topName = if (state.isBoardFlipped) state.whiteName else state.blackName
                val topCaptured = if (state.isBoardFlipped) snapshot.capturedBlack else snapshot.capturedWhite
                
                val bottomName = if (state.isBoardFlipped) state.blackName else state.whiteName
                val bottomCaptured = if (state.isBoardFlipped) snapshot.capturedWhite else snapshot.capturedBlack

                Text(text = topName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                CapturedPiecesView(pieces = topCaptured)
                
                Spacer(modifier = Modifier.height(8.dp))
                MoveHistoryList(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = bottomName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                CapturedPiecesView(pieces = bottomCaptured)
            }
        }

        snapshot.pendingPromotion?.let { moves ->
            PromotionDialog(
                moves = moves,
                onChoice = { onAction(GameAction.PromotionChoice(it)) }
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
            PieceImage(
                piece = piece,
                modifier = Modifier.size(24.dp).padding(horizontal = 1.dp)
            )
        }
    }
}

@Composable
fun MoveHistoryList(
    state: GameState,
    onAction: (GameAction) -> Unit,
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
            fontWeight = if (state.currentIndex == 0) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier
                .clickable { onAction(GameAction.JumpToHistory(0)) }
                .padding(vertical = 4.dp)
        )

        val historySnapshots = state.snapshots.drop(1)
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
                    fontWeight = if (state.currentIndex == whiteMoveIdx) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .width(64.dp)
                        .clickable { onAction(GameAction.JumpToHistory(whiteMoveIdx)) }
                )
                if (blackMove.isNotEmpty()) {
                    Text(
                        text = blackMove,
                        fontWeight = if (state.currentIndex == blackMoveIdx) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .width(64.dp)
                            .clickable { onAction(GameAction.JumpToHistory(blackMoveIdx)) }
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
                        PieceImage(
                            piece = move.move.piece,
                            modifier = Modifier.size(48.dp)
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
    snapshot: GameSnapshotState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isSelected = snapshot.selectedPosition == position
    val isLegalMove = snapshot.isLegalMove(position)

    val backgroundColor = when {
        isSelected -> Color.Yellow
        isLegalMove -> Color.Green.copy(alpha = 0.5f)
        position.isLightSquare() -> Color(0xFFEEEED2)
        else -> Color(0xFF769656)
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        snapshot.board[position].piece?.let { piece ->
            PieceImage(
                piece = piece,
                modifier = Modifier.fillMaxSize(0.85f)
            )
        }
    }
}
