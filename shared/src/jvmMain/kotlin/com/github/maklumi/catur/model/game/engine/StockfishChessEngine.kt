package com.github.maklumi.catur.model.game.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader

class StockfishChessEngine : ChessEngine {
    private var process: Process? = null
    private var output: BufferedWriter? = null
    private var input: BufferedReader? = null
    private val mutex = Mutex()

    private fun start() {
        if (process?.isAlive == true) return
        try {
            val p = ProcessBuilder("E:\\stockfish-windows-x86-64-avx2\\stockfish\\stockfish-windows-x86-64-avx2.exe")
                .redirectErrorStream(true)
                .start()
            process = p
            output = p.outputStream.bufferedWriter()
            input = BufferedReader(InputStreamReader(p.inputStream))
            
            val out = output!!
            val inp = input!!

            out.write("uci\n")
            out.flush()
            while (true) {
                val line = inp.readLine() ?: break
                if (line.contains("uciok")) break
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    override suspend fun getBestMove(moves: List<String>, model: String, fen: String?): String? = null // Not used for eval

    override suspend fun evaluate(moves: List<String>, fen: String?): Int = withContext(Dispatchers.IO) {
        mutex.withLock {
            start()
            val out = output ?: return@withLock 0
            val inp = input ?: return@withLock 0

            val sideToMove = when {
                fen != null -> {
                    // Check FEN part 2 (active color)
                    val parts = fen.split(" ")
                    if (parts.size > 1) parts[1] else "w"
                }
                else -> if (moves.size % 2 == 0) "w" else "b"
            }

            val posCmd = when {
                fen != null -> if (moves.isEmpty()) "position fen $fen\n" else "position fen $fen moves ${moves.joinToString(" ")}\n"
                else -> if (moves.isEmpty()) "position startpos\n" else "position startpos moves ${moves.joinToString(" ")}\n"
            }
            out.write(posCmd)
            out.write("go depth 10\n")
            out.flush()

            var latestScore = 0
            while (true) {
                val line = inp.readLine() ?: break
                if (line.contains("score cp")) {
                    val parts = line.split("score cp ")
                    if (parts.size > 1) {
                        val rawScore = parts[1].split(" ").getOrNull(0)?.toIntOrNull() ?: latestScore
                        latestScore = if (sideToMove == "b") -rawScore else rawScore
                    }
                } else if (line.contains("score mate")) {
                    val parts = line.split("score mate ")
                    if (parts.size > 1) {
                        val mateIn = parts[1].split(" ").getOrNull(0)?.toIntOrNull() ?: 0
                        val mateScore = if (mateIn > 0) 10000 else -10000
                        latestScore = if (sideToMove == "b") -mateScore else mateScore
                    }
                }
                if (line.startsWith("bestmove")) break
            }
            latestScore
        }
    }

    override fun stop() {
        try {
            output?.write("quit\n")
            output?.flush()
        } catch (_: Exception) {}
        process?.destroy()
    }
}
