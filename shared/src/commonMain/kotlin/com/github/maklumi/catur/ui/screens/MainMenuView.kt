package com.github.maklumi.catur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.state.model.GameAction
import com.github.maklumi.catur.state.model.Screen

@Composable
fun MainMenuView(
    onAction: (GameAction) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().background(colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CATUR",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = colorScheme.primary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        MenuButton(
            text = "Play",
            description = "Local match or vs Computer",
            onClick = { onAction(GameAction.Nav.NavigateTo(Screen.PLAY_SELECTION)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Puzzles",
            description = "Train with chess puzzles",
            onClick = { onAction(GameAction.Nav.NavigateTo(Screen.PUZZLES)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Analysis",
            description = "Sandbox move analysis",
            onClick = { onAction(GameAction.Flow.StartAnalysis) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Settings",
            description = "Configure themes and preferences",
            onClick = { onAction(GameAction.Nav.NavigateTo(Screen.SETTINGS)) }
        )
    }
}

@Composable
private fun MenuButton(
    text: String,
    description: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .width(300.dp)
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
