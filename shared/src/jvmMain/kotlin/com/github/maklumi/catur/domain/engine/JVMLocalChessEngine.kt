package com.github.maklumi.catur.domain.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.*

import kotlin.time.Duration.Companion.milliseconds

class JVMLocalChessEngine : ChessEngine {

    private val mutex = Mutex()
    private var sfProcess: Process? = null
    private var sfOut: PrintWriter? = null
    private var sfIn: BufferedReader? = null

    private var lc0Process: Process? = null
    private var lc0Out: PrintWriter? = null
    private var lc0In: BufferedReader? = null
    private var currentLc0Model: String? = null

    private val binDir = File("bin").apply { if (!exists()) mkdirs() }

    init {
        deployBinaries()
    }

    private fun deployBinaries() {
        val sfTarget = File(binDir, "stockfish18.exe")
        val lc0Target = File(binDir, "lc0.exe")
        val dlls = listOf("dnnl.dll", "mimalloc-override.dll", "mimalloc-redirect.dll").map { File(binDir, it) }
        val models = listOf("1300", "1500", "1700", "1900").map { File(binDir, "maia-$it.pb.gz") }

        if (sfTarget.exists() && lc0Target.exists() && dlls.all { it.exists() } && models.all { it.exists() }) {
            return
        }

        if (!sfTarget.exists()) extractResource("/bin/stockfish18.exe", sfTarget)
        if (!lc0Target.exists()) extractResource("/bin/lc0.exe", lc0Target)
        
        listOf("dnnl.dll", "mimalloc-override.dll", "mimalloc-redirect.dll").forEach { name ->
            val target = File(binDir, name)
            if (!target.exists()) extractResource("/bin/$name", target)
        }
        
        listOf("1300", "1500", "1700", "1900").forEach { v ->
            val modelName = "maia-$v.pb.gz"
            val target = File(binDir, modelName)
            if (!target.exists()) extractResource("/bin/$modelName", target)
        }
    }

    private fun extractResource(resourcePath: String, targetFile: File) {
        try {
            val stream = this::class.java.getResourceAsStream(resourcePath) ?: return
            stream.use { input -> FileOutputStream(targetFile).use { output -> input.copyTo(output) } }
            targetFile.setExecutable(true)
        } catch (_: Exception) { }
    }

    private fun startStockfish() {
        if (sfProcess?.isAlive == true) return
        try {
            val sfFile = File(binDir, "stockfish18.exe")
            val pb = ProcessBuilder(sfFile.absolutePath)
                .directory(binDir.absoluteFile)
                .redirectErrorStream(true)
            
            val env = pb.environment()
            val pathKey = env.keys.find { it.equals("PATH", ignoreCase = true) } ?: "PATH"
            env[pathKey] = binDir.absolutePath + File.pathSeparator + (env[pathKey] ?: "")
            
            val p = pb.start()
            sfProcess = p
            sfOut = PrintWriter(BufferedWriter(OutputStreamWriter(p.outputStream)), true)
            sfIn = p.inputStream.bufferedReader()
            
            sfOut?.println("uci")
            sfOut?.println("isready")
            readUntil(sfIn, "uciok")
            readUntil(sfIn, "readyok")
        } catch (_: Exception) {
            sfProcess = null
        }
    }

    private fun startLc0(model: String) {
        val modelFile = when (model) {
            "maia-1300" -> "maia-1300.pb.gz"
            "maia-1500" -> "maia-1500.pb.gz"
            "maia-1700" -> "maia-1700.pb.gz"
            "maia-1900" -> "maia-1900.pb.gz"
            else -> "maia-1500.pb.gz"
        }

        if (lc0Process?.isAlive == true && currentLc0Model == modelFile) return
        stopLc0()
        
        try {
            val lc0File = File(binDir, "lc0.exe")
            val weightsFile = File(binDir, modelFile)
            
            if (!lc0File.exists()) return

            val pb = ProcessBuilder(lc0File.absolutePath, "--weights=${weightsFile.absolutePath}", "--backend=blas")
                .directory(binDir.absoluteFile) 
                .redirectErrorStream(true)
            
            val env = pb.environment()
            val pathKey = env.keys.find { it.equals("PATH", ignoreCase = true) } ?: "PATH"
            env[pathKey] = binDir.absolutePath + File.pathSeparator + (env[pathKey] ?: "")
            
            val p = pb.start()
            lc0Process = p
            lc0Out = PrintWriter(BufferedWriter(OutputStreamWriter(p.outputStream)), true)
            lc0In = p.inputStream.bufferedReader()
            currentLc0Model = modelFile
            
            lc0Out?.println("uci")
            lc0Out?.println("isready")
            
            if (readUntil(lc0In, "uciok") == null || readUntil(lc0In, "readyok") == null) {
                stopLc0()
            }
        } catch (_: Exception) {
            lc0Process = null
        }
    }

    private fun stopLc0() {
        try {
            lc0Out?.println("quit")
            lc0Process?.destroy()
        } catch (_: Exception) {}
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
        val cleanMoves = moves.filter { it.isNotBlank() }
        return when {
            fen != null -> if (cleanMoves.isEmpty()) "position fen $fen" else "position fen $fen moves ${cleanMoves.joinToString(" ")}"
            else -> if (cleanMoves.isEmpty()) "position startpos" else "position startpos moves ${cleanMoves.joinToString(" ")}"
        }
    }

    override suspend fun getBestMove(moves: List<String>, model: String, fen: String?): String? = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                if (model == "stockfish") {
                    startStockfish()
                    val out = sfOut ?: return@withLock null
                    val inp = sfIn ?: return@withLock null
                    out.println(getPositionCommand(moves, fen))
                    out.println("go movetime 1000")
                    val line = readUntil(inp, "bestmove")
                    line?.split(" ")?.getOrNull(1)
                } else {
                    startLc0(model)
                    val out = lc0Out ?: return@withLock null
                    val inp = lc0In ?: return@withLock null
                    
                    out.println(getPositionCommand(moves, fen))
                    delay(100.milliseconds)
                    out.println("go nodes 1")
                    
                    val line = readUntil(inp, "bestmove")
                    val move = line?.split(" ")?.getOrNull(1)
                    
                    if (lc0Process?.isAlive == false) {
                        stopLc0()
                    }
                    move
                }
            } catch (_: Exception) {
                sfProcess?.destroy(); sfProcess = null
                lc0Process?.destroy(); lc0Process = null
                null
            }
        }
    }

    override suspend fun getTopMoves(moves: List<String>, model: String, count: Int, fen: String?): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                startStockfish()
                val out = sfOut ?: return@withLock emptyList()
                val inp = sfIn ?: return@withLock emptyList()
                val sideToMove = if (fen != null) (fen.split(" ").getOrNull(1) ?: "w") else (if (moves.size % 2 == 0) "w" else "b")

                out.println(getPositionCommand(moves, fen))
                out.println("setoption name MultiPV value $count")
                out.println("go depth 10")

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
                                score = if (sideToMove == "b") (if (mateIn > 0) -10000 else 10000) else (if (mateIn > 0) 10000 else -10000)
                            }
                            val move = parts[parts.indexOf("pv") + 1]
                            topMoves[pvIdx] = move to score
                        } catch (_: Exception) { }
                    }
                    if (line.startsWith("bestmove")) break
                }
                out.println("setoption name MultiPV value 1")
                topMoves.values.toList()
            } catch (_: Exception) {
                sfProcess?.destroy(); sfProcess = null
                emptyList()
            }
        }
    }

    override suspend fun evaluate(moves: List<String>, model: String, fen: String?): Int = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                startStockfish()
                val out = sfOut ?: return@withLock 0
                val inp = sfIn ?: return@withLock 0
                val sideToMove = if (fen != null) (fen.split(" ").getOrNull(1) ?: "w") else (if (moves.size % 2 == 0) "w" else "b")

                out.println(getPositionCommand(moves, fen))
                out.println("go depth 10")
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
                        score = if (sideToMove == "b") (if (mateIn > 0) -10000 else 10000) else (if (mateIn > 0) 10000 else -10000)
                    }
                    if (line.startsWith("bestmove")) break
                }
                score
            } catch (_: Exception) {
                sfProcess?.destroy(); sfProcess = null
                0
            }
        }
    }

    override fun stop() {
        sfProcess?.destroy()
        stopLc0()
    }
}
