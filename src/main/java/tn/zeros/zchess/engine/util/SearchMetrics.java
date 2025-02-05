package tn.zeros.zchess.engine.util;

public class SearchMetrics {
    private static SearchMetrics instance;

    // Core metrics
    private long nodesVisited;        // All nodes in main search
    private long qNodesVisited;       // Quiescence search nodes
    private long transpositionHits;   // TT cache hits
    private long checkmatesDetected;  // Mate scores found
    private long timeoutsOccurred;    // Aborted searches
    private long terminalEvaluations; // Leaf node evaluations
    private long nonTerminalEvals;    // Non-leaf evaluations
    private long searchStartTime;

    // PV tracking
    private int[] pvMoves = new int[SearchUtils.MAX_DEPTH];
    private int pvLength;

    private SearchMetrics() {
    }

    public static SearchMetrics getInstance() {
        if (instance == null) {
            instance = new SearchMetrics();
        }
        return instance;
    }

    public void startSearch() {
        searchStartTime = System.currentTimeMillis();
        nodesVisited = 0;
        qNodesVisited = 0;
        transpositionHits = 0;
        checkmatesDetected = 0;
        timeoutsOccurred = 0;
        terminalEvaluations = 0;
        nonTerminalEvals = 0;
        pvLength = 0;
    }

    // === Add these increment methods ===
    public void incrementNodes() {
        nodesVisited++;
    }

    public void incrementQNodes() {
        qNodesVisited++;
    }

    public void incrementTranspositionHits() {
        transpositionHits++;
    }

    public void incrementCheckmates() {
        checkmatesDetected++;
    }

    public void incrementTimeouts() {
        timeoutsOccurred++;
    }

    public void incrementTerminalEvals() {
        terminalEvaluations++;
    }

    public void incrementNonTerminalEvals() {
        nonTerminalEvals++;
    }

    // PV management
    public void updatePrincipalVariation(int move, int depth) {
        //Arrays.fill(pvMoves, depth, SearchUtils.MAX_DEPTH, Move.NULL_MOVE);
        //pvMoves[depth] = move;
        pvLength = depth + 1;
    }

    public String getPVString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pvLength; i++) {
            //sb.append(Move.toAlgebraic(pvMoves[i])).append(" ");
        }
        return sb.toString();
    }

    public SearchResult endSearch() {
        long duration = System.currentTimeMillis() - searchStartTime;
        return new SearchResult(
                duration,
                nodesVisited,
                qNodesVisited,
                transpositionHits,
                checkmatesDetected,
                timeoutsOccurred,
                terminalEvaluations,
                nonTerminalEvals,
                getPVString()
        );
    }

    public record SearchResult(
            long timeMs,
            long nodes,
            long qNodes,
            long ttHits,
            long checkmates,
            long timeouts,
            long terminalEvals,
            long nonTerminalEvals,
            String pv
    ) {
        public void logDiagnostics() {
            long totalNodes = nodes + qNodes;
            double nps = (totalNodes * 1000.0) / (timeMs + 1);

            System.out.printf(
                    "Search Diagnostics:\n" +
                            "PV: %s\n" +
                            "Nodes: %,d (%,.1f knps)\n" +
                            "QNodes: %,d (%.1f%%)\n" +
                            "TT Hits: %,d (%.1f%% of nodes)\n" +
                            "Checkmates: %,d  Timeouts: %,d\n" +
                            "Evaluations: %,d terminal / %,d non-terminal\n",
                    pv,
                    totalNodes, nps / 1000,
                    qNodes, (qNodes * 100.0) / totalNodes,
                    ttHits, (ttHits * 100.0) / totalNodes,
                    checkmates, timeouts,
                    terminalEvals, nonTerminalEvals
            );
        }
    }
}

