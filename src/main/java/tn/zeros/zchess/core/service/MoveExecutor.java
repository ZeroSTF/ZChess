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
        if (move.getCapturedPiece() != Piece.NONE) {
            state.removePiece(move.getToSquare(), move.getCapturedPiece());
        }
        state.movePiece(move.getFromSquare(), move.getToSquare(), move.getPiece());
    }

    private static void executeEnPassant(BoardState state, Move move) {
        int capturedSquare = move.getToSquare() + (move.getPiece().isWhite() ? -8 : 8);
        state.movePiece(move.getFromSquare(), move.getToSquare(), move.getPiece());
        state.removePiece(capturedSquare, move.getCapturedPiece());
    }

    private static void executeCastling(BoardState state, Move move) {
        int from = move.getFromSquare();
        int to = move.getToSquare();
        boolean kingside = (to % 8) > (from % 8);
        int rookFrom = kingside ? from + 3 : from - 4;
        int rookTo = kingside ? to - 1 : to + 1;

        state.movePiece(from, to, move.getPiece());
        Piece rook = move.getPiece().isWhite() ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
        state.movePiece(rookFrom, rookTo, rook);
    }

    private static void executePromotion(BoardState state, Move move) {
        state.removePiece(move.getToSquare(), move.getPiece());
        state.addPiece(move.getToSquare(), move.getPromotionPiece());
    }

    private static void updateGameState(BoardState state, Move move) {
        updateEnPassant(state, move);
        CastlingService.updateCastlingRights(state, move.getPiece(), move.getFromSquare());
        updateMoveClocks(state, move);
    }

    private static void updateEnPassant(BoardState state, Move move) {
        if (move.getPiece().isPawn() && Math.abs(move.getToSquare()/8 - move.getFromSquare()/8) == 2) {
            int epSquare = move.getFromSquare() + (move.getPiece().isWhite() ? 8 : -8);
            state.setEnPassantSquare(epSquare);
        } else {
            state.setEnPassantSquare(-1);
        }
    }

    private static void updateMoveClocks(BoardState state, Move move) {
        if (move.getPiece().isPawn() || move.getCapturedPiece() != Piece.NONE) {
            state.setHalfMoveClock(0);
        } else {
            state.setHalfMoveClock(state.getHalfMoveClock() + 1);
        }

        if (!move.getPiece().isWhite()) {
            state.setFullMoveNumber(state.getFullMoveNumber() + 1);
        }
    }
}
