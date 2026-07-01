package com.github.maklumi.catur.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.model.game.state.Screen

@Composable
fun PlaySelectionView(
    onAction: (GameAction) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

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
            text = "New Game Local",
            description = "Player vs Player (Pass & Play)",
            onClick = { onAction(GameAction.StartLocalGame) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PlayOptionButton(
            text = "Play vs Computer",
            description = "Challenge the Maia engine",
            onClick = { onAction(GameAction.StartComputerGame) }
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = { onAction(GameAction.NavigateTo(Screen.MENU)) }) {
            Text("Back to Menu")
        }
    }
}

@Composable
private fun PlayOptionButton(
    text: String,
    description: String,
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
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
