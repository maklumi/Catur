package com.github.maklumi.catur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.piece.King
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.state.model.*
import com.github.maklumi.catur.ui.components.*

@Composable
internal fun MobileBoardLayout(
    controller: GameController,
    boardState: BoardState,
    matchState: MatchState,
    clockState: ClockState,
    puzzleState: PuzzleState,
    uiVisualState: UiVisualState,
) {
    val snapshot = boardState.currentSnapshot
    val colorScheme = MaterialTheme.colorScheme
    val isPuzzle = puzzleState.currentPuzzleIndex != null
    val currentIndex = if (isPuzzle) puzzleState.currentPuzzleIndex else boardState.currentIndex
    val totalItems = if (isPuzzle) puzzleState.puzzles.size else boardState.snapshots.size - 1

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        // --- Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.MENU)) }) {
                Text(
                    "←",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (isPuzzle) {
                            if (currentIndex > 0) controller.dispatch(
                                GameAction.Puzzles.SelectPuzzle(
                                    currentIndex - 1
                                )
                            )
                        } else {
                            controller.dispatch(GameAction.Nav.StepBack)
                        }
                    },
                    enabled = currentIndex > 0
                ) {
                    Text(
                        "◀",
                        fontSize = 20.sp,
                        color = if (currentIndex > 0) colorScheme.onBackground else colorScheme.outline
                    )
                }

                Text(
                    text = if (isPuzzle) "${currentIndex + 1} / $totalItems" else "$currentIndex / $totalItems",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                IconButton(
                    onClick = {
                        if (isPuzzle) {
                            controller.dispatch(GameAction.Puzzles.NextPuzzle)
                        } else {
                            controller.dispatch(GameAction.Nav.StepForward)
                        }
                    },
                    enabled = currentIndex < totalItems
                ) {
                    Text(
                        "▶",
                        fontSize = 20.sp,
                        color = if (currentIndex < totalItems) colorScheme.onBackground else colorScheme.outline
                    )
                }
            }

            Row {
                if (isPuzzle) {
                    IconButton(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.PUZZLES)) }) {
                        Text("⊞", fontSize = 24.sp, color = colorScheme.onBackground)
                    }
                }
                IconButton(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.SETTINGS)) }) {
                    Text("⋮", fontSize = 24.sp, color = colorScheme.onBackground)
                }
            }
        }

        // --- Sub-header: Turn Indicator ---
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
                    text = if (uiVisualState.currentScreen == Screen.ANALYSIS) "Analysis Mode" else "${
                        snapshot.activeColor.name.lowercase().replaceFirstChar { it.uppercase() }
                    } to move",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
            }
            if (isPuzzle) {
                Text(
                    text = "● Previous result: ${puzzleState.completedPuzzleIndices.size} from $totalItems",
                    fontSize = 12.sp,
                    color = colorScheme.secondary
                )
            } else {
                Text(
                    text = boardState.openingName ?: "",
                    fontSize = 12.sp,
                    color = colorScheme.secondary
                )
            }
        }

        // --- Board Area ---
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

        // --- Stats Row ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPuzzle) {
                val totalSteps = puzzleState.puzzles.getOrNull(currentIndex)?.solutionMoves?.size ?: 0
                Text(
                    text = "${puzzleState.currentPuzzleStep}/$totalSteps",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.outline
                )
            } else {
                Text(
                    text = matchState.whiteName.take(8),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
            }

            Text(
                text = "±",
                fontSize = 28.sp,
                color = colorScheme.onBackground
            )

            if (isPuzzle) {
                val completed = puzzleState.completedPuzzleIndices.size
                Text(
                    text = "Score: $completed",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.outline
                )
            } else {
                val time =
                    if (boardState.isBoardFlipped) clockState.whiteTimeMillis else clockState.blackTimeMillis
                Text(
                    text = formatTime(time),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (time < 30000) colorScheme.error else colorScheme.onBackground
                )
            }
        }

        // --- Info / Controls Area ---
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)
        ) {
            if (isPuzzle) {
                Text(
                    text = puzzleState.puzzles.getOrNull(currentIndex)?.title ?: "",
                    fontSize = 14.sp,
                    color = colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                        .background(
                            colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    val history = boardState.snapshots.drop(1).joinToString(" ") { it.notation ?: "" }
                    
                    Text(
                        text = if (history.isNotEmpty()) history else "Play for advantage",
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
            } else {
                MoveHistoryList(controller = controller, modifier = Modifier.weight(1f))

                if (uiVisualState.currentScreen == Screen.GAME && snapshot.status == GameStatus.ONGOING) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { controller.dispatch(GameAction.Flow.Resign) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.errorContainer,
                                contentColor = colorScheme.onErrorContainer
                            )
                        ) { Text("Resign") }
                        Button(
                            onClick = { controller.dispatch(GameAction.Flow.OfferDraw) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Draw") }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
