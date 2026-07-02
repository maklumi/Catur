package com.github.maklumi.catur.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.github.maklumi.catur.domain.chess.board.Position
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ChessBoardOverlay(
    from: Position,
    to: Position,
    isBoardFlipped: Boolean,
    color: Color
) {
    Canvas(modifier = Modifier.fillMaxSize().zIndex(20f)) {
        val squareWidth = size.width / 8
        val squareHeight = size.height / 8

        fun getCenter(pos: Position): androidx.compose.ui.geometry.Offset {
            val file = if (isBoardFlipped) 8 - (pos.ordinal / 8) else (pos.ordinal / 8) + 1
            val rank = if (isBoardFlipped) (pos.ordinal % 8) + 1 else 8 - (pos.ordinal % 8)
            
            return androidx.compose.ui.geometry.Offset(
                x = (file - 0.5f) * squareWidth,
                y = (rank - 0.5f) * squareHeight
            )
        }

        val start = getCenter(from)
        val end = getCenter(to)

        val strokeWidth = 8.dp.toPx()
        val headSize = 20.dp.toPx()

        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        val angle = atan2(end.y - start.y, end.x - start.x)
        val path = Path().apply {
            moveTo(end.x, end.y)
            lineTo(
                end.x - headSize * cos(angle - 0.5f),
                end.y - headSize * sin(angle - 0.5f)
            )
            lineTo(
                end.x - headSize * cos(angle + 0.5f),
                end.y - headSize * sin(angle + 0.5f)
            )
            close()
        }
        drawPath(path, color)
    }
}
