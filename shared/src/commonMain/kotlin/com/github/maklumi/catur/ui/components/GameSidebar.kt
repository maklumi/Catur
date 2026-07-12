package com.github.maklumi.catur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.getPlatform
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.state.model.BoardState
import com.github.maklumi.catur.state.model.GameAction
import com.github.maklumi.catur.state.model.BoardTheme
import com.github.maklumi.catur.domain.chess.notation.PgnUtils
import com.github.maklumi.catur.state.model.PuzzleState
import com.github.maklumi.catur.domain.chess.piece.Piece
import com.github.maklumi.catur.domain.chess.piece.PieceColor

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
                text = "Moves",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            
            Button(
                onClick = {
                    val currentState = controller.state.value
                    val pgn = PgnUtils.generatePgn(currentState)
                    getPlatform().setClipboardText(pgn)
                },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                modifier = Modifier.height(24.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.secondaryContainer,
                    contentColor = colorScheme.onSecondaryContainer
                )
            ) {
                Text("Copy PGN", fontSize = 9.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        val historySnapshots = boardState.snapshots.drop(1)
        val startsWithBlack = boardState.snapshots.firstOrNull()?.activeColor == PieceColor.BLACK
        
        val totalMoves = historySnapshots.size
        val itemsCount = if (startsWithBlack) totalMoves + 1 else totalMoves

        for (i in 0 until itemsCount step 2) {
            val turnNumber = i / 2 + 1
            
            val whiteMoveIdx: Int
            val blackMoveIdx: Int
            val whiteMove: String
            val blackMove: String

            if (startsWithBlack && i == 0) {
                whiteMoveIdx = -1
                blackMoveIdx = 1
                whiteMove = "..."
                blackMove = historySnapshots.getOrNull(0)?.notation ?: ""
            } else {
                val offset = if (startsWithBlack) -1 else 0
                whiteMoveIdx = i + 1 + offset
                blackMoveIdx = i + 2 + offset
                whiteMove = historySnapshots.getOrNull(i + offset)?.notation ?: ""
                blackMove = historySnapshots.getOrNull(i + 1 + offset)?.notation ?: ""
            }

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
                        .clickable(enabled = whiteMoveIdx != -1) { 
                            controller.dispatch(GameAction.Nav.JumpToHistory(whiteMoveIdx)) 
                        }
                )
                if (blackMove.isNotEmpty()) {
                    Text(
                        text = blackMove,
                        fontWeight = if (boardState.currentIndex == blackMoveIdx) FontWeight.Bold else FontWeight.Normal,
                        color = if (boardState.currentIndex == blackMoveIdx) colorScheme.primary else colorScheme.onBackground,
                        modifier = Modifier
                            .width(64.dp)
                            .clickable { controller.dispatch(GameAction.Nav.JumpToHistory(blackMoveIdx)) }
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
        "maia-1300" to "Maia 1300",
        "maia-1500" to "Maia 1500",
        "maia-1700" to "Maia 1700",
        "maia-1900" to "Maia 1900"
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
                    .clickable { controller.dispatch(GameAction.Puzzles.SelectPuzzle(index)) }
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

@Composable
fun BoardThemeSelector(
    currentTheme: BoardTheme,
    onThemeChange: (BoardTheme) -> Unit
) {
    val themes = listOf(
        BoardTheme.GREEN to "Green",
        BoardTheme.WOOD to "Wood",
        BoardTheme.BLUE to "Blue",
        BoardTheme.CLASSIC to "Classic"
    )
    val colorScheme = MaterialTheme.colorScheme

    Column {
        Text(text = "Board Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            themes.forEach { (theme, label) ->
                val isSelected = currentTheme == theme
                Box(
                    modifier = Modifier
                        .size(width = 70.dp, height = 40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) colorScheme.primary else colorScheme.surfaceVariant)
                        .border(1.dp, colorScheme.outline, RoundedCornerShape(4.dp))
                        .clickable { onThemeChange(theme) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
