package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;

public class MoveExecutor {
    public static void executeMove(BoardState state, Move move) {
        handleSpecialMoves(state, move);
        updateGameState(state, move);
    }

    private static void handleSpecialMoves(BoardState state, Move move) {
        if (move.isEnPassant()) {
            executeEnPassant(state, move);
        } else if (move.isCastling()) {
            executeCastling(state, move);
        } else {
            executeRegularMove(state, move);
        }

        if (move.isPromotion()) {
            executePromotion(state, move);
        }
    }

    private static void executeRegularMove(BoardState state, Move move) {
        // Remove captured piece first
        if (move.capturedPiece() != Piece.NONE) {
            state.removePiece(move.toSquare(), move.capturedPiece());
        }
        state.movePiece(move.fromSquare(), move.toSquare(), move.piece());
    }

    private static void executeEnPassant(BoardState state, Move move) {
        int capturedSquare = move.toSquare() + (move.piece().isWhite() ? -8 : 8);
        state.movePiece(move.fromSquare(), move.toSquare(), move.piece());
        state.removePiece(capturedSquare, move.capturedPiece());
    }

    private static void executeCastling(BoardState state, Move move) {
        int from = move.fromSquare();
        int to = move.toSquare();
        boolean kingside = (to % 8) > (from % 8);
        int rookFrom = kingside ? from + 3 : from - 4;
        int rookTo = kingside ? to - 1 : to + 1;

        state.movePiece(from, to, move.piece());
        Piece rook = move.piece().isWhite() ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
        state.movePiece(rookFrom, rookTo, rook);
    }

    private static void executePromotion(BoardState state, Move move) {
        state.removePiece(move.toSquare(), move.piece());
        state.addPiece(move.toSquare(), move.promotionPiece());
    }

    private static void updateGameState(BoardState state, Move move) {
        updateEnPassant(state, move);
        CastlingService.updateCastlingRights(state, move.piece(), move.fromSquare());
        updateMoveClocks(state, move);
    }

    private static void updateEnPassant(BoardState state, Move move) {
        if (move.piece().isPawn() && Math.abs(move.toSquare()/8 - move.fromSquare()/8) == 2) {
            int epSquare = move.fromSquare() + (move.piece().isWhite() ? 8 : -8);
            state.setEnPassantSquare(epSquare);
        } else {
            state.setEnPassantSquare(-1);
        }
    }

    private static void updateMoveClocks(BoardState state, Move move) {
        if (move.piece().isPawn() || move.capturedPiece() != Piece.NONE) {
            state.setHalfMoveClock(0);
        } else {
            state.setHalfMoveClock(state.getHalfMoveClock() + 1);
        }

        if (!move.piece().isWhite()) {
            state.setFullMoveNumber(state.getFullMoveNumber() + 1);
        }
    }
}
