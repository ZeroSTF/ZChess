# ![ZChess Logo](src/main/resources/icons/app-icon-32x32.png) ZChess

A modern JavaFX chess application featuring advanced bitboard implementation with magic move generation, AI
capabilities, and future networking support.

## Features

- **Bitboard Engine**
    - 64-bit bitboard representation
    - Magic bitboards for sliding piece move generation
    - Efficient move generation using bitwise operations
    - Legal move validation with check detection
    - FEN import/export support

- **JavaFX UI**
    - Interactive chess board with piece dragging
    - Move highlighting
    - Game history tracking
    - Move undo/redo functionality

- **Future Development**
    - Chess engine with AI opponents (planned)
    - Network multiplayer functionality (planned)
    - Tournament mode (planned)
    - Multiple theme support (planned)

## Prerequisites

- Java 17 or higher

## Installation & Running

## Method 1: Pre-built Installer (Recommended)

Download the latest installer from the [Releases](https://github.com/ZeroSTF/ZChess/releases) page.

## Method 2: Build from Source

1. **Clone the repository:**

```bash
git clone https://github.com/ZeroSTF/ZChess.git
```

2. **Navigate to the project directory:**

```bash
cd ZChess
```

3. **Build the application:**

```bash
./mvnw clean package
```

4. **Run the application:**

```bash
./mvnw javafx:run
```

## Usage

1. **Starting a Game**

- Choose game mode (Local vs Human, vs AI)
- Select time controls (if enabled)
- Pick board theme from settings

2. **Making Moves**

- Click and drag pieces to valid squares
- Automatic promotion to Queen (right-click for other promotions)
- Highlighted squares show possible moves

3. **Game Controls**

- `Ctrl+Z` / `Ctrl+Y` for undo/redo
- `Space` to flip board perspective
- `Esc` to return to main menu

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

Please ensure code follows existing style and includes appropriate tests.

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Acknowledgments

- Magic bitboard implementation inspired by [Chess Programming Wiki](https://www.chessprogramming.org/Magic_Bitboards)
- JavaFX documentation from [FXExperience](https://openjfx.io/javadoc/23/)
- Test positions from [Chess Programming Perft Results](https://www.chessprogramming.org/Perft_Results)