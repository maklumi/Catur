package com.github.maklumi.catur.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import catur.shared.generated.resources.Res
import catur.shared.generated.resources.compose_multiplatform
import catur.shared.generated.resources.dubrovny_bb
import catur.shared.generated.resources.dubrovny_bk
import catur.shared.generated.resources.dubrovny_bn
import catur.shared.generated.resources.dubrovny_bp
import catur.shared.generated.resources.dubrovny_bq
import catur.shared.generated.resources.dubrovny_br
import catur.shared.generated.resources.dubrovny_wb
import catur.shared.generated.resources.dubrovny_wk
import catur.shared.generated.resources.dubrovny_wn
import catur.shared.generated.resources.dubrovny_wp
import catur.shared.generated.resources.dubrovny_wq
import catur.shared.generated.resources.dubrovny_wr
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.board.isLightSquare
import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.model.game.state.GameState
import com.github.maklumi.catur.model.game.state.GameStatus
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.piece.Piece
import com.github.maklumi.catur.model.piece.PieceColor
import org.jetbrains.compose.resources.painterResource

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

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
                val lastMoveToRank = snapshot.lastMove?.move?.to?.rank

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
                            val isDestinationRank = rank == lastMoveToRank
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .zIndex(if (isDestinationRank) 5f else 0f)
                            ) {
                                for (file in files) {
                                    val position = Position.from(file, rank)
                                    val isLeftmost = file == (if (state.isBoardFlipped) 8 else 1)
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
                val topTime = if (state.isBoardFlipped) state.whiteTimeMillis else state.blackTimeMillis
                
                val bottomName = if (state.isBoardFlipped) state.blackName else state.whiteName
                val bottomCaptured = if (state.isBoardFlipped) snapshot.capturedWhite else snapshot.capturedBlack
                val bottomTime = if (state.isBoardFlipped) state.blackTimeMillis else state.whiteTimeMillis

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
                MoveHistoryList(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier.weight(1f)
                )
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
                    onModelChange = { onAction(GameAction.ChangeEngineLevel(it)) }
                )
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
fun EngineLevelSelector(
    currentModel: String,
    onModelChange: (String) -> Unit
) {
    val models = listOf(
        "maia3-3m-ablation" to "3M Ablation",
        "maia3-5m" to "5M (Standard)",
        "maia3-23m" to "23M (Strong)",
        "maia3-79m" to "79M (Expert)"
    )

    Column {
        Text(text = "Engine Level", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        models.forEach { (model, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onModelChange(model) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(if (currentModel == model) Color.Green else Color.LightGray, RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun CapturedPiecesView(pieces: List<Piece>, imbalance: Int = 0) {
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
        if (imbalance > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "+$imbalance",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
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
    gameState: GameState,
    modifier: Modifier = Modifier,
    showRank: Boolean = false,
    showFile: Boolean = false,
    onAction: (GameAction) -> Unit
) {
    val snapshot = gameState.currentSnapshot
    val eval = gameState.moveEvaluations.entries.find { it.key.substring(2, 4) == position.toString() }?.value
    val isLastMove = snapshot.lastMove?.let { it.move.from == position || it.move.to == position } ?: false
    val isLegalMove = snapshot.legalMoves.any { it.move.to == position }
    val piece = snapshot.board[position].piece
    val hasPiece = piece != null
    val isLight = position.isLightSquare()

    // --- Animation Logic ---
    var squareSize by remember { mutableIntStateOf(0) }
    val ranks = if (gameState.isBoardFlipped) 1..8 else 8 downTo 1
    val files = if (gameState.isBoardFlipped) 8 downTo 1 else 1..8
    
    val rankIdx = ranks.indexOf(position.rank)
    val fileIdx = files.indexOf(position.file)
    
    val lastMove = snapshot.lastMove?.move
    val lastMoveId = snapshot.lastMove?.id
    val wasJustMovedTo = lastMove?.to == position
    
    val animOffset = remember(lastMoveId) {
        val initialOffset = if (wasJustMovedTo && squareSize > 0) {
            val fromRankIdx = ranks.indexOf(lastMove.from.rank)
            val fromFileIdx = files.indexOf(lastMove.from.file)
            val dx = (fromFileIdx - fileIdx) * squareSize.toFloat()
            val dy = (fromRankIdx - rankIdx) * squareSize.toFloat()
            androidx.compose.ui.geometry.Offset(dx, dy)
        } else {
            androidx.compose.ui.geometry.Offset.Zero
        }
        Animatable(initialOffset, androidx.compose.ui.geometry.Offset.VectorConverter)
    }
    
    LaunchedEffect(lastMoveId) {
        if (wasJustMovedTo && squareSize > 0) {
            animOffset.animateTo(androidx.compose.ui.geometry.Offset.Zero, animationSpec = tween(300))
        }
    }
    // -----------------------

    val backgroundColor = when {
        snapshot.selectedPosition == position || gameState.longPressedPosition == position -> Color.Yellow
        eval != null -> {
            // Map eval (-300 to 300) to Red-Green gradient
            val normalized = ((eval + 300) / 600f).coerceIn(0f, 1f)
            Color(red = normalized, green = 1f - normalized, blue = 0f, alpha = 0.6f)
        }
        isLastMove -> Color(0xFFF6F682)
        isLight -> Color(0xFFEEEED2)
        else -> Color(0xFF769656)
    }

    val labelColor = if (isLight) Color(0xFF769656) else Color(0xFFEEEED2)

    Box(
        modifier = modifier
            .zIndex(if (wasJustMovedTo) 10f else 0f)
            .background(backgroundColor)
            .onGloballyPositioned { squareSize = it.size.width }
            .pointerInput(position) {
                detectTapGestures(
                    onTap = { 
                        onAction(GameAction.ClearLongPress)
                        onAction(GameAction.SquareClick(position)) 
                    },
                    onLongPress = { onAction(GameAction.SquareLongPress(position)) }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (showRank) {
            Text(
                text = position.rank.toString(),
                color = labelColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp, top = 1.dp)
            )
        }
        
        if (showFile) {
            Text(
                text = position.toString().take(1),
                color = labelColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 1.dp)
            )
        }

        piece?.let { p ->
            PieceImage(
                piece = p,
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .zIndex(10f)
                    .offset {
                        IntOffset(animOffset.value.x.toInt(), animOffset.value.y.toInt())
                    }
            )
        }

        if (isLegalMove) {
            if (!hasPiece) {
                // Dot for empty square
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Black.copy(alpha = 0.15f), CircleShape)
                )
            } else {
                // Border for capture
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .border(
                            width = 4.dp,
                            color = Color.Black.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
