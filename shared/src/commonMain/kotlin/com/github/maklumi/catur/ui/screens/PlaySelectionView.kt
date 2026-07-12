package com.github.maklumi.catur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.domain.chess.piece.PieceColor
import com.github.maklumi.catur.state.model.GameAction
import com.github.maklumi.catur.state.model.Screen

@Composable
fun PlaySelectionView(
    onAction: (GameAction) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedColor by remember { mutableStateOf(PieceColor.WHITE) }

    Column(
        modifier = Modifier.fillMaxSize().background(colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Mode",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        PlayOptionButton(
            onClick = { onAction(GameAction.Flow.StartLocalGame) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Play vs Computer",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { selectedColor = PieceColor.WHITE },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedColor == PieceColor.WHITE) colorScheme.primary else colorScheme.surfaceVariant,
                    contentColor = if (selectedColor == PieceColor.WHITE) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                )
            ) { Text("Play as White") }
            Button(
                onClick = { selectedColor = PieceColor.BLACK },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedColor == PieceColor.BLACK) colorScheme.primary else colorScheme.surfaceVariant,
                    contentColor = if (selectedColor == PieceColor.BLACK) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                )
            ) { Text("Play as Black") }
        }

        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            val engines = listOf(
                "maia-1300" to "Maia 1300",
                "maia-1500" to "Maia 1500",
                "maia-1700" to "Maia 1700",
                "maia-1900" to "Maia 1900"
            )

            engines.forEach { (model, label) ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = 100.dp, height = 60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorScheme.secondaryContainer)
                        .clickable {
                            onAction(
                                GameAction.Flow.StartComputerGame(
                                    model,
                                    selectedColor
                                )
                            )
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = { onAction(GameAction.Nav.NavigateTo(Screen.MENU)) }) {
            Text("Back to Menu")
        }
    }
}

@Composable
private fun PlayOptionButton(
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .width(350.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "New Game Local",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = "Player vs Player (Pass & Play)",
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
