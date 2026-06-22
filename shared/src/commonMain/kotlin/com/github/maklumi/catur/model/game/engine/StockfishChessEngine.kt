package com.github.maklumi.catur.model.game.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.util.Scanner

class StockfishChessEngine : ChessEngine {
    private var process: Process? = null
    private var output: BufferedWriter? = null
    private var input: Scanner? = null

    private fun start() {
        if (process?.isAlive == true) return
        try {
            val p = ProcessBuilder("E:\\stockfish-windows-x86-64-avx2\\stockfish\\stockfish-windows-x86-64-avx2.exe")
                .redirectErrorStream(true)
                .start()
            process = p
            output = p.outputStream.bufferedWriter()
            input = Scanner(p.inputStream)
            output?.write("uci\n")
            output?.flush()
            while (input?.hasNextLine() == true) {
                if (input?.nextLine()?.contains("uciok") == true) break
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    override suspend fun getBestMove(moves: List<String>, model: String): String? = null // Not used for eval

    override suspend fun evaluate(moves: List<String>): Int = withContext(Dispatchers.IO) {
        start()
        val out = output ?: return@withContext 0
        val inp = input ?: return@withContext 0

        val pos = if (moves.isEmpty()) "position startpos\n" else "position startpos moves ${moves.joinToString(" ")}\n"
        out.write(pos)
        out.write("go depth 10\n")
        out.flush()

        var score = 0
        while (inp.hasNextLine()) {
            val line = inp.nextLine()
            if (line.contains("score cp")) {
                score = line.split("score cp ")[1].split(" ")[0].toInt()
            }
            if (line.startsWith("bestmove")) break
        }
        score
    }

    override fun stop() {
        output?.write("quit\n")
        output?.flush()
        process?.destroy()
    }
}