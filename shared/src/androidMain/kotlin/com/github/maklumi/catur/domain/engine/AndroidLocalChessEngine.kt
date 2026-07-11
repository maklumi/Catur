package com.github.maklumi.catur.domain.engine

import android.content.Context
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.move.BoardMove
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.*

class AndroidLocalChessEngine(private val context: Context) : ChessEngine {

    private val mutex = Mutex()
    private var sfProcess: Process? = null
    private var sfOut: BufferedWriter? = null
    private var sfIn: BufferedReader? = null

    private var lc0Process: Process? = null
    private var lc0Out: BufferedWriter? = null
    private var lc0In: BufferedReader? = null
    private var currentLc0Model: String? = null

    private val binDir = File(context.filesDir, "bin")
    private val nativeLibDir = context.applicationInfo.nativeLibraryDir

    init {
        deployBinaries()
    }

    private fun deployBinaries() {
        if (!binDir.exists()) binDir.mkdirs()

        // Maia Models
        listOf("1300", "1500", "1700", "1900").forEach { v ->
            val baseName = "maia-$v.pb"
            val target = File(binDir, baseName)
            if (!target.exists()) {
                listOf("$baseName.gz", baseName).any { copyAsset(it, target) }
            }
        }
    }

    private fun copyAsset(assetPath: String, targetFile: File): Boolean {
        return try {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun findNativeLib(name: String): File? {
        val root = File(nativeLibDir)
        val direct = File(root, name)
        if (direct.exists()) return direct

        // Deep search in lib subdirectories (some Android versions use arm/ vs arm64/)
        return root.walkTopDown().find { it.name == name }
    }

    private fun startStockfish() {
        if (sfProcess?.isAlive == true) return
        try {
            val sfFile = findNativeLib("libstockfish.so") ?: return
            
            val p = ProcessBuilder(sfFile.absolutePath)
                .redirectErrorStream(true)
                .start()
            sfProcess = p
            sfOut = p.outputStream.bufferedWriter()
            sfIn = p.inputStream.bufferedReader()
            
            sfOut?.write("uci\n")
            sfOut?.flush()
            readUntil(sfIn, "uciok")
        } catch (_: Exception) { }
    }

    private fun startLc0(model: String) {
        val modelBase = when (model) {
            "maia3-3m-ablation" -> "maia-1300.pb"
            "maia3-5m" -> "maia-1500.pb"
            "maia3-23m" -> "maia-1700.pb"
            "maia3-79m" -> "maia-1900.pb"
            else -> "maia-1500.pb"
        }
        val modelFile = modelBase

        if (lc0Process?.isAlive == true && currentLc0Model == modelFile) return
        
        stopLc0()
        
        try {
            val lc0File = findNativeLib("liblc0.so") ?: return

            val p = ProcessBuilder(
                lc0File.absolutePath,
                "--weights=${File(binDir, modelFile).absolutePath}"
            )
                .redirectErrorStream(true)
                .start()
            lc0Process = p
            lc0Out = p.outputStream.bufferedWriter()
            lc0In = p.inputStream.bufferedReader()
            currentLc0Model = modelFile
            
            lc0Out?.write("uci\n")
            lc0Out?.flush()
            readUntil(lc0In, "uciok")
        } catch (_: Exception) { }
    }

    private fun stopLc0() {
        lc0Process?.destroy()
        lc0Process = null
        lc0Out = null
        lc0In = null
        currentLc0Model = null
    }

    private fun readUntil(reader: BufferedReader?, target: String): String? {
        try {
            while (true) {
                val line = reader?.readLine() ?: break
                if (line.contains(target)) return line
            }
        } catch (_: Exception) { }
        return null
    }

    private fun getPositionCommand(moves: List<String>, fen: String?): String {
        return when {
            fen != null -> if (moves.isEmpty()) "position fen $fen\n" else "position fen $fen moves ${moves.joinToString(" ")}\n"
            else -> if (moves.isEmpty()) "position startpos\n" else "position startpos moves ${moves.joinToString(" ")}\n"
        }
    }

    override suspend fun getBestMove(moves: List<String>, model: String, fen: String?): String? = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (model == "stockfish") {
                startStockfish()
                val out = sfOut ?: return@withLock null
                val inp = sfIn ?: return@withLock null
                
                out.write(getPositionCommand(moves, fen))
                out.write("go movetime 1000\n")
                out.flush()
                val line = readUntil(inp, "bestmove")
                line?.split(" ")?.getOrNull(1)
            } else {
                startLc0(model)
                val out = lc0Out ?: return@withLock null
                val inp = lc0In ?: return@withLock null
                
                out.write(getPositionCommand(moves, fen))
                out.write("go movetime 1000\n")
                out.flush()
                val line = readUntil(inp, "bestmove")
                line?.split(" ")?.getOrNull(1)
            }
        }
    }

    override suspend fun getTopMoves(moves: List<String>, model: String, count: Int, fen: String?): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        mutex.withLock {
            startStockfish()
            val out = sfOut ?: return@withLock emptyList()
            val inp = sfIn ?: return@withLock emptyList()

            val sideToMove = if (fen != null) {
                val parts = fen.split(" ")
                if (parts.size > 1) parts[1] else "w"
            } else if (moves.size % 2 == 0) "w" else "b"

            out.write(getPositionCommand(moves, fen))
            out.write("setoption name MultiPV value $count\n")
            out.write("go depth 10\n")
            out.flush()

            val topMoves = mutableMapOf<Int, Pair<String, Int>>()
            while (true) {
                val line = inp.readLine() ?: break
                if (line.contains("multipv")) {
                    try {
                        val parts = line.split(" ")
                        val pvIdx = parts[parts.indexOf("multipv") + 1].toInt()
                        
                        var score = 0
                        if (line.contains("score cp")) {
                            val rawScore = parts[parts.indexOf("cp") + 1].toInt()
                            score = if (sideToMove == "b") -rawScore else rawScore
                        } else if (line.contains("score mate")) {
                            val mateIn = parts[parts.indexOf("mate") + 1].toInt()
                            val mateScore = if (mateIn > 0) 10000 else -10000
                            score = if (sideToMove == "b") -mateScore else mateScore
                        }
                        
                        val move = parts[parts.indexOf("pv") + 1]
                        topMoves[pvIdx] = move to score
                    } catch (_: Exception) {}
                }
                if (line.startsWith("bestmove")) break
            }
            out.write("setoption name MultiPV value 1\n")
            out.flush()
            topMoves.values.toList()
        }
    }

    override suspend fun evaluate(moves: List<String>, model: String, fen: String?): Int = withContext(Dispatchers.IO) {
        mutex.withLock {
            startStockfish()
            val out = sfOut ?: return@withLock 0
            val inp = sfIn ?: return@withLock 0
            
            val sideToMove = if (fen != null) {
                val parts = fen.split(" ")
                if (parts.size > 1) parts[1] else "w"
            } else if (moves.size % 2 == 0) "w" else "b"

            out.write(getPositionCommand(moves, fen))
            out.write("go depth 10\n")
            out.flush()

            var score = 0
            while (true) {
                val line = inp.readLine() ?: break
                if (line.contains("score cp")) {
                    val parts = line.split(" ")
                    val rawScore = parts[parts.indexOf("cp") + 1].toInt()
                    score = if (sideToMove == "b") -rawScore else rawScore
                } else if (line.contains("score mate")) {
                    val parts = line.split(" ")
                    val mateIn = parts[parts.indexOf("mate") + 1].toInt()
                    val mateScore = if (mateIn > 0) 10000 else -10000
                    score = if (sideToMove == "b") -mateScore else mateScore
                }
                if (line.startsWith("bestmove")) break
            }
            score
        }
    }

    override fun stop() {
        sfProcess?.destroy()
        sfProcess = null
        sfOut = null
        sfIn = null
        stopLc0()
    }
}
