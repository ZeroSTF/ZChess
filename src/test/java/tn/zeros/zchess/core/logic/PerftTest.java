package tn.zeros.zchess.core.logic;

import org.junit.jupiter.api.Test;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.FenService;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.core.util.ChessConstants;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PerftTest {

    @Test
    void testPerftPositions() {
        //testPerft(ChessConstants.DEFAULT_FEN, 3, 8902);
        testPerft(ChessConstants.POSITION_5_FEN, 1, 44);
        testPerft(ChessConstants.POSITION_5_FEN, 2, 1486);
        testPerft(ChessConstants.POSITION_5_FEN, 3, 62379);
        testPerft(ChessConstants.POSITION_5_FEN, 4, 2103487);
        testPerft(ChessConstants.POSITION_5_FEN, 5, 89941194);
    }

    @Test
    void debugProblemPosition() {
        String fen = ChessConstants.POSITION_5_FEN;
        debugPerft(fen, 4);
    }

    public void testPerft(String fen, int depth, long expectedNodes) {
        BoardState state = FenService.parseFEN(fen);
        long startTime = System.nanoTime();

        long nodeCount = perft(state, depth);

        long duration = System.nanoTime() - startTime;
        printResults(fen, depth, nodeCount, duration);

        assertEquals(expectedNodes, nodeCount, "Perft mismatch for FEN: " + fen);
    }

    private long perft(BoardState state, int depth) {
        if (depth == 0) return 1L;

        long nodes = 0;
        List<Integer> moves = MoveGenerator.generateAllMoves(state);

        for (int move : moves) {
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            nodes += perft(state, depth - 1);
            MoveExecutor.unmakeMove(state, undoInfo);
        }
        return nodes;
    }

    private void printResults(String fen, int depth, long nodes, long nanos) {
        double ms = nanos / 1_000_000.0;
        double npms = (nodes / ms);

        System.out.printf("\nDepth %d: %,d nodes in %.3f ms (%,.1f N/ms)\n", depth, nodes, ms, npms);
    }

    public void debugPerft(String fen, int depth) {
        BoardState state = FenService.parseFEN(fen);
        System.out.println("Debugging FEN: " + fen);
        System.out.println("For Depth " + depth);
        long total = divide(state, depth, depth);
        System.out.println("Total nodes: " + total);
    }

    private long divide(BoardState state, int currentDepth, int maxDepth) {
        if (currentDepth == 0) return 1L;

        long total = 0;
        List<Integer> moves = MoveGenerator.generateAllMoves(state);

        for (int move : moves) {
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            long nodes = perft(state, currentDepth - 1);
            total += nodes;
            if (currentDepth == maxDepth) {
                System.out.printf("%-6s %,d%n", moveToUCI(move), nodes);
            }
            MoveExecutor.unmakeMove(state, undoInfo);
        }
        return total;
    }

    private String moveToUCI(int move) {
        String from = squareToAlgebraic(Move.getFrom(move));
        String to = squareToAlgebraic(Move.getTo(move));
        String promotion = Move.isPromotion(move) ?
                Character.toString(Character.toLowerCase(Piece.getSymbol(Move.getPromotionPiece(move)))) : "";
        return from + to + promotion;
    }

    private String squareToAlgebraic(int square) {
        char file = (char) ('a' + (square % 8));
        int rank = (square / 8) + 1;
        return "" + file + rank;
    }

}