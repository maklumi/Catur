package com.github.maklumi.catur.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.board.isLightSquare
import com.github.maklumi.catur.state.model.*
import com.github.maklumi.catur.ui.theme.CaturTheme

@Composable
fun SquareView(
    position: Position,
    boardState: BoardState,
    uiVisualState: UiVisualState,
    modifier: Modifier = Modifier,
    showRank: Boolean = false,
    showFile: Boolean = false,
    onAction: (GameAction) -> Unit
) {
    val snapshot = boardState.currentSnapshot
    val eval = uiVisualState.moveEvaluations.entries.find {
        it.key.length >= 4 && it.key.substring(2, 4) == position.toString()
    }?.value
    val isLastMove =
        snapshot.lastMove?.let { it.move.from == position || it.move.to == position } ?: false
    val isLegalMove = snapshot.legalMoves.any { it.move.to == position }
    val piece = snapshot.board[position].piece
    val hasPiece = piece != null
    val isLight = position.isLightSquare()
    val isThreatened = uiVisualState.threats.contains(position)

    val boardColors = CaturTheme.board

    // --- Animation Logic ---
    var squareSize by remember { mutableIntStateOf(0) }
    val ranks = if (boardState.isBoardFlipped) 1..8 else 8 downTo 1
    val files = if (boardState.isBoardFlipped) 8 downTo 1 else 1..8

    val rankIdx = ranks.indexOf(position.rank)
    val fileIdx = files.indexOf(position.file)

    val lastMove = snapshot.lastMove?.move
    val lastMoveId = boardState.lastMoveId
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
            animOffset.animateTo(
                androidx.compose.ui.geometry.Offset.Zero,
                animationSpec = tween(300)
            )
        }
    }

    val backgroundColor = when {
        snapshot.selectedPosition == position || uiVisualState.longPressedPosition == position -> boardColors.selected
        eval != null -> {
            val normalized = ((eval + 300) / 600f).coerceIn(0f, 1f)
            Color(red = normalized, green = 1f - normalized, blue = 0f, alpha = 0.6f)
        }

        isLastMove -> boardColors.lastMove
        isThreatened -> if (isLight) boardColors.threatenedLight else boardColors.threatenedDark
        isLight -> boardColors.lightSquare
        else -> boardColors.darkSquare
    }

    val labelColor = if (isLight) boardColors.darkSquare else boardColors.lightSquare

    Box(
        modifier = modifier
            .zIndex(if (wasJustMovedTo) 10f else 0f)
            .background(backgroundColor)
            .onGloballyPositioned { squareSize = it.size.width }
            .pointerInput(position) {
                detectTapGestures(
                    onTap = {
                        onAction(GameAction.Ui.ClearLongPress)
                        onAction(GameAction.Move.SquareClick(position))
                    },
                    onLongPress = { onAction(GameAction.Ui.SquareLongPress(position)) }
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
                modifier = Modifier.align(Alignment.TopStart).padding(start = 2.dp, top = 1.dp)
            )
        }

        if (showFile) {
            Text(
                text = position.toString().take(1),
                color = labelColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 2.dp, bottom = 1.dp)
            )
        }

        piece?.let { p ->
            val density = androidx.compose.ui.platform.LocalDensity.current
            val pieceSizeDp = with(density) { (squareSize * 1.1f).toDp() }
            
            PieceImage(
                piece = p,
                modifier = Modifier
                    .size(pieceSizeDp)
                    .zIndex(10f)
                    .offset { IntOffset(animOffset.value.x.toInt(), animOffset.value.y.toInt()) }
            )
        }

        if (isLegalMove) {
            if (!hasPiece) {
                Box(
                    modifier = Modifier.size(16.dp)
                        .background(Color.Black.copy(alpha = 0.15f), CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().padding(4.dp)
                        .border(4.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                )
            }
        }
    }
}
