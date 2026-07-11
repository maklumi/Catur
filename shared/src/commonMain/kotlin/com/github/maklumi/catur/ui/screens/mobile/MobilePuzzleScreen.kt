package com.github.maklumi.catur.ui.screens.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.domain.chess.piece.King
import com.github.maklumi.catur.domain.chess.piece.PieceColor
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.state.model.*
import com.github.maklumi.catur.ui.components.*

@Composable
internal fun MobilePuzzleScreen(
    controller: GameController,
    boardState: BoardState,
    puzzleState: PuzzleState,
    uiVisualState: UiVisualState,
) {
    val snapshot = boardState.currentSnapshot
    val colorScheme = MaterialTheme.colorScheme
    val currentIndex = puzzleState.currentPuzzleIndex ?: 0
    val totalItems = puzzleState.puzzles.size

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        // --- Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.MENU)) }) {
                Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (currentIndex > 0) controller.dispatch(GameAction.Puzzles.SelectPuzzle(currentIndex - 1))
                    },
                    enabled = currentIndex > 0
                ) {
                    Text("◀", fontSize = 20.sp, color = if (currentIndex > 0) colorScheme.onBackground else colorScheme.outline)
                }

                Text(
                    text = "${currentIndex + 1} / $totalItems",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                IconButton(
                    onClick = { controller.dispatch(GameAction.Puzzles.NextPuzzle) },
                    enabled = currentIndex < totalItems - 1
                ) {
                    Text("▶", fontSize = 20.sp, color = if (currentIndex < totalItems - 1) colorScheme.onBackground else colorScheme.outline)
                }
            }

            Row {
                IconButton(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.PUZZLES)) }) {
                    Text("⊞", fontSize = 24.sp, color = colorScheme.onBackground)
                }
                IconButton(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.SETTINGS)) }) {
                    Text("⋮", fontSize = 24.sp, color = colorScheme.onBackground)
                }
            }
        }

        // --- Sub-header ---
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PieceImage(
                    piece = King(snapshot.activeColor),
                    modifier = Modifier.size(24.dp).padding(end = 8.dp)
                )
                Text(
                    text = "${snapshot.activeColor.name.lowercase().replaceFirstChar { it.uppercase() }} to move",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
            }
            Text(
                text = "● Previous result: ${puzzleState.completedPuzzleIndices.size} from $totalItems",
                fontSize = 12.sp,
                color = colorScheme.secondary
            )
        }

        // --- Board Area ---
        MobileBoardView(
            boardState = boardState,
            uiVisualState = uiVisualState,
            onAction = { controller.dispatch(it) }
        )

        // --- Stats Row ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val totalSteps = puzzleState.puzzles.getOrNull(currentIndex)?.solutionMoves?.size ?: 0
            Text(
                text = "${puzzleState.currentPuzzleStep}/$totalSteps",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.outline
            )

            Text(text = "±", fontSize = 28.sp, color = colorScheme.onBackground)

            val completed = puzzleState.completedPuzzleIndices.size
            Text(
                text = "Score: $completed",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.outline
            )
        }

        // --- Info / Controls Area ---
        Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)) {
            Text(
                text = puzzleState.puzzles.getOrNull(currentIndex)?.title ?: "",
                fontSize = 14.sp,
                color = colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f)
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                val historySnapshots = boardState.snapshots.drop(1)
                val startsWithBlack = boardState.snapshots.firstOrNull()?.activeColor == PieceColor.BLACK
                
                val history = historySnapshots.indices.joinToString(" ") { i ->
                    val moveNumber = if (startsWithBlack) {
                        if (i == 0) "1... " else if (i % 2 != 0) "${(i + 1) / 2 + 1}. " else ""
                    } else {
                        if (i % 2 == 0) "${i / 2 + 1}. " else ""
                    }
                    "$moveNumber${historySnapshots[i].notation ?: ""}"
                }

                Text(
                    text = history.ifEmpty { "Play for advantage" },
                    fontSize = 16.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto-forward", fontSize = 12.sp, color = colorScheme.outline)
                Switch(
                    checked = puzzleState.isAutoForward,
                    onCheckedChange = { controller.dispatch(GameAction.Puzzles.SetAutoForward(it)) },
                    modifier = Modifier.scale(0.8f)
                )
            }
        }
    }
}
