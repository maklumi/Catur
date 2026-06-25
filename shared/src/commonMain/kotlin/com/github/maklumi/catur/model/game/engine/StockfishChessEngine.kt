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

    override suspend fun getBestMove(moves: List<String>, model: String): String? = null // Not used for eval

    override suspend fun evaluate(moves: List<String>): Int = withContext(Dispatchers.IO) {
        mutex.withLock {
            start()
            val out = output ?: return@withLock 0
            val inp = input ?: return@withLock 0

            val pos = if (moves.isEmpty()) "position startpos\n" else "position startpos moves ${moves.joinToString(" ")}\n"
            out.write(pos)
            out.write("go depth 10\n")
            out.flush()

            var score = 0
            while (true) {
                val line = inp.readLine() ?: break
                if (line.contains("score cp")) {
                    val parts = line.split("score cp ")
                    if (parts.size > 1) {
                        score = parts[1].split(" ").getOrNull(0)?.toIntOrNull() ?: score
                    }
                }
                if (line.startsWith("bestmove")) break
            }
            score
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
