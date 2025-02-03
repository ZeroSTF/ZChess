package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoveGenerator {
    private static final int DEFAULT_CAPACITY = 128;

    public static MoveList generateAllMoves(BoardState state, boolean capturesOnly) {
        MoveList moveList = new MoveList(DEFAULT_CAPACITY);
        moveList.clear();

        // Get king square and check info early
        boolean isWhite = state.isWhiteToMove();
        int kingSquare = state.getKingSquare(isWhite);
        long checkers = LegalMoveFilter.getAttackersBitboard(state, kingSquare, !isWhite);
        int checkCount = Long.bitCount(checkers);

        // If double check, only generate king moves
        if (checkCount >= 2) {
            KingMoveGenerator.generate(state, kingSquare, moveList, checkers, capturesOnly);
            return moveList;
        }

        long pinned = calculatePinnedPieces(state, kingSquare, isWhite);
        long checkingRay = checkCount == 1 ? PrecomputedMoves.getBetweenBitboard(kingSquare, Long.numberOfTrailingZeros(checkers)) | checkers : -1L;

        long pieces = state.getFriendlyPieces(isWhite);

        while (pieces != 0) {
            int square = Long.numberOfTrailingZeros(pieces);
            int piece = state.getPieceAt(square);

            if (Piece.isPawn(piece)) {
                PawnMoveGenerator.generate(state, square, moveList, pinned, checkingRay, checkers, capturesOnly);
            } else if (Piece.isKnight(piece)) {
                KnightMoveGenerator.generate(state, square, moveList, pinned, checkingRay, capturesOnly);
            } else if (Piece.isBishop(piece)) {
                BishopMoveGenerator.generate(state, square, moveList, pinned, checkingRay, capturesOnly);
            } else if (Piece.isRook(piece)) {
                RookMoveGenerator.generate(state, square, moveList, pinned, checkingRay, capturesOnly);
            } else if (Piece.isQueen(piece)) {
                QueenMoveGenerator.generate(state, square, moveList, pinned, checkingRay, capturesOnly);
            } else if (Piece.isKing(piece)) {
                KingMoveGenerator.generate(state, square, moveList, checkers, capturesOnly);
            }

            pieces &= pieces - 1;
        }

        return moveList;
    }

    public static long calculatePinRay(int pieceSquare, int kingSquare, BoardState state) {
        int kingColor = Piece.getColor(state.getPieceAt(kingSquare));
        int dx = Integer.signum((pieceSquare & 7) - (kingSquare & 7));
        int dy = Integer.signum((pieceSquare >> 3) - (kingSquare >> 3));

        int attackerType;
        if (dx != 0 && dy != 0) {
            attackerType = Piece.BISHOP;
        } else {
            attackerType = Piece.ROOK;
        }

        int current = pieceSquare;
        while (true) {
            int nextFile = (current & 7) + dx;
            int nextRank = (current >> 3) + dy;
            if (nextFile < 0 || nextFile >= 8 || nextRank < 0 || nextRank >= 8) break;
            current = nextRank * 8 + nextFile;
            int piece = state.getPieceAt(current);
            if (piece == Piece.NONE) continue;

            if (Piece.getColor(piece) != kingColor &&
                    (Piece.getType(piece) == attackerType || Piece.isQueen(piece))) {
                // Found the attacker, compute ray between king and attacker
                return PrecomputedMoves.getBetweenBitboard(kingSquare, current)
                        | (1L << kingSquare) | (1L << current);
            } else {
                break; // Blocked by another piece
            }
        }

        // Fallback if attacker not found (shouldn't occur for valid pins)
        return PrecomputedMoves.getBetweenBitboard(kingSquare, pieceSquare)
                | (1L << kingSquare) | (1L << pieceSquare);
    }

    private static long calculatePinnedPieces(BoardState state, int kingSquare, boolean isWhite) {
        long pinned = 0L;
        long allPieces = state.getAllPieces();
        long friendlyPieces = state.getFriendlyPieces(isWhite);
        int enemyColor = isWhite ? Piece.BLACK : Piece.WHITE;

        // Check for pins by enemy bishops/queens along diagonals
        long bishopQueens = state.getPieces(Piece.BISHOP, enemyColor) |
                state.getPieces(Piece.QUEEN, enemyColor);
        long bishopPins = calculateDirectionalPins(kingSquare, allPieces, friendlyPieces,
                bishopQueens, PrecomputedMoves::getMagicBishopAttack);

        // Check for pins by enemy rooks/queens along ranks/files
        long rookQueens = state.getPieces(Piece.ROOK, enemyColor) |
                state.getPieces(Piece.QUEEN, enemyColor);
        long rookPins = calculateDirectionalPins(kingSquare, allPieces, friendlyPieces,
                rookQueens, PrecomputedMoves::getMagicRookAttack);

        return bishopPins | rookPins;
    }

    private static long calculateDirectionalPins(int kingSquare, long allPieces, long friendlyPieces,
                                                 long attackers, AttackFunction attackFunc) {
        long pins = 0L;

        while (attackers != 0) {
            int attackerSquare = Long.numberOfTrailingZeros(attackers);
            // Get ray between king and attacker using magic bitboard attacks
            long kingAttacks = attackFunc.getAttacks(kingSquare, 0L);
            long attackerAttacks = attackFunc.getAttacks(attackerSquare, 0L);
            long rayMask = kingAttacks & attackerAttacks;

            long between = PrecomputedMoves.getBetweenBitboard(kingSquare, attackerSquare) & rayMask;
            long blockers = between & allPieces;

            // If exactly one friendly piece is between king and attacker, it's pinned
            if (Long.bitCount(blockers) == 1 && (blockers & friendlyPieces) != 0) {
                pins |= blockers;
            }

            attackers &= attackers - 1;
        }
        return pins;
    }

    @FunctionalInterface
    private interface AttackFunction {
        long getAttacks(int square, long occupied);
    }

    public static class MoveList {
        public int[] moves;
        public int size;

        MoveList(int capacity) {
            this.moves = new int[capacity];
            this.size = 0;
        }

        void add(int move) {
            if (size == moves.length) {
                moves = Arrays.copyOf(moves, moves.length * 2);
            }
            moves[size++] = move;
        }

        public List<Integer> toList() {
            List<Integer> moveList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                moveList.add(moves[i]);
            }
            return moveList;
        }

        public void clear() {
            size = 0;
        }

        public boolean isEmpty() {
            return size == 0;
        }
    }
}