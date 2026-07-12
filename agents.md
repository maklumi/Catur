# Catur Agents & Engine Personalities

This document describes the various chess agents and engine personalities integrated into the **Catur** application.

## Overview

Catur uses a combination of local and remote engine logic to provide a rich playing and analysis experience. 
The architecture follows a **Clean Architecture** pattern, where the engine implementations are encapsulated
within the `domain/engine` package and exposed through the `ChessEngine` interface.

## Engine Personalities (Maia)

The primary playing agent is based on the **Maia Chess** engine. Maia is a neural network chess engine 
trained on millions of games from human players, designed to play in a human-like way at specific skill levels.

In Catur, you can select from the following Maia-based personalities:

| Personality | Model ID | Description |
|:-------------| :--- | :--- |
| **Novice** | `maia-1300` | A beginner-level agent (Maia 1300) that plays naturally but makes frequent tactical mistakes. |
| **Casual** | `maia-1500` | The default level (Maia 1500), suitable for casual players. Balanced human-like play. |
| **Club** | `maia-1700` | A stronger agent (Maia 1700), approximating the strength of a club-level human player. |
| **Expert** | `maia-1900` | The strongest Maia model (Maia 1900), providing a significant challenge for advanced players. |

### Technical Implementation

- **`JVMLocalChessEngine.kt`**: A JVM-specific implementation that bundles and extracts local Stockfish and Lc0 binaries for Desktop use.
- **`AndroidLocalChessEngine.kt`**: An Android-specific implementation that bundles engines as native libraries (`jniLibs`) for high-performance offline play.
- **`RemoteChessEngine.kt`**: A platform-independent implementation that communicates with a Python/FastAPI server (typically at `localhost:8000`). This is used for the **Web (Wasm)** target.

## Analysis Agent (Stockfish)

For move evaluation and "best move" suggestions in **Analysis Mode**, Catur uses **Stockfish**.

- **Normalization**: Evaluations are automatically normalized to a **White-relative** perspective 
- (positive means White is winning, negative means Black is winning), regardless of whose turn it is.
- **Mate Detection**: Stockfish detects forced mates and returns a high/low score (±10,000) to 
- represent the winning side.

## Logic Agents

### Opening Explorer
While not a traditional "AI agent," the **Opening Explorer** acts as a logic-based agent. It monitors 
the UCI move sequence of the current game and consults the `OpeningBook` to identify and display the name 
of the opening (e.g., *Sicilian Defense: Najdorf Variation*).

### Puzzle Logic
The `PuzzleReducer` handles the flow of tactical puzzles. It acts as an opponent by automatically playing 
the "correct" response moves from the puzzle's solution sequence, guiding the player through the training exercise.

## Future Enhancements

- **Dynamic Skill Scaling**: Automatically adjusting the engine level based on the player's performance.
- **Remote Engine Cloud**: Support for connecting to high-performance remote engines for deep analysis.
