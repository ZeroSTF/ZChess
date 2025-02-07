package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.model.Move;

public class SearchLogger {
    private final SearchDebugConfig config;
    private final SearchMetrics metrics;
    private final TranspositionTable transpositionTable;

    public SearchLogger(SearchMetrics metrics, TranspositionTable transpositionTable) {
        this.config = SearchDebugConfig.getInstance();
        this.metrics = metrics;
        this.transpositionTable = transpositionTable;
    }

    public void logIterationResults() {
        if (!config.isIterationLogging()) return;

        int occupancy = transpositionTable.getOccupancy();
        double occupancyPercentage = ((double) occupancy / transpositionTable.size) * 100;

        StringBuilder sb = new StringBuilder("\n--- Iteration ")
                .append(metrics.getCurrentDepth())
                .append(" ---\n");

        sb.append(String.format("Best Move: %s\n", Move.toAlgebraic(metrics.getBestMove())))
                .append(String.format("Evaluation: %s\n", formatEval(metrics.getBestEval())))
                .append(String.format("Nodes: %,d (%,.0f nodes/s)\n",
                        metrics.getNodesEvaluated(),
                        metrics.getNodesPerSecond()))
                .append(String.format("TT Hits: %,d (%.1f%%)\n",
                        metrics.getTTHits(),
                        metrics.getTTHitRate()))
                .append(String.format("TT Occupancy: %.1f%% (%d / %d)",
                        occupancyPercentage,
                        occupancy,
                        transpositionTable.size));

        System.out.println(sb);
    }

    public void logFinalSummary() {
        if (!config.isFinalSummary()) return;

        int occupancy = transpositionTable.getOccupancy();
        double occupancyPercentage = ((double) occupancy / transpositionTable.size) * 100;

        System.out.println("\n=== Search Summary ===");
        System.out.println("Depth Reached: " + metrics.getCurrentDepth());
        System.out.printf("Total Positions: %,d\n", metrics.getNodesEvaluated());
        System.out.printf("TT Hit Rate: %.1f%%\n", metrics.getTTHitRate());
        System.out.printf("TT Occupancy: %.1f%% (%d / %d)\n", occupancyPercentage, occupancy, transpositionTable.size);
        System.out.printf("Final Evaluation: %s\n", formatEval(metrics.getBestEval()));
    }

    private String formatEval(int eval) {
        if (SearchUtils.isMateScore(eval)) {
            int pliesRemaining = SearchUtils.CHECKMATE_EVAL - Math.abs(eval);
            int movesToMate = (pliesRemaining + 1) / 2;
            return eval > 0 ?
                    "Mate in " + movesToMate :
                    "Opponent can mate in " + movesToMate;
        }
        return String.format("%.2f", eval / 100.0);
    }
}