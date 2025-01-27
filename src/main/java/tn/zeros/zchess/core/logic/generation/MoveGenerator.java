package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;

import java.util.ArrayList;
import java.util.List;

import static tn.zeros.zchess.core.util.ChessConstants.*;

public abstract class MoveGenerator {
    public static List<Move> generateAllMoves(BoardState state) {
        List<Move> pseudoLegal = generatePseudoLegalMoves(state);
        return LegalMoveFilter.filterLegalMoves(state, pseudoLegal);
    }

    private static List<Move> generatePseudoLegalMoves(BoardState state) {
        List<Move> moves = new ArrayList<>(64);

        // Generate for each piece type
        generatePawnMoves(state, moves);
        generateKnightMoves(state, moves);
        generateBishopMoves(state, moves);
        generateRookMoves(state, moves);
        generateQueenMoves(state, moves);
        generateKingMoves(state, moves);

        return moves;
    }

    private static void generatePawnMoves(BoardState state, List<Move> moves) {
        long pawns = state.getPieces(PAWN, state.isWhiteToMove() ? WHITE : BLACK);
        while (pawns != 0) {
            int from = Long.numberOfTrailingZeros(pawns);
            moves.addAll(PawnMoveGenerator.generate(state, from));
            pawns ^= 1L << from;
        }
    }

    private static void generateKnightMoves(BoardState state, List<Move> moves) {
        long knights = state.getPieces(KNIGHT, state.isWhiteToMove() ? WHITE : BLACK);
        while (knights != 0) {
            int from = Long.numberOfTrailingZeros(knights);
            moves.addAll(KnightMoveGenerator.generate(state, from));
            knights ^= 1L << from;
        }
    }

    private static void generateBishopMoves(BoardState state, List<Move> moves) {
        long bishops = state.getPieces(BISHOP, state.isWhiteToMove() ? WHITE : BLACK);
        while (bishops != 0) {
            int from = Long.numberOfTrailingZeros(bishops);
            moves.addAll(BishopMoveGenerator.generate(state, from));
            bishops ^= 1L << from;
        }
    }

    private static void generateRookMoves(BoardState state, List<Move> moves) {
        long rooks = state.getPieces(ROOK, state.isWhiteToMove() ? WHITE : BLACK);
        while (rooks != 0) {
            int from = Long.numberOfTrailingZeros(rooks);
            moves.addAll(RookMoveGenerator.generate(state, from));
            rooks ^= 1L << from;
        }
    }

    private static void generateQueenMoves(BoardState state, List<Move> moves) {
        long queens = state.getPieces(QUEEN, state.isWhiteToMove() ? WHITE : BLACK);
        while (queens != 0) {
            int from = Long.numberOfTrailingZeros(queens);
            moves.addAll(QueenMoveGenerator.generate(state, from));
            queens ^= 1L << from;
        }
    }

    private static void generateKingMoves(BoardState state, List<Move> moves) {
        long kings = state.getPieces(KING, state.isWhiteToMove() ? WHITE : BLACK);
        while (kings != 0) {
            int from = Long.numberOfTrailingZeros(kings);
            moves.addAll(KingMoveGenerator.generate(state, from));
            kings ^= 1L << from;
        }
    }
}