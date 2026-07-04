package com.github.maklumi.catur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.maklumi.catur.domain.chess.move.BoardMove
import com.github.maklumi.catur.ui.components.PieceImage

@Composable
fun PromotionDialog(
    moves: List<BoardMove>,
    onChoice: (BoardMove) -> Unit,
    onCancel: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moves.forEach { move ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colorScheme.surfaceVariant)
                            .clickable { onChoice(move) },
                        contentAlignment = Alignment.Center
                    ) {
                        PieceImage(
                            piece = move.move.piece,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }
}
