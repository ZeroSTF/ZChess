package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class KingMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList, long checkers, boolean capturesOnly) {
        boolean isWhite = state.isWhiteToMove();
        int kingPiece = state.getPieceAt(from);
        long friendlyPieces = state.getFriendlyPieces(isWhite);

        // Temporarily remove king to compute enemy attacks
        state.removePiece(from, kingPiece);
        final long enemyAttacks = calculateAllEnemyAttacks(state, !isWhite);
        state.addPiece(from, kingPiece);

        // Generate & filter regular moves
        long possibleMoves = PrecomputedMoves.getKingMoves(from, friendlyPieces) & ~enemyAttacks;

        if (capturesOnly) {
            possibleMoves &= state.getEnemyPieces(isWhite); // Capture-only filter
        }


        processRegularMoves(state, from, moveList, possibleMoves, kingPiece);

        // Generate castling if not in check
        if (checkers == 0 && !capturesOnly && state.getCastlingRights() != 0) {
            generateCastlingMoves(state, from, isWhite, moveList, enemyAttacks);
        }
    }

    private static void processRegularMoves(BoardState state, int from, MoveGenerator.MoveList moveList, long possibleMoves, int kingPiece) {
        while (possibleMoves != 0) {
            final int to = Long.numberOfTrailingZeros(possibleMoves);
            moveList.add(Move.createMove(from, to, kingPiece, state.getPieceAt(to), 0, Piece.NONE));
            possibleMoves &= possibleMoves - 1;
        }
    }

    private static long calculateAllEnemyAttacks(BoardState state, boolean enemyIsWhite) {
        final long allPieces = state.getAllPieces(); // King is already removed
        final int color = enemyIsWhite ? Piece.WHITE : Piece.BLACK;
        long attacks = 0;

        // Pawn attacks
        long pawns = state.getPieces(Piece.PAWN, color);
        while (pawns != 0) {
            final int sq = Long.numberOfTrailingZeros(pawns);
            attacks |= PrecomputedMoves.getPawnAttacks(sq, enemyIsWhite);
            pawns &= pawns - 1;
        }

        // Knight attacks
        long knights = state.getPieces(Piece.KNIGHT, color);
        while (knights != 0) {
            final int sq = Long.numberOfTrailingZeros(knights);
            attacks |= PrecomputedMoves.getKnightMoves(sq, 0L);
            knights &= knights - 1;
        }

        // Bishop/Queen attacks
        long bishopsQueens = state.getPieces(Piece.BISHOP, color) | state.getPieces(Piece.QUEEN, color);
        while (bishopsQueens != 0) {
            final int sq = Long.numberOfTrailingZeros(bishopsQueens);
            attacks |= PrecomputedMoves.getMagicBishopAttack(sq, allPieces);
            bishopsQueens &= bishopsQueens - 1;
        }

        // Rook/Queen attacks
        long rooksQueens = state.getPieces(Piece.ROOK, color) | state.getPieces(Piece.QUEEN, color);
        while (rooksQueens != 0) {
            final int sq = Long.numberOfTrailingZeros(rooksQueens);
            attacks |= PrecomputedMoves.getMagicRookAttack(sq, allPieces);
            rooksQueens &= rooksQueens - 1;
        }

        // King attacks
        final long enemyKing = state.getPieces(Piece.KING, color);
        if (enemyKing != 0) {
            int sq = Long.numberOfTrailingZeros(enemyKing);
            attacks |= PrecomputedMoves.getKingMoves(sq, 0L);
        }

        return attacks;
    }

    private static void generateCastlingMoves(BoardState state, int from, boolean isWhite, MoveGenerator.MoveList moveList, long enemyAttacks) {
        if (from != (isWhite ? 4 : 60)) return; // Verify king's starting position

        final int castlingRights = state.getCastlingRights();
        final long allPieces = state.getAllPieces();
        final int kingRank = isWhite ? 0 : 7;
        final long kingFileMask = 1L << (4 + kingRank * 8);

        // Kingside castling
        if ((castlingRights & (isWhite ? ChessConstants.WHITE_KINGSIDE : ChessConstants.BLACK_KINGSIDE)) != 0) {
            final long kingsidePath = isWhite ? 0x60L : 0x60L << 56;
            if ((allPieces & kingsidePath) == 0 && (enemyAttacks & (kingFileMask | kingsidePath)) == 0) {
                moveList.add(Move.createCastling(from, from + 2, state.getPieceAt(from)));
            }
        }

        // Queenside castling
        if ((castlingRights & (isWhite ? ChessConstants.WHITE_QUEENSIDE : ChessConstants.BLACK_QUEENSIDE)) != 0) {
            final long queensidePath = isWhite ? 0x0EL : 0x0EL << 56;
            final long safetyPath = isWhite ? 0x1CL : 0x1CL << 56; // Includes b-file for black
            if ((allPieces & queensidePath) == 0 && (enemyAttacks & (kingFileMask | safetyPath)) == 0) {
                moveList.add(Move.createCastling(from, from - 2, state.getPieceAt(from)));
            }
        }
    }
}