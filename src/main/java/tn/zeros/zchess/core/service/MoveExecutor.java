package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.model.Piece;

public class MoveExecutor {
    public static MoveUndoInfo makeMove(BoardState state, Move move) {
        // Save pre-move state
        MoveUndoInfo undoInfo = new MoveUndoInfo(move, state.getCastlingRights(), state.getEnPassantSquare(), state.getHalfMoveClock());

        // Handle special moves
        if (move.isCastling()) {
            executeCastling(state, move);
        } else if (move.isEnPassant()) {
            executeEnPassant(state, move);
        } else {
            executeRegularMove(state, move);
        }

        // Handle promotion
        if (move.isPromotion()) {
            executePromotion(state, move);
        }

        // Update game state
        updateGameState(state, move);

        return undoInfo;
    }

    private static void executeRegularMove(BoardState state, Move move) {
        // Remove captured piece first
        if (move.capturedPiece() != Piece.NONE) {
            state.removePiece(move.toSquare(), move.capturedPiece());
        }
        state.movePiece(move.fromSquare(), move.toSquare(), move.piece());
    }

    private static void executeEnPassant(BoardState state, Move move) {
        int capturedSquare = move.toSquare() + (Piece.isWhite(move.piece()) ? -8 : 8);
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
        int rook = Piece.isWhite(move.piece()) ? Piece.makePiece(3, 0) : Piece.makePiece(3, 1);
        state.movePiece(rookFrom, rookTo, rook);
    }

    private static void executePromotion(BoardState state, Move move) {
        state.removePiece(move.toSquare(), move.piece());
        state.addPiece(move.toSquare(), move.promotionPiece());
    }

    private static void updateGameState(BoardState state, Move move) {
        updateEnPassant(state, move);
        CastlingService.updateCastlingRights(state, move.piece(), move.fromSquare(), move.capturedPiece(), move.toSquare());
        updateMoveClocks(state, move);
        state.setWhiteToMove(!state.isWhiteToMove());
    }

    private static void updateEnPassant(BoardState state, Move move) {
        if (Piece.isPawn(move.piece()) && Math.abs(move.toSquare() / 8 - move.fromSquare() / 8) == 2) {
            int epSquare = move.fromSquare() + (Piece.isWhite(move.piece()) ? 8 : -8);
            state.setEnPassantSquare(epSquare);
        } else {
            state.setEnPassantSquare(-1);
        }
    }

    private static void updateMoveClocks(BoardState state, Move move) {
        if (Piece.isPawn(move.piece()) || move.capturedPiece() != Piece.NONE) {
            state.setHalfMoveClock(0);
        } else {
            state.setHalfMoveClock(state.getHalfMoveClock() + 1);
        }

        if (!Piece.isWhite(move.piece())) {
            state.setFullMoveNumber(state.getFullMoveNumber() + 1);
        }
    }

    public static Move unmakeMove(BoardState state, MoveUndoInfo undoInfo) {
        Move move = undoInfo.move();

        // Restore game state
        state.setCastlingRights(undoInfo.previousCastlingRights());
        state.setEnPassantSquare(undoInfo.previousEnPassantSquare());
        state.setHalfMoveClock(undoInfo.previousHalfMoveClock());
        state.setWhiteToMove(!state.isWhiteToMove());

        // Reverse special moves
        if (move.isCastling()) {
            unmakeCastling(state, move);
        } else if (move.isEnPassant()) {
            unmakeEnPassant(state, move);
        } else if (move.isPromotion()) {
            unmakePromotion(state, move);
        } else {
            unmakeRegularMove(state, move);
        }
        return move;
    }

    private static void unmakeRegularMove(BoardState state, Move move) {
        // Move piece back
        state.movePiece(move.toSquare(), move.fromSquare(), move.piece());

        // Restore captured piece
        if (move.capturedPiece() != Piece.NONE) {
            state.addPiece(move.toSquare(), move.capturedPiece());
        }
    }

    private static void unmakeEnPassant(BoardState state, Move move) {
        int capturedSquare = move.toSquare() + (Piece.isWhite(move.piece()) ? -8 : 8);

        // Move pawn back
        state.movePiece(move.toSquare(), move.fromSquare(), move.piece());

        // Restore captured pawn
        state.addPiece(capturedSquare, move.capturedPiece());
    }

    private static void unmakeCastling(BoardState state, Move move) {
        int from = move.fromSquare();
        int to = move.toSquare();
        boolean kingside = (to % 8) > (from % 8);

        // Move king back
        state.movePiece(to, from, move.piece());

        // Move rook back (calculate from move data)
        int rookFrom = kingside ? from + 3 : from - 4;
        int rookTo = kingside ? to - 1 : to + 1;
        int rook = Piece.isWhite(move.piece()) ? Piece.makePiece(3, 0) : Piece.makePiece(3, 1);
        state.movePiece(rookTo, rookFrom, rook);
    }

    private static void unmakePromotion(BoardState state, Move move) {
        // Remove promoted piece
        state.removePiece(move.toSquare(), move.promotionPiece());

        // Restore pawn
        state.addPiece(move.fromSquare(), move.piece());

        // Restore captured piece if any
        if (move.capturedPiece() != Piece.NONE) {
            state.addPiece(move.toSquare(), move.capturedPiece());
        }
    }
}
