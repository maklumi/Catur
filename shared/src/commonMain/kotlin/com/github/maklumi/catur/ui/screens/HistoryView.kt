package com.github.maklumi.catur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.maklumi.catur.domain.chess.GameRecord
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.state.model.GameAction
import com.github.maklumi.catur.state.model.HistoryState
import com.github.maklumi.catur.state.model.Screen

@Composable
fun HistoryView(
    controller: GameController
) {
    val historyState by controller.historyState.collectAsState(HistoryState())
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().background(colorScheme.background).padding(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            Button(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.MENU)) }) {
                Text("Back to Menu")
            }
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = "Game History",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
        }

        if (historyState.pastGames.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No games recorded yet.", color = colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyState.pastGames) { record ->
                    GameRecordItem(record) {
                        controller.dispatch(GameAction.History.LoadGame(record.pgn))
                    }
                }
            }
        }
    }
}

@Composable
private fun GameRecordItem(
    record: GameRecord,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${record.white} vs ${record.black}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = record.date,
                    fontSize = 12.sp,
                    color = colorScheme.outline
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (record.opening != null) {
                        Text(
                            text = record.opening,
                            fontSize = 12.sp,
                            color = colorScheme.secondary
                        )
                    }
                    Text(
                        text = record.result,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.primary
                    )
                }
            }
        }
    }
}
