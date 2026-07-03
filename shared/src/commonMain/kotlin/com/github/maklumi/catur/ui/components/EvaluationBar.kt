package com.github.maklumi.catur.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EvaluationBar(
    evaluation: Int?, // in centipawns
    isBoardFlipped: Boolean,
    modifier: Modifier = Modifier
) {
    // Normalize evaluation to a 0.0 to 1.0 range where 0.5 is equal
    // We'll cap it at +/- 1000 centipawns (10 pawns)
    val targetFill = remember(evaluation, isBoardFlipped) {
        val eval = evaluation ?: 0
        val capped = eval.coerceIn(-1000, 1000)
        
        // Positive eval (White winning) -> White part should be larger.
        // If not flipped (White at bottom), White part is at the bottom.
        // If flipped (Black at bottom), Black part is at the bottom.
        
        val whiteProportion = (capped + 1000) / 2000f
        if (isBoardFlipped) 1f - whiteProportion else whiteProportion
    }

    val animatedFill = remember { Animatable(0.5f) }
    
    LaunchedEffect(targetFill) {
        animatedFill.animateTo(targetFill, animationSpec = tween(500))
    }

    val backgroundColor = if (isBoardFlipped) Color.White else Color.Black
    val fillColor = if (isBoardFlipped) Color.Black else Color.White
    
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
    ) {
        // The "fill" part (represents the bottom color)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(animatedFill.value)
                .align(Alignment.BottomCenter)
                .background(fillColor)
        )

        // Text display
        val evalText = remember(evaluation) {
            if (evaluation == null) "0.0" else {
                val score = evaluation / 100.0
                val sign = if (score > 0) "+" else ""
                val s = score.toString()
                val dotIdx = s.indexOf('.')
                val formatted = if (dotIdx == -1) s else s.substring(0, (dotIdx + 2).coerceAtMost(s.length))
                "$sign$formatted"
            }
        }

        Text(
            text = evalText,
            color = if (animatedFill.value > 0.5f) Color.DarkGray else Color.LightGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(if (isBoardFlipped) Alignment.TopCenter else Alignment.BottomCenter)
                .padding(vertical = 4.dp)
        )
    }
}
