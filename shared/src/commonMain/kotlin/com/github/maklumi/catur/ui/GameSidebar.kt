package com.github.maklumi.catur.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.model.game.state.GameState
import com.github.maklumi.catur.model.piece.Piece

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
                        .background(
                            if (currentModel == model) Color.Green else Color.LightGray,
                            RoundedCornerShape(6.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, fontSize = 14.sp)
            }
        }
    }
}