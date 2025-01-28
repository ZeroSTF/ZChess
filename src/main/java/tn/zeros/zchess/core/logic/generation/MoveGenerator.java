package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;

import java.util.Arrays;
import java.util.List;

public abstract class MoveGenerator {
    // Thread-local list pool to avoid allocations
    private static final ThreadLocal<MoveList> MOVE_LIST_POOL =
            ThreadLocal.withInitial(() -> new MoveList(128));

    public static MoveList getThreadLocalMoveList() {
        return MOVE_LIST_POOL.get();
    }

    public static List<Move> generateAllMoves(BoardState state) {
        List<Move> pseudoLegal = generatePseudoLegalMoves(state);
        return LegalMoveFilter.filterLegalMoves(state, pseudoLegal);
    }

    private static List<Move> generatePseudoLegalMoves(BoardState state) {
        MoveList moveList = MOVE_LIST_POOL.get();
        moveList.clear();

        // Generate for each piece type
        generatePawnMoves(state, moveList);
        generateKnightMoves(state, moveList);
        generateBishopMoves(state, moveList);
        generateRookMoves(state, moveList);
        generateQueenMoves(state, moveList);
        generateKingMoves(state, moveList);

        return moveList.toList();
    }

    private static void generatePawnMoves(BoardState state, MoveList moveList) {
        long pawns = state.getPieces(Piece.PAWN, state.isWhiteToMove() ? Piece.WHITE : Piece.BLACK);
        while (pawns != 0) {
            int from = Long.numberOfTrailingZeros(pawns);
            PawnMoveGenerator.generate(state, from, moveList);
            pawns ^= 1L << from;
        }
    }

    private static void generateKnightMoves(BoardState state, MoveList moveList) {
        long knights = state.getPieces(Piece.KNIGHT, state.isWhiteToMove() ? Piece.WHITE : Piece.BLACK);
        while (knights != 0) {
            int from = Long.numberOfTrailingZeros(knights);
            KnightMoveGenerator.generate(state, from, moveList);
            knights ^= 1L << from;
        }
    }

    private static void generateBishopMoves(BoardState state, MoveList moveList) {
        long bishops = state.getPieces(Piece.BISHOP, state.isWhiteToMove() ? Piece.WHITE : Piece.BLACK);
        while (bishops != 0) {
            int from = Long.numberOfTrailingZeros(bishops);
            BishopMoveGenerator.generate(state, from, moveList);
            bishops ^= 1L << from;
        }
    }

    private static void generateRookMoves(BoardState state, MoveList moveList) {
        long rooks = state.getPieces(Piece.ROOK, state.isWhiteToMove() ? Piece.WHITE : Piece.BLACK);
        while (rooks != 0) {
            int from = Long.numberOfTrailingZeros(rooks);
            RookMoveGenerator.generate(state, from, moveList);
            rooks ^= 1L << from;
        }
    }

    private static void generateQueenMoves(BoardState state, MoveList moveList) {
        long queens = state.getPieces(Piece.QUEEN, state.isWhiteToMove() ? Piece.WHITE : Piece.BLACK);
        while (queens != 0) {
            int from = Long.numberOfTrailingZeros(queens);
            QueenMoveGenerator.generate(state, from, moveList);
            queens ^= 1L << from;
        }
    }

    private static void generateKingMoves(BoardState state, MoveList moveList) {
        long kings = state.getPieces(Piece.KING, state.isWhiteToMove() ? Piece.WHITE : Piece.BLACK);
        while (kings != 0) {
            int from = Long.numberOfTrailingZeros(kings);
            KingMoveGenerator.generate(state, from, moveList);
            kings ^= 1L << from;
        }
    }

    public static class MoveList {
        Move[] moves;
        int size;

        MoveList(int capacity) {
            this.moves = new Move[capacity];
            this.size = 0;
        }

        void add(Move move) {
            if (size == moves.length) {
                moves = Arrays.copyOf(moves, moves.length * 2);
            }
            moves[size++] = move;
        }

        public List<Move> toList() {
            return Arrays.asList(Arrays.copyOf(moves, size));
        }

        public void clear() {
            size = 0;
        }
    }
}