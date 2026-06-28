package com.github.maklumi.catur.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.model.game.controller.GameController
import com.github.maklumi.catur.model.game.state.BoardState
import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.model.game.state.PgnUtils
import com.github.maklumi.catur.model.game.state.PuzzleState
import com.github.maklumi.catur.model.piece.Piece

@Composable
fun CapturedPiecesView(pieces: List<Piece>, imbalance: Int = 0) {
    val sortedPieces = pieces.sortedBy { it.value }
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
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
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MoveHistoryList(
    controller: GameController,
    modifier: Modifier = Modifier
) {
    val boardState by controller.boardState.collectAsState(BoardState())
    val clipboardManager = LocalClipboardManager.current
    val colorScheme = MaterialTheme.colorScheme
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Move History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            
            Button(
                onClick = {
                    val currentState = controller.state.value
                    val pgn = PgnUtils.generatePgn(currentState)
                    println(pgn)
                    clipboardManager.setText(AnnotatedString(pgn))
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.secondaryContainer,
                    contentColor = colorScheme.onSecondaryContainer
                )
            ) {
                Text("Copy PGN", fontSize = 10.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start",
            fontWeight = if (boardState.currentIndex == 0) FontWeight.Bold else FontWeight.Normal,
            color = if (boardState.currentIndex == 0) colorScheme.primary else colorScheme.onBackground,
            modifier = Modifier
                .clickable { controller.dispatch(GameAction.JumpToHistory(0)) }
                .padding(vertical = 4.dp)
        )

        val historySnapshots = boardState.snapshots.drop(1)
        for (i in historySnapshots.indices step 2) {
            val turnNumber = i / 2 + 1
            val whiteMoveIdx = i + 1
            val blackMoveIdx = i + 2
            
            val whiteMove = historySnapshots[i].notation ?: ""
            val blackMove = historySnapshots.getOrNull(i + 1)?.notation ?: ""

            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = "$turnNumber. ",
                    color = colorScheme.outline,
                    modifier = Modifier.width(32.dp)
                )
                Text(
                    text = whiteMove,
                    fontWeight = if (boardState.currentIndex == whiteMoveIdx) FontWeight.Bold else FontWeight.Normal,
                    color = if (boardState.currentIndex == whiteMoveIdx) colorScheme.primary else colorScheme.onBackground,
                    modifier = Modifier
                        .width(64.dp)
                        .clickable { controller.dispatch(GameAction.JumpToHistory(whiteMoveIdx)) }
                )
                if (blackMove.isNotEmpty()) {
                    Text(
                        text = blackMove,
                        fontWeight = if (boardState.currentIndex == blackMoveIdx) FontWeight.Bold else FontWeight.Normal,
                        color = if (boardState.currentIndex == blackMoveIdx) colorScheme.primary else colorScheme.onBackground,
                        modifier = Modifier
                            .width(64.dp)
                            .clickable { controller.dispatch(GameAction.JumpToHistory(blackMoveIdx)) }
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
    val colorScheme = MaterialTheme.colorScheme

    Column {
        Text(text = "Engine Level", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colorScheme.onBackground)
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
                        .background(if (currentModel == model) colorScheme.primary else colorScheme.outlineVariant, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, fontSize = 14.sp, color = colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun PuzzleList(
    controller: GameController,
    modifier: Modifier = Modifier
) {
    val puzzleState by controller.puzzleState.collectAsState(PuzzleState())
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Puzzles",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
            color = colorScheme.onBackground
        )

        puzzleState.puzzles.forEachIndexed { index, puzzle ->
            val isSelected = puzzleState.currentPuzzleIndex == index
            val isCompleted = puzzle.isCompleted
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { controller.dispatch(GameAction.SelectPuzzle(index)) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}.  ${puzzle.title}",
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) colorScheme.primary else colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                if (isCompleted) {
                    Text(
                        text = "✓",
                        color = Color(0xFF4CAF50), // Material Green
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
