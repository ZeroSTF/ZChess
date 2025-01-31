package tn.zeros.zchess.core.logic;

import org.junit.jupiter.api.Test;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.service.FenService;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.core.util.ChessConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PerftTest {

    @Test
    void testPerftPositions() {
        testPerft(ChessConstants.POSITION_5_FEN, 1, 44);
        testPerft(ChessConstants.POSITION_5_FEN, 2, 1486);
        testPerft(ChessConstants.POSITION_5_FEN, 3, 62379);
        testPerft(ChessConstants.POSITION_5_FEN, 4, 2103487);
        testPerft(ChessConstants.POSITION_5_FEN, 5, 89941194);

        // New tests
        testPerft(ChessConstants.DEFAULT_FEN, 3, 8902);
        
        testPerft(ChessConstants.POSITION_2_FEN, 1, 48);
        testPerft(ChessConstants.POSITION_2_FEN, 2, 2039);
        testPerft(ChessConstants.POSITION_2_FEN, 3, 97862);
        testPerft(ChessConstants.POSITION_2_FEN, 4, 4085603);
        testPerft(ChessConstants.POSITION_2_FEN, 5, 193690690);

        testPerft(ChessConstants.POSITION_3_FEN, 1, 14);
        testPerft(ChessConstants.POSITION_3_FEN, 2, 191);
        testPerft(ChessConstants.POSITION_3_FEN, 3, 2812);
        testPerft(ChessConstants.POSITION_3_FEN, 4, 43238);
        testPerft(ChessConstants.POSITION_3_FEN, 5, 674624);

        testPerft(ChessConstants.POSITION_4_FEN, 1, 6);
        testPerft(ChessConstants.POSITION_4_FEN, 2, 264);
        testPerft(ChessConstants.POSITION_4_FEN, 3, 9467);
        testPerft(ChessConstants.POSITION_4_FEN, 4, 422333);
        testPerft(ChessConstants.POSITION_4_FEN, 5, 15833292);
    }

    @Test
    void debugProblemPosition() {
        String fen = ChessConstants.POSITION_3_FEN;
        debugPerft(fen, 4);
    }

    public void testPerft(String fen, int depth, long expectedNodes) {
        BoardState state = new BoardState();
        FenService.parseFEN(fen, state);
        long startTime = System.nanoTime();

        long nodeCount = perft(state, depth);

        long duration = System.nanoTime() - startTime;
        printResults(fen, depth, nodeCount, duration);

        assertEquals(expectedNodes, nodeCount, "Perft mismatch for FEN: " + fen);
    }

    private long perft(BoardState state, int depth) {
        if (depth == 0) return 1L;

        long nodes = 0;

        MoveGenerator.MoveList moves = MoveGenerator.generateAllMoves(state);

        for (int i = 0; i < moves.size; i++) {
            int move = moves.moves[i];
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
        BoardState state = new BoardState();
        FenService.parseFEN(fen, state);
        System.out.println("Debugging FEN: " + fen);
        System.out.println("For Depth " + depth);
        long total = divide(state, depth, depth);
        System.out.println("Total nodes: " + total);
    }

    private long divide(BoardState state, int currentDepth, int maxDepth) {
        if (currentDepth == 0) return 1L;

        long total = 0;
        MoveGenerator.MoveList moves = MoveGenerator.generateAllMoves(state);

        for (int i = 0; i < moves.size; i++) {
            int move = moves.moves[i];
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            long nodes = perft(state, currentDepth - 1);
            total += nodes;
            if (currentDepth == maxDepth) {
                System.out.printf("%-6s %,d%n", ChessConstants.moveToUCI(move), nodes);
            }
            MoveExecutor.unmakeMove(state, undoInfo);
        }
        return total;
    }

}