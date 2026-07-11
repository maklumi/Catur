package com.github.maklumi.catur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.getPlatform
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.state.model.GameAction
import com.github.maklumi.catur.state.model.PuzzleState
import com.github.maklumi.catur.state.model.Screen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun PuzzleListView(
    controller: GameController
) {
    val puzzleState by controller.puzzleState.collectAsState(PuzzleState())
    val puzzles = puzzleState.puzzles
    
    val colorScheme = MaterialTheme.colorScheme
    val isMobile = remember { getPlatform().isMobile }

    // Prefetch colors
    val surfaceVariant = colorScheme.surfaceVariant
    val onSurfaceVariant = colorScheme.onSurfaceVariant
    val primary = colorScheme.primary
    val outline = colorScheme.outline

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(if (isMobile) 16.dp else 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = if (isMobile) 16.dp else 32.dp)
            ) {
                Button(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.MENU)) }) {
                    Text(if (isMobile) "Back" else "Back to Menu")
                }
                Spacer(modifier = Modifier.width(if (isMobile) 12.dp else 24.dp))
                Text(
                    text = "Puzzles (${puzzles.size})",
                    fontSize = if (isMobile) 24.sp else 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
            }

            if (puzzles.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading puzzles...", color = outline)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = if (isMobile) GridCells.Adaptive(80.dp) else GridCells.Adaptive(250.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(
                        count = puzzles.size,
                        key = { it }
                    ) { index ->
                        val puzzle = puzzles[index]
                        PuzzleItem(
                            index = index,
                            puzzle = puzzle,
                            isMobile = isMobile,
                            surfaceVariant = surfaceVariant,
                            onSurfaceVariant = onSurfaceVariant,
                            primary = primary,
                            outline = outline,
                            onClick = { controller.dispatch(GameAction.Puzzles.SelectPuzzle(index)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PuzzleItem(
    index: Int,
    puzzle: com.github.maklumi.catur.domain.puzzle.Puzzle,
    isMobile: Boolean,
    surfaceVariant: Color,
    onSurfaceVariant: Color,
    primary: Color,
    outline: Color,
    onClick: () -> Unit
) {
    val isCompleted = puzzle.isCompleted

    if (isMobile) {
        val completedColor = remember { Color(0xFF4CAF50) }
        val completedBg = remember { completedColor.copy(alpha = 0.1f) }
        
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isCompleted) completedBg else surfaceVariant)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${index + 1}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) completedColor else onSurfaceVariant
            )
            if (isCompleted) {
                Text(
                    text = "✓",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = completedColor,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                )
            }
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = primary.copy(alpha = 0.5f),
                    modifier = Modifier.width(40.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = puzzle.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = if (isCompleted) "Solved ✓" else "Not solved",
                        fontSize = 12.sp,
                        color = if (isCompleted) Color(0xFF4CAF50) else outline
                    )
                }
            }
        }
    }
}
