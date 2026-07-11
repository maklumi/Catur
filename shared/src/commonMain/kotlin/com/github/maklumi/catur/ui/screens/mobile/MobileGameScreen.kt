package com.github.maklumi.catur.ui.screens.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.piece.King
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.state.model.*
import com.github.maklumi.catur.ui.components.*

@Composable
internal fun MobileGameScreen(
    controller: GameController,
    boardState: BoardState,
    matchState: MatchState,
    clockState: ClockState,
    uiVisualState: UiVisualState,
) {
    val snapshot = boardState.currentSnapshot
    val colorScheme = MaterialTheme.colorScheme
    val totalItems = boardState.snapshots.size - 1
    val currentIndex = boardState.currentIndex

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

            IconButton(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.SETTINGS)) }) {
                Text("⋮", fontSize = 24.sp, color = colorScheme.onBackground)
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
                    text = "${snapshot.activeColor.name.lowercase().replaceFirstChar { it.uppercase() }} to move",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
            }
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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = matchState.whiteName.take(8),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )

            Text(text = "±", fontSize = 28.sp, color = colorScheme.onBackground)

            val time = if (boardState.isBoardFlipped) clockState.whiteTimeMillis else clockState.blackTimeMillis
            Text(
                text = formatTime(time),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (time < 30000) colorScheme.error else colorScheme.onBackground
            )
        }

        // --- Info / Controls Area ---
        Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)) {
            MoveHistoryList(controller = controller, modifier = Modifier.weight(1f))

            if (snapshot.status == GameStatus.ONGOING) {
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
