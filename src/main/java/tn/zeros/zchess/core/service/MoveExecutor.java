package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.model.Piece;

import static tn.zeros.zchess.core.util.ChessConstants.*;

public class MoveExecutor {
    public static MoveUndoInfo makeMove(BoardState state, int move) {
        // Save pre-move state
        MoveUndoInfo undoInfo = new MoveUndoInfo(move, state.getCastlingRights(), state.getEnPassantSquare(), state.getHalfMoveClock());

        // Handle special moves
        if (Move.isCastling(move)) {
            executeCastling(state, move);
        } else if (Move.isEnPassant(move)) {
            executeEnPassant(state, move);
        } else {
            executeRegularMove(state, move);
        }

        // Handle promotion
        if (Move.isPromotion(move)) {
            executePromotion(state, move);
        }

        // Update game state
        updateGameState(state, move);

        return undoInfo;
    }

    private static void executeRegularMove(BoardState state, int move) {
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int piece = Move.getPiece(move);
        int capturedPiece = Move.getCapturedPiece(move);

        // Remove captured piece first
        if (capturedPiece != Piece.NONE) {
            state.removePiece(to, capturedPiece);
        }
        state.movePiece(from, to, piece);
    }

    private static void executeEnPassant(BoardState state, int move) {
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int piece = Move.getPiece(move);
        int capturedPiece = Move.getCapturedPiece(move);
        int capturedSquare = to + (Piece.isWhite(piece) ? -8 : 8);
        state.movePiece(from, Move.getTo(move), piece);
        state.removePiece(capturedSquare, capturedPiece);
    }

    private static void executeCastling(BoardState state, int move) {
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int piece = Move.getPiece(move);

        // Get rook positions FROM THE MOVE ITSELF
        int rookFrom = Move.getRookFrom(move);
        int rookTo = Move.getRookTo(move);

        state.movePiece(from, to, piece);
        int rook = Piece.isWhite(piece) ?
                Piece.makePiece(Piece.ROOK, Piece.WHITE) :
                Piece.makePiece(Piece.ROOK, Piece.BLACK);
        state.movePiece(rookFrom, rookTo, rook);
    }

    private static void executePromotion(BoardState state, int move) {
        int to = Move.getTo(move);
        int piece = Move.getPiece(move);
        int promotionPiece = Move.getPromotionPiece(move);

        state.removePiece(to, piece);
        state.addPiece(to, promotionPiece);
    }

    private static void updateGameState(BoardState state, int move) {
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int piece = Move.getPiece(move);
        int capturedPiece = Move.getCapturedPiece(move);

        updateEnPassant(state, piece, from, to);
        updateCastlingRights(state, piece, from, capturedPiece, to);
        updateMoveClocks(state, piece, capturedPiece);
        state.setWhiteToMove(!state.isWhiteToMove());
    }

    private static void updateEnPassant(BoardState state, int movedPiece, int fromSquare, int toSquare) {
        if (Piece.isPawn(movedPiece) && Math.abs(toSquare / 8 - fromSquare / 8) == 2) {
            int epSquare = fromSquare + (Piece.isWhite(movedPiece) ? 8 : -8);
            state.setEnPassantSquare(epSquare);
        } else {
            state.setEnPassantSquare(-1);
        }
    }

    public static void updateCastlingRights(BoardState state, int movedPiece, int fromSquare, int capturedPiece, int toSquare) {
        int rights = state.getCastlingRights();

        // Handle moved pieces
        if (Piece.isKing(movedPiece)) {
            rights &= Piece.isWhite(movedPiece) ?
                    ~(WHITE_KINGSIDE | WHITE_QUEENSIDE) :
                    ~(BLACK_KINGSIDE | BLACK_QUEENSIDE);
        } else if (Piece.isRook(movedPiece)) {
            if (fromSquare == 0) rights &= ~WHITE_QUEENSIDE;
            else if (fromSquare == 7) rights &= ~WHITE_KINGSIDE;
            else if (fromSquare == 56) rights &= ~BLACK_QUEENSIDE;
            else if (fromSquare == 63) rights &= ~BLACK_KINGSIDE;
        }

        // Handle captured rooks
        if (capturedPiece != Piece.NONE && Piece.isRook(capturedPiece)) {
            switch (toSquare) {
                case 0 -> rights &= ~WHITE_QUEENSIDE;
                case 7 -> rights &= ~WHITE_KINGSIDE;
                case 56 -> rights &= ~BLACK_QUEENSIDE;
                case 63 -> rights &= ~BLACK_KINGSIDE;
            }
        }

        state.setCastlingRights(rights);
    }

    private static void updateMoveClocks(BoardState state, int movedPiece, int capturedPiece) {
        if (Piece.isPawn(movedPiece) || capturedPiece != Piece.NONE) {
            state.setHalfMoveClock(0);
        } else {
            state.setHalfMoveClock(state.getHalfMoveClock() + 1);
        }

        if (!Piece.isWhite(movedPiece)) {
            state.setFullMoveNumber(state.getFullMoveNumber() + 1);
        }
    }

    public static int unmakeMove(BoardState state, MoveUndoInfo undoInfo) {
        int move = undoInfo.move();
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int piece = Move.getPiece(move);
        int capturedPiece = Move.getCapturedPiece(move);
        int promotionPiece = Move.getPromotionPiece(move);

        // Restore game state
        state.setCastlingRights(undoInfo.previousCastlingRights());
        state.setEnPassantSquare(undoInfo.previousEnPassantSquare());
        state.setHalfMoveClock(undoInfo.previousHalfMoveClock());
        state.setWhiteToMove(!state.isWhiteToMove());
        if (!Piece.isWhite(piece)) {
            state.setFullMoveNumber(state.getFullMoveNumber() - 1);
        }

        // Reverse special moves
        if (Move.isCastling(move)) {
            unmakeCastling(state, move);
        } else if (Move.isEnPassant(move)) {
            unmakeEnPassant(state, from, to, piece, capturedPiece);
        } else if (Move.isPromotion(move)) {
            unmakePromotion(state, from, to, piece, capturedPiece, promotionPiece);
        } else {
            unmakeRegularMove(state, from, to, piece, capturedPiece);
        }
        return move;
    }

    private static void unmakeRegularMove(BoardState state, int from, int to, int piece, int capturedPiece) {
        // Move piece back
        state.movePiece(to, from, piece);

        // Restore captured piece
        if (capturedPiece != Piece.NONE) {
            state.addPiece(to, capturedPiece);
        }
    }

    private static void unmakeEnPassant(BoardState state, int from, int to, int piece, int capturedPiece) {
        int capturedSquare = to + (Piece.isWhite(piece) ? -8 : 8);
        // Move pawn back
        state.movePiece(to, from, piece);

        // Restore captured pawn
        state.addPiece(capturedSquare, capturedPiece);
    }

    private static void unmakeCastling(BoardState state, int move) {
        int kingFrom = Move.getFrom(move);
        int kingTo = Move.getTo(move);
        int piece = Move.getPiece(move);

        // Retrieve exact rook positions from the move
        int rookFrom = Move.getRookFrom(move);
        int rookTo = Move.getRookTo(move);

        // Move king back
        state.movePiece(kingTo, kingFrom, piece);

        // Move rook back using stored positions
        int rook = Piece.isWhite(piece) ?
                Piece.makePiece(Piece.ROOK, Piece.WHITE) :
                Piece.makePiece(Piece.ROOK, Piece.BLACK);
        state.movePiece(rookTo, rookFrom, rook);
    }

    private static void unmakePromotion(BoardState state, int from, int to, int piece, int capturedPiece, int promotionPiece) {
        // Remove promoted piece
        state.removePiece(to, promotionPiece);

        // Restore pawn
        state.addPiece(from, piece);

        // Restore captured piece if any
        if (capturedPiece != Piece.NONE) {
            state.addPiece(to, capturedPiece);
        }
    }
}
