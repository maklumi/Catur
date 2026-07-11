package com.github.maklumi.catur.ui.screens.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.state.model.*
import com.github.maklumi.catur.ui.components.*

@Composable
internal fun MobileBoardView(
    boardState: BoardState,
    uiVisualState: UiVisualState,
    onAction: (GameAction) -> Unit
) {
    val snapshot = boardState.currentSnapshot
    val ranks = if (boardState.isBoardFlipped) 1..8 else 8 downTo 1
    val files = if (boardState.isBoardFlipped) 8 downTo 1 else 1..8
    val lastMoveToRank = snapshot.lastMove?.to?.rank

    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (rank in ranks) {
                val isDestinationRank = rank == lastMoveToRank
                Row(
                    modifier = Modifier.weight(1f).zIndex(if (isDestinationRank) 5f else 0f)
                ) {
                    for (file in files) {
                        val position = Position.from(file, rank)
                        SquareView(
                            position = position,
                            boardState = boardState,
                            uiVisualState = uiVisualState,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            showRank = file == (if (boardState.isBoardFlipped) 8 else 1),
                            showFile = rank == (if (boardState.isBoardFlipped) 8 else 1),
                            onAction = onAction
                        )
                    }
                }
            }
        }
        
        if (uiVisualState.currentScreen == Screen.ANALYSIS && uiVisualState.topAnalysisMoves.isNotEmpty()) {
            ChessBoardArrows(
                moves = uiVisualState.topAnalysisMoves,
                isBoardFlipped = boardState.isBoardFlipped
            )
        } else {
            uiVisualState.bestMoveArrow?.let { arrow ->
                ChessBoardOverlay(
                    from = arrow.first,
                    to = arrow.second,
                    isBoardFlipped = boardState.isBoardFlipped
                )
            }
        }
    }
}
