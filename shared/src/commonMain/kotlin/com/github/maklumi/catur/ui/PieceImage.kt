package com.github.maklumi.catur.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import catur.shared.generated.resources.*
import com.github.maklumi.catur.model.piece.Piece
import org.jetbrains.compose.resources.painterResource

@Composable
fun PieceImage(piece: Piece, modifier: Modifier = Modifier) {
    val resource = when (piece.resName) {
        "dubrovny_bb" -> Res.drawable.dubrovny_bb
        "dubrovny_bk" -> Res.drawable.dubrovny_bk
        "dubrovny_bn" -> Res.drawable.dubrovny_bn
        "dubrovny_bp" -> Res.drawable.dubrovny_bp
        "dubrovny_bq" -> Res.drawable.dubrovny_bq
        "dubrovny_br" -> Res.drawable.dubrovny_br
        "dubrovny_wb" -> Res.drawable.dubrovny_wb
        "dubrovny_wk" -> Res.drawable.dubrovny_wk
        "dubrovny_wn" -> Res.drawable.dubrovny_wn
        "dubrovny_wp" -> Res.drawable.dubrovny_wp
        "dubrovny_wq" -> Res.drawable.dubrovny_wq
        "dubrovny_wr" -> Res.drawable.dubrovny_wr
        else -> Res.drawable.compose_multiplatform
    }
    Image(
        painter = painterResource(resource),
        contentDescription = piece.symbol,
        modifier = modifier
    )
}
