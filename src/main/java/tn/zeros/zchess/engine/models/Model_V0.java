package tn.zeros.zchess.engine.models;

import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.engine.search.SearchService;
import tn.zeros.zchess.engine.util.SearchMetrics;
import tn.zeros.zchess.engine.util.SearchUtils;
import tn.zeros.zchess.engine.util.TTEntryType;

public class Model_V0 implements EngineModel {
    protected final SearchService searchService;
    protected final long maxSearchTimeMs;

    public Model_V0(SearchService searchService, long maxSearchTimeMs) {
        this.searchService = searchService;
        this.maxSearchTimeMs = maxSearchTimeMs;
    }

    @Override
    public int generateMove(BoardState boardState) {
        SearchMetrics metrics = SearchMetrics.getInstance();
        metrics.startSearch();
        if (boardState.isGameOver()) return Move.NULL_MOVE;

        MoveGenerator.MoveList legalMoves = MoveGenerator.generateAllMoves(boardState, false);
        if (legalMoves.isEmpty()) return Move.NULL_MOVE;

        searchService.clearTT();
        long searchEndTime = System.currentTimeMillis() + maxSearchTimeMs;
        int bestMove = Move.NULL_MOVE;
        int alpha = SearchUtils.MIN_EVAL;
        int beta = SearchUtils.MAX_EVAL;
        int currentDepth = 1;

        while (System.currentTimeMillis() < searchEndTime) {
            int currentBestMove = Move.NULL_MOVE;
            int currentBestEval = SearchUtils.MIN_EVAL;
            boolean timeout = false;
            for (int i = 0; i < legalMoves.size; i++) {
                // Check timeout frequently at root level
                if (System.currentTimeMillis() >= searchEndTime) {
                    timeout = true;
                    break;
                }

                int move = legalMoves.moves[i];
                MoveUndoInfo undoInfo = MoveExecutor.makeMove(boardState, move);

                int eval = -searchService.alphaBetaPrune(currentDepth - 1, -beta, -alpha, boardState, 1, searchEndTime);
                MoveExecutor.unmakeMove(boardState, undoInfo);

                if (eval == SearchUtils.TIMEOUT_VALUE) {
                    timeout = true;
                    break;
                }

                if (eval > currentBestEval) {
                    currentBestEval = eval;
                    currentBestMove = move;

                    if (eval > alpha) {
                        alpha = eval;
                        bestMove = move;
                    }
                }

                if (alpha >= beta) break; // Beta cutoff
            }

            if (timeout) break;

            // Store completed depth results
            searchService.storeTranspositionEntry(boardState, currentDepth, currentBestEval, currentBestEval >= beta ? TTEntryType.LOWER_BOUND : TTEntryType.UPPER_BOUND, currentBestMove, 0);

            currentDepth++;

            // Early exit if mate found
            if (SearchUtils.isMateScore(currentBestEval)) break;

            /*if (currentBestMove != Move.NULL_MOVE) {
                SearchMetrics.getInstance().updatePrincipalVariation(currentBestMove, currentDepth);
                System.out.printf("Depth %d: %s eval %d\n",
                        currentDepth,
                        SearchMetrics.getInstance().getPVString(),
                        currentBestEval
                );
            }*/
        }

        //SearchMetrics.SearchResult result = metrics.endSearch();
        //result.logDiagnostics();
        return bestMove != Move.NULL_MOVE ? bestMove : legalMoves.moves[0]; // Fallback
    }

}