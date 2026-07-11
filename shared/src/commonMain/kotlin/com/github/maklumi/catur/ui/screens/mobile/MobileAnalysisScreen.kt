package com.github.maklumi.catur.ui.screens.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.domain.chess.piece.King
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.state.model.*
import com.github.maklumi.catur.ui.components.*

@Composable
internal fun MobileAnalysisScreen(
    controller: GameController,
    boardState: BoardState,
    matchState: MatchState,
    uiVisualState: UiVisualState,
) {
    val snapshot = boardState.currentSnapshot
    val colorScheme = MaterialTheme.colorScheme
    val totalItems = boardState.snapshots.size - 1
    val currentIndex = boardState.currentIndex

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        // --- Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.MENU)) }) {
                Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                PieceImage(
                    piece = King(snapshot.activeColor),
                    modifier = Modifier.size(24.dp).padding(end = 8.dp)
                )
                Text(
                    text = "Analysis Mode",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
            }

            Row {
                IconButton(onClick = { controller.dispatch(GameAction.Ui.SetPgnImportDialogOpen(open = true)) }) {
                    Text("📥", fontSize = 24.sp, color = colorScheme.onBackground)
                }
                IconButton(onClick = { controller.dispatch(GameAction.Ui.SetFenImportDialogOpen(open = true)) }) {
                    Text("🧩", fontSize = 24.sp, color = colorScheme.onBackground)
                }
                IconButton(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.SETTINGS)) }) {
                    Text("⋮", fontSize = 24.sp, color = colorScheme.onBackground)
                }
            }
        }

        // --- Opening name ---
        Row (
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = boardState.openingName ?: "",
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
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = matchState.whiteName.take(8),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { controller.dispatch(GameAction.Nav.StepBack) },
                    enabled = currentIndex > 0
                ) {
                    Text("◀", fontSize = 20.sp, color = if (currentIndex > 0) colorScheme.onBackground else colorScheme.outline)
                }

                Text(
                    text = "$currentIndex / $totalItems",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                IconButton(
                    onClick = { controller.dispatch(GameAction.Nav.StepForward) },
                    enabled = currentIndex < totalItems
                ) {
                    Text("▶", fontSize = 20.sp, color = if (currentIndex < totalItems) colorScheme.onBackground else colorScheme.outline)
                }
            }

            Text(
                text = matchState.blackName.take(8),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
        }

        // --- Info / Controls Area ---
        Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopMovesView(
                    topMoves = uiVisualState.topAnalysisMoves,
                    modifier = Modifier.weight(0.45f).fillMaxHeight()
                )
                MoveHistoryList(
                    controller = controller,
                    modifier = Modifier.weight(0.55f).fillMaxHeight()
                )
            }
        }
    }
}
