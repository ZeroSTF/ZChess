package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class PawnMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList, long pinned, long checkingRay) {
        int pawn = state.getPieceAt(from);
        if (pawn == Piece.NONE || !Piece.isPawn(pawn)) return;

        boolean isWhite = Piece.isWhite(pawn);
        if (isWhite != state.isWhiteToMove()) return;
        long fromBitboard = 1L << from;

        // If pinned, calculate pin ray and only allow moves along it
        if ((fromBitboard & pinned) != 0) {
            // Calculate pin ray by finding the direction to the king
            int kingSquare = state.getKingSquare(isWhite);
            long pinRay = MoveGenerator.calculatePinRay(from, kingSquare, state);

            long validMoves = PrecomputedMoves.getPawnMoves(from, state.getAllPieces(), state.getEnemyPieces(isWhite), isWhite);
            validMoves &= pinRay;
            if (checkingRay != -1L) {
                validMoves &= checkingRay;
            }
            generatePawnMovesFromBitboard(state, from, pawn, validMoves, moveList);
            return;
        }

        // Calculate base moves
        long allPieces = state.getAllPieces();
        long enemyPieces = state.getEnemyPieces(isWhite);
        int enPassantSquare = state.getEnPassantSquare();

        // Handle en passant square separately to check for special cases
        if (enPassantSquare != -1) {
            enemyPieces |= 1L << enPassantSquare;
        }

        long possibleMoves = PrecomputedMoves.getPawnMoves(from, allPieces, enemyPieces, isWhite);

        // If in check, only allow moves that block or capture the checker
        if (checkingRay != -1L) {
            possibleMoves &= checkingRay;
        }

        // Special handling for en passant captures
        if (enPassantSquare != -1 && (possibleMoves & (1L << enPassantSquare)) != 0) {
            if (!isEnPassantLegal(state, from, enPassantSquare, isWhite)) {
                // Remove the en passant capture if it would leave the king in check
                possibleMoves &= ~(1L << enPassantSquare);
            }
        }

        generatePawnMovesFromBitboard(state, from, pawn, possibleMoves, moveList);
    }

    private static void generatePawnMovesFromBitboard(BoardState state, int from, int pawn, long moves, MoveGenerator.MoveList moveList) {
        boolean isWhite = Piece.isWhite(pawn);
        int enPassantSquare = state.getEnPassantSquare();

        while (moves != 0) {
            int to = Long.numberOfTrailingZeros(moves);
            moves &= moves - 1; // Clear the least significant bit

            int captured = getCapturedPiece(state, from, to, enPassantSquare, isWhite);
            boolean isEnPassant = (to == enPassantSquare);
            if (isPromotionRank(to, isWhite)) {
                addPromotionMoves(moveList, from, to, pawn, captured);
            } else {
                moveList.add(Move.createMove(from, to, pawn, captured,
                        isEnPassant ? Move.FLAG_ENPASSANT : 0, Piece.NONE));
            }
        }
    }

    private static boolean isEnPassantLegal(BoardState state, int from, int enPassantSquare, boolean isWhite) {
        // Get the square of the pawn being captured
        int capturedPawnSquare = enPassantSquare + (isWhite ? -8 : 8);

        // Get king position
        int kingSquare = state.getKingSquare(isWhite);

        // If king is not on the same rank as the capturing pawn, en passant is safe
        if (kingSquare / 8 != from / 8) {
            return true;
        }

        // Create a mask of the squares that will be empty after the en passant
        long removedPawns = (1L << from) | (1L << capturedPawnSquare);
        long remainingPieces = state.getAllPieces() & ~removedPawns;

        int attackerColor = isWhite ? Piece.BLACK : Piece.WHITE;
        long RooksAndQueens = state.getPieces(Piece.QUEEN, attackerColor) | state.getPieces(Piece.ROOK, attackerColor);

        // Check for attacks along the rank
        long rankMask = PrecomputedMoves.getMagicRookAttack(kingSquare, remainingPieces)
                & RooksAndQueens;

        // If there are any enemy rooks or queens that could attack along the rank, the move is illegal
        return rankMask == 0;
    }

    private static int getCapturedPiece(BoardState state, int from, int to, int enPassantSquare, boolean isWhite) {
        if (to == enPassantSquare) {
            // For en passant, the captured pawn is on a different square
            int capturedSquare = enPassantSquare + (isWhite ? -8 : 8);
            return state.getPieceAt(capturedSquare);
        }
        return state.getPieceAt(to);
    }

    private static boolean isPromotionRank(int square, boolean isWhite) {
        return (isWhite && square >= 56) || (!isWhite && square <= 7);
    }

    private static void addPromotionMoves(MoveGenerator.MoveList moveList, int from, int to, int pawn, int captured) {
        // Create promotion moves in order of likely value (Queen, Knight, Rook, Bishop)
        int[] promotions = {
                Piece.isWhite(pawn) ? Piece.makePiece(Piece.QUEEN, Piece.WHITE) : Piece.makePiece(Piece.QUEEN, Piece.BLACK), // Queen
                Piece.isWhite(pawn) ? Piece.makePiece(Piece.KNIGHT, Piece.WHITE) : Piece.makePiece(Piece.KNIGHT, Piece.BLACK), // Knight
                Piece.isWhite(pawn) ? Piece.makePiece(Piece.ROOK, Piece.WHITE) : Piece.makePiece(Piece.ROOK, Piece.BLACK), // Rook
                Piece.isWhite(pawn) ? Piece.makePiece(Piece.BISHOP, Piece.WHITE) : Piece.makePiece(Piece.BISHOP, Piece.BLACK)  // Bishop
        };

        for (int promotion : promotions) {
            moveList.add(Move.createMove(from, to, pawn, captured, Move.FLAG_PROMOTION, promotion));
        }
    }
}