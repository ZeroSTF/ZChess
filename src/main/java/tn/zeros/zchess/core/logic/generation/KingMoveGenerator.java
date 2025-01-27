package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;
import tn.zeros.zchess.core.util.PrecomputedMoves;

import java.util.ArrayList;
import java.util.List;

public class KingMoveGenerator {
    public static List<Move> generate(BoardState state, int from) {
        List<Move> moves = new ArrayList<>();
        Piece king = state.getPieceAt(from);

        if (king == null || !king.isKing()) {
            return moves;
        }

        boolean isWhite = king.isWhite();
        long friendlyPieces = state.getFriendlyPieces(isWhite);

        // Generate regular king moves
        long possibleMoves = PrecomputedMoves.getKingMoves(from, friendlyPieces);
        processMoves(state, from, king, possibleMoves, moves);

        // Generate castling moves (separate logic)
        if (state.getCastlingRights() != 0) {
            addCastlingMoves(state, from, isWhite, moves);
        }
        return moves;
    }

    private static void processMoves(BoardState state, int from, Piece king, long moveMask, List<Move> moves) {
        while (moveMask != 0) {
            int to = Long.numberOfTrailingZeros(moveMask);
            moveMask ^= 1L << to;

            Piece captured = state.getPieceAt(to).isWhite() != king.isWhite() ?
                    state.getPieceAt(to) :
                    Piece.NONE;

            moves.add(new Move(
                    from, to, king, captured,
                    false, false, false, Piece.NONE
            ));
        }
    }

    private static void addCastlingMoves(BoardState state, int from, boolean isWhite, List<Move> moves) {
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
                    moves.add(new Move(
                            from, from + 2, state.getPieceAt(from), Piece.NONE,
                            false, true, false, Piece.NONE
                    ));
                }
            }
        }

        // Queenside castling
        if ((castlingRights & (isWhite ? ChessConstants.WHITE_QUEENSIDE : ChessConstants.BLACK_QUEENSIDE)) != 0) {
            long queensideMask = isWhite ? 0xEL : 0xE00000000000000L;
            if ((allPieces & queensideMask) == 0) {
                int intermediateSquare = from - 1;
                if (!LegalMoveFilter.isSquareAttacked(state, intermediateSquare, !isWhite)) {
                    moves.add(new Move(
                            from, from - 2, state.getPieceAt(from), Piece.NONE,
                            false, true, false, Piece.NONE
                    ));
                }
            }
        }
    }
}