package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class PawnMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList, long pinned, long checkingRay, long checkers, boolean capturesOnly) {
        final int pawn = state.getPieceAt(from);
        if (!Piece.isPawn(pawn)) return;

        final boolean isWhite = Piece.isWhite(pawn);
        final long fromBit = 1L << from;
        final int kingSquare = state.getKingSquare(isWhite);
        final long allPieces = state.getAllPieces();
        final long enemyPieces = state.getEnemyPieces(isWhite) | getEnPassantMask(state, isWhite);
        final int enPassantSquare = state.getEnPassantSquare();

        // If pinned, calculate pin ray and only allow moves along it
        if ((fromBit & pinned) != 0) {
            final long pinRay = MoveGenerator.calculatePinRay(from, kingSquare, state);
            final long pawnAttacks = PrecomputedMoves.getPawnAttacks(from, isWhite);
            long validMoves = capturesOnly
                    ? pawnAttacks & enemyPieces
                    : PrecomputedMoves.getPawnMoves(from, allPieces, enemyPieces, isWhite);

            validMoves &= pinRay;
            if (checkingRay != -1L) validMoves &= checkingRay;
            generateMoves(state, from, pawn, validMoves, moveList, enPassantSquare, isWhite);
            return;
        }

        // Calculate possible moves using bitboard parallelism
        final long pawnAttacks = PrecomputedMoves.getPawnAttacks(from, isWhite);
        long possibleMoves = capturesOnly
                ? pawnAttacks & enemyPieces
                : PrecomputedMoves.getPawnMoves(from, allPieces, enemyPieces, isWhite);

        // Check evasion and en passant validation
        if (checkingRay != -1L) {
            possibleMoves &= checkingRay | (validateEnPassantInCheck(state, from, enPassantSquare, checkers, isWhite) ? 1L << enPassantSquare : 0);
        } else if (enPassantSquare != -1 && (possibleMoves & (1L << enPassantSquare)) != 0) {
            possibleMoves &= ~(isEnPassantDangerous(state, from, enPassantSquare, isWhite) ? (1L << enPassantSquare) : 0);
        }

        generateMoves(state, from, pawn, possibleMoves, moveList, enPassantSquare, isWhite);
    }

    private static long getEnPassantMask(BoardState state, boolean isWhite) {
        final int ep = state.getEnPassantSquare();
        return ep != -1 ? 1L << ep : 0L;
    }

    private static boolean validateEnPassantInCheck(BoardState state, int from, int epSquare, long checkers, boolean isWhite) {
        if (epSquare == -1) return false;
        final int capturedPawnSquare = epSquare + (isWhite ? -8 : 8);
        return (checkers & (1L << capturedPawnSquare)) != 0 &&
                (PrecomputedMoves.getPawnAttacks(from, isWhite) & (1L << epSquare)) != 0;
    }

    private static void generateMoves(BoardState state, int from, int pawn, long moves,
                                      MoveGenerator.MoveList moveList, int epSquare, boolean isWhite) {
        final int[] promotions = getPromotionPieces(pawn);
        final long promotionMask = isWhite ? ChessConstants.RANK_8 : ChessConstants.RANK_1;

        while (moves != 0) {
            final int to = Long.numberOfTrailingZeros(moves);
            final boolean isPromotion = ((1L << to) & promotionMask) != 0;
            final boolean isEp = (to == epSquare);
            final int captured = getCapturedPiece(state, from, to, epSquare, isWhite);

            if (isPromotion) {
                addPromotionMoves(moveList, from, to, pawn, captured, promotions);
            } else {
                moveList.add(Move.createMove(from, to, pawn, captured, isEp ? Move.FLAG_ENPASSANT : 0, Piece.NONE));
            }
            moves &= moves - 1;
        }
    }

    private static int[] getPromotionPieces(int pawn) {
        final int color = Piece.getColor(pawn);
        return new int[]{
                Piece.makePiece(Piece.QUEEN, color),
                Piece.makePiece(Piece.KNIGHT, color),
                Piece.makePiece(Piece.ROOK, color),
                Piece.makePiece(Piece.BISHOP, color)
        };
    }

    private static void addPromotionMoves(MoveGenerator.MoveList moveList, int from, int to,
                                          int pawn, int captured, int[] promotions) {
        for (int promotion : promotions) {
            moveList.add(Move.createMove(from, to, pawn, captured, Move.FLAG_PROMOTION, promotion));
        }
    }

    private static int getCapturedPiece(BoardState state, int from, int to, int epSquare, boolean isWhite) {
        return (to == epSquare) ? state.getPieceAt(epSquare + (isWhite ? -8 : 8)) : state.getPieceAt(to);
    }

    private static boolean isEnPassantDangerous(BoardState state, int from, int epSquare, boolean isWhite) {
        final int kingSquare = state.getKingSquare(isWhite);
        final int kingRank = kingSquare >>> 3; // Divide by 8 to get the rank
        final int fromRank = from >>> 3;

        // If king isn't on the same rank as the moving pawn, en passant is safe
        if (kingRank != fromRank) return false;

        final int capturedSquare = epSquare + (isWhite ? -8 : 8);
        final long occupied = (state.getAllPieces() & ~((1L << from) | (1L << capturedSquare))) | (1L << epSquare);

        final int enemyColor = isWhite ? Piece.BLACK : Piece.WHITE;
        final long rooksQueens = state.getPieces(Piece.ROOK, enemyColor) | state.getPieces(Piece.QUEEN, enemyColor);

        return (PrecomputedMoves.getMagicRookAttack(kingSquare, occupied) & rooksQueens) != 0;
    }
}