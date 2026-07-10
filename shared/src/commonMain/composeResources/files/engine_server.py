import subprocess
import threading
from fastapi import FastAPI, Query
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import os
from typing import List, Optional
from pydantic import BaseModel

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- CONFIGURATION ---
MAIA_CWD = r"E:\maia3"
STOCKFISH_PATH = r"E:\stockfish-windows-x86-64-avx2\stockfish\stockfish-windows-x86-64-avx2.exe"

class TopMove(BaseModel):
    move: str
    score: int

class TopMovesResponse(BaseModel):
    moves: List[TopMove]

class MoveResponse(BaseModel):
    move: Optional[str]

class EvalResponse(BaseModel):
    score: int

class EngineWrapper:
    def __init__(self, command, cwd=None):
        self.lock = threading.Lock()
        self.command = command
        self.cwd = cwd
        self.process = None
        self._start()

    def _start(self):
        """Starts or restarts the engine process."""
        if self.process:
            print("Stopping current engine process...")
            self.process.terminate()
            try:
                self.process.wait(timeout=2)
            except subprocess.TimeoutExpired:
                self.process.kill()

        print(f"Starting engine with command: {' '.join(self.command)}")
        self.process = subprocess.Popen(
            self.command,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1,
            cwd=self.cwd
        )
        self._send("uci")
        self._read_until("uciok")

    def _send(self, cmd):
        self.process.stdin.write(f"{cmd}\n")
        self.process.stdin.flush()

    def _read_until(self, target):
        while True:
            line = self.process.stdout.readline()
            if not line: break
            line = line.strip()
            if target in line: return line
        return None

    def update_model_if_needed(self, model_name: str):
        """Restarts Maia only if the requested model is different from the running one."""
        with self.lock:
            if model_name == "stockfish": return # Never update maia to stockfish
            new_command = ["python", "-m", "maia3.uci", "--model", model_name]
            if self.command != new_command:
                print(f"Difficulty changed! Switching model to: {model_name}")
                self.command = new_command
                self._start()

    def get_best_move(self, moves_str: str, fen: str = None):
        with self.lock:
            if fen:
                self._send(f"position fen {fen} moves {moves_str}" if moves_str else f"position fen {fen}")
            else:
                cmd = f"position startpos moves {moves_str}" if moves_str else "position startpos"
                self._send(cmd)
            self._send("go movetime 1000")
            line = self._read_until("bestmove")
            if not line: return None
            parts = line.split()
            if len(parts) < 2: return None
            move = parts[1]
            return None if move == "(none)" else move

    def get_evaluation(self, moves_str: str, fen: str = None):
        with self.lock:
            side_to_move = "w"
            if fen:
                parts = fen.split()
                side_to_move = parts[1] if len(parts) > 1 else "w"
                self._send(f"position fen {fen} moves {moves_str}" if moves_str else f"position fen {fen}")
            else:
                side_to_move = "b" if moves_str and len(moves_str.split()) % 2 != 0 else "w"
                cmd = f"position startpos moves {moves_str}" if moves_str else "position startpos"
                self._send(cmd)

            self._send("go depth 10")
            score = 0
            while True:
                line = self.process.stdout.readline()
                if not line: break
                line = line.strip()
                if "score cp" in line:
                    parts = line.split("score cp ")
                    raw_score = int(parts[1].split()[0])
                    score = -raw_score if side_to_move == "b" else raw_score
                elif "score mate" in line:
                    parts = line.split("score mate ")
                    mate_in = int(parts[1].split()[0])
                    mate_score = 10000 if mate_in > 0 else -10000
                    score = -mate_score if side_to_move == "b" else mate_score
                if "bestmove" in line:
                    break
            return score

    def get_top_moves(self, moves_str: str, count: int = 3, fen: str = None):
        with self.lock:
            side_to_move = "w"
            if fen:
                parts = fen.split()
                side_to_move = parts[1] if len(parts) > 1 else "w"
                self._send(f"position fen {fen} moves {moves_str}" if moves_str else f"position fen {fen}")
            else:
                side_to_move = "b" if moves_str and len(moves_str.split()) % 2 != 0 else "w"
                cmd = f"position startpos moves {moves_str}" if moves_str else "position startpos"
                self._send(cmd)

            self._send(f"setoption name MultiPV value {count}")
            self._send("go depth 10")

            top_moves = {} # pv index -> (move, score)
            while True:
                line = self.process.stdout.readline()
                if not line: break
                line = line.strip()

                if "multipv" in line and " pv " in line:
                    try:
                        parts = line.split()
                        pv_idx = int(parts[parts.index("multipv") + 1])

                        # Parse score
                        score = 0
                        if "cp" in parts:
                            raw_score = int(parts[parts.index("cp") + 1])
                            score = -raw_score if side_to_move == "b" else raw_score
                        elif "mate" in parts:
                            mate_in = int(parts[parts.index("mate") + 1])
                            mate_score = 10000 if mate_in > 0 else -10000
                            score = -mate_score if side_to_move == "b" else mate_score

                        # Parse move
                        move = parts[parts.index("pv") + 1]
                        top_moves[pv_idx] = (move, score)
                    except (ValueError, IndexError):
                        continue

                if line.startswith("bestmove"):
                    break

            # Reset MultiPV to 1 for other calls
            self._send("setoption name MultiPV value 1")

            result = []
            for i in sorted(top_moves.keys()):
                m, s = top_moves[i]
                result.append(TopMove(move=m, score=s))
            return result[:count]

# Global engine instances
print("Initializing engines...")
maia = EngineWrapper(["python", "-m", "maia3.uci", "--model", "maia3-5m"], cwd=MAIA_CWD)
stockfish = EngineWrapper([STOCKFISH_PATH])
print("Engines ready!")

@app.get("/best-move", response_model=MoveResponse)
def best_move(moves: str = "", model: str = "maia3-5m", fen: str = None):
    if model == "stockfish":
        move = stockfish.get_best_move(moves, fen)
    else:
        maia.update_model_if_needed(model)
        move = maia.get_best_move(moves, fen)
    return MoveResponse(move=move)

@app.get("/evaluate", response_model=EvalResponse)
def evaluate(moves: str = "", model: str = "stockfish", fen: str = None):
    engine = stockfish if model == "stockfish" else maia
    score = engine.get_evaluation(moves, fen)
    return EvalResponse(score=score)

@app.get("/top-moves", response_model=TopMovesResponse)
def get_top_moves(moves: str = "", model: str = "stockfish", count: int = 3, fen: str = None):
    engine = stockfish if model == "stockfish" else maia
    result = engine.get_top_moves(moves, count, fen)
    return TopMovesResponse(moves=result)

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
