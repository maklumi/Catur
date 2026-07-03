package com.github.maklumi.catur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.state.model.GameAction
import com.github.maklumi.catur.state.model.Screen
import com.github.maklumi.catur.state.model.UiVisualState
import com.github.maklumi.catur.ui.components.BoardThemeSelector

@Composable
fun SettingsView(
    controller: GameController
) {
    val uiVisualState by controller.uiVisualState.collectAsState(UiVisualState())
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().background(colorScheme.background).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Column(
            modifier = Modifier.width(400.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            BoardThemeSelector(
                currentTheme = uiVisualState.boardTheme,
                onThemeChange = { controller.dispatch(GameAction.Ui.SetBoardTheme(it)) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Sound Effects", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onBackground)
                    Text(text = "Play sounds on moves and captures", fontSize = 14.sp, color = colorScheme.outline)
                }
                Switch(
                    checked = uiVisualState.isSoundEnabled,
                    onCheckedChange = { controller.dispatch(GameAction.Ui.SetSoundEnabled(it)) }
                )
            }
            
            Text("Other settings coming soon...", color = colorScheme.outline)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.MENU)) }) {
            Text("Back to Menu")
        }
    }
}
