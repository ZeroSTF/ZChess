package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;

import static tn.zeros.zchess.core.util.ChessConstants.FEN_DELIMITER;
import static tn.zeros.zchess.core.util.ChessConstants.FEN_SEPARATOR;

public class FenService {
    public static String generateFEN(BoardState state) {
        StringBuilder fen = new StringBuilder();

        // Piece placement
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                int piece = state.getPieceAt(square);

                if (piece == Piece.NONE) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(Piece.getSymbol(piece));
                }
            }
            if (emptyCount > 0) fen.append(emptyCount);
            if (rank > 0) fen.append(FEN_SEPARATOR);
        }

        // Active color
        fen.append(FEN_DELIMITER)
                .append(state.isWhiteToMove()
                        ? ChessConstants.FEN_WHITE_ACTIVE
                        : ChessConstants.FEN_BLACK_ACTIVE)
                .append(FEN_DELIMITER);

        // Castling rights
        String castling = getCastlingString(state.getCastlingRights());
        fen.append(castling.isEmpty() ? "-" : castling)
                .append(FEN_DELIMITER);

        // En passant
        fen.append(state.getEnPassantSquare() == -1 ?
                        "-" : squareToAlgebraic(state.getEnPassantSquare()))
                .append(FEN_DELIMITER);

        // Move clocks
        fen.append(state.getHalfMoveClock())
                .append(FEN_DELIMITER)
                .append(state.getFullMoveNumber());

        return fen.toString();
    }

    private static String getCastlingString(int castlingRights) {
        StringBuilder sb = new StringBuilder();
        if ((castlingRights & ChessConstants.WHITE_KINGSIDE) != 0) sb.append('K');
        if ((castlingRights & ChessConstants.WHITE_QUEENSIDE) != 0) sb.append('Q');
        if ((castlingRights & ChessConstants.BLACK_KINGSIDE) != 0) sb.append('k');
        if ((castlingRights & ChessConstants.BLACK_QUEENSIDE) != 0) sb.append('q');
        return sb.toString();
    }

    public static String squareToAlgebraic(int square) {
        char file = (char) ('a' + (square & 7));
        int rank = (square >> 3) + 1;
        return "" + file + rank;
    }

    public static BoardState parseFEN(String fen, BoardState state) {
        clearBoard(state);

        String[] parts = fen.split(" ");
        if (parts.length < 4) throw new IllegalArgumentException("Invalid FEN - not enough sections");
        if (!parts[0].matches("([rnbqkpRNBQKP1-8]+/){7}[rnbqkpRNBQKP1-8]+"))
            throw new IllegalArgumentException("Invalid piece placement");

        // Set default move clocks in case they are missing
        String halfMove = parts.length > 4 ? parts[4] : "0";
        String fullMove = parts.length > 5 ? parts[5] : "1";

        parsePiecePlacement(state, parts[0]);
        parseActiveColor(state, parts[1]);
        parseCastlingRights(state, parts[2]);
        parseEnPassant(state, parts[3]);
        parseMoveClocks(state, halfMove, fullMove);

        return state;
    }

    private static void clearBoard(BoardState state) {
        for (int i = 0; i < 64; i++) {
            int piece = state.getPieceAt(i);
            if (piece != Piece.NONE) {
                state.removePiece(i, piece);
            }
        }
    }

    private static void parsePiecePlacement(BoardState state, String placement) {
        String[] ranks = placement.split("/");
        for (int rank = 7; rank >= 0; rank--) {
            int file = 0;
            for (char c : ranks[7 - rank].toCharArray()) {
                if (Character.isDigit(c)) {
                    file += Character.getNumericValue(c);
                } else {
                    int piece = Piece.fromSymbol(c);
                    state.addPiece(rank * 8 + file, piece);
                    file++;
                }
            }
        }
    }

    private static void parseActiveColor(BoardState state, String color) {
        state.setWhiteToMove(color.equals("w"));
    }

    private static void parseCastlingRights(BoardState state, String rights) {
        int castling = 0;
        if (!rights.equals("-")) {
            for (char c : rights.toCharArray()) {
                switch (c) {
                    case 'K':
                        castling |= ChessConstants.WHITE_KINGSIDE;
                        break;
                    case 'Q':
                        castling |= ChessConstants.WHITE_QUEENSIDE;
                        break;
                    case 'k':
                        castling |= ChessConstants.BLACK_KINGSIDE;
                        break;
                    case 'q':
                        castling |= ChessConstants.BLACK_QUEENSIDE;
                        break;
                }
            }
        }
        state.setCastlingRights(castling);
    }

    private static void parseEnPassant(BoardState state, String ep) {
        if (ep.equals("-")) {
            state.setEnPassantSquare(-1);
        } else {
            int file = ep.charAt(0) - 'a';
            int rank = ep.charAt(1) - '1';
            state.setEnPassantSquare(rank * 8 + file);
        }
    }

    private static void parseMoveClocks(BoardState state, String halfMove, String fullMove) {
        state.setHalfMoveClock(Integer.parseInt(halfMove));
        state.setFullMoveNumber(Integer.parseInt(fullMove));
    }
}
