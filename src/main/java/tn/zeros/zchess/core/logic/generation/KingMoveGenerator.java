package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class KingMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList) {
        int king = state.getPieceAt(from);

        if (king == Piece.NONE || !Piece.isKing(king)) return;

        boolean isWhite = Piece.isWhite(king);
        long friendlyPieces = state.getFriendlyPieces(isWhite);

        // Generate regular king moves
        long possibleMoves = PrecomputedMoves.getKingMoves(from, friendlyPieces);
        processMoves(state, from, king, possibleMoves, moveList);

        // Generate castling moves (separate logic)
        if (state.getCastlingRights() != 0) {
            addCastlingMoves(state, from, isWhite, moveList);
        }
    }

    private static void processMoves(BoardState state, int from, int king, long moveMask, MoveGenerator.MoveList moveList) {
        while (moveMask != 0) {
            int to = Long.numberOfTrailingZeros(moveMask);
            moveMask ^= 1L << to;

            int captured = Piece.isWhite(state.getPieceAt(to)) != Piece.isWhite(king) ?
                    state.getPieceAt(to) :
                    Piece.NONE;

            moveList.add(Move.createMove(from, to, king, captured, 0, Piece.NONE));
        }
    }

    private static void addCastlingMoves(BoardState state, int from, boolean isWhite, MoveGenerator.MoveList moveList) {
        if (from != (isWhite ? 4 : 60)) return; // King not on starting position

        int castlingRights = state.getCastlingRights();
        long allPieces = state.getAllPieces();

        // Check if king is currently in check
        if (LegalMoveFilter.isSquareAttacked(state, from, !isWhite)) {
            return;
        }

        // Kingside castling
        if ((castlingRights & (isWhite ? ChessConstants.WHITE_KINGSIDE : ChessConstants.BLACK_KINGSIDE)) != 0) {
            long kingsideMask = isWhite ? 0x60L : 0x6000000000000000L;
            if ((allPieces & kingsideMask) == 0) {
                int intermediateSquare = from + 1;
                if (!LegalMoveFilter.isSquareAttacked(state, intermediateSquare, !isWhite)) {
                    moveList.add(Move.createMove(from, from + 2, state.getPieceAt(from), Piece.NONE, Move.FLAG_CASTLING, Piece.NONE));
                }
            }
        }

        // Queenside castling
        if ((castlingRights & (isWhite ? ChessConstants.WHITE_QUEENSIDE : ChessConstants.BLACK_QUEENSIDE)) != 0) {
            long queensideMask = isWhite ? 0xEL : 0xE00000000000000L;
            if ((allPieces & queensideMask) == 0) {
                int intermediateSquare = from - 1;
                if (!LegalMoveFilter.isSquareAttacked(state, intermediateSquare, !isWhite)) {
                    moveList.add(Move.createMove(from, from - 2, state.getPieceAt(from), Piece.NONE, Move.FLAG_CASTLING, Piece.NONE));
                }
            }
        }
    }
}