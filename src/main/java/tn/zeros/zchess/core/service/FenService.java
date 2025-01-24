package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;

public class FenService {
    private static final String FEN_DELIMITER = " ";
    private static final String FEN_SEPARATOR = "/";

    public static String generateFEN(BoardState state) {
        StringBuilder fen = new StringBuilder();

        // Piece placement
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                Piece piece = state.getPieceAt(square);

                if (piece == Piece.NONE) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(piece.getSymbol());
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

    private static String squareToAlgebraic(int square) {
        char file = (char) ('a' + (square % 8));
        int rank = (square / 8) + 1;
        return "" + file + rank;
    }
}
