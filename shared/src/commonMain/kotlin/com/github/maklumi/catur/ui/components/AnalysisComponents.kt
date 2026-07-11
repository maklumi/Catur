package com.github.maklumi.catur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.maklumi.catur.domain.chess.board.Position

@Composable
fun TopMovesView(
    topMoves: List<Pair<Pair<Position, Position>, Int>>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Text(
            text = "Stockfish top moves",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        if (topMoves.isEmpty()) {
            Text("...", fontSize = 12.sp, color = colorScheme.outline)
        }

        topMoves.forEachIndexed { index, (move, score) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${index + 1}.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.outline,
                        modifier = Modifier.width(20.dp)
                    )
                    Text(
                        text = "${move.first}${move.second}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                
                val scoreText = if (score >= 9000) "Mate" else if (score <= -9000) "-Mate" else {
                    val s = score / 100.0
                    (if (s > 0) "+" else "") + s.toString()
                }
                
                Text(
                    text = scoreText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (score > 0) Color(0xFF4CAF50) else if (score < 0) Color(0xFFF44336) else colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun PgnImportDialog(
    onImport: (String) -> Unit,
    onCancel: () -> Unit
) {
    var pgnText by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme

    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Import PGN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = pgnText,
                    onValueChange = { pgnText = it },
                    label = { Text("Paste PGN here") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onImport(pgnText) },
                        enabled = pgnText.isNotBlank(),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Import")
                    }
                }
            }
        }
    }
}

@Composable
fun FenImportDialog(
    onImport: (String) -> Unit,
    onCancel: () -> Unit
) {
    var fenText by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme

    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Import FEN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = fenText,
                    onValueChange = { fenText = it },
                    label = { Text("Paste FEN here") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onImport(fenText) },
                        enabled = fenText.isNotBlank(),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Import")
                    }
                }
            }
        }
    }
}
