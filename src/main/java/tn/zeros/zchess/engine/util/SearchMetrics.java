package tn.zeros.zchess.engine.util;

public class SearchMetrics {
    private static SearchMetrics instance;
    private long nodesVisited;       // All nodes in main search
    private long qNodesVisited;      // Quiescence search nodes
    private long transpositionHits;  // TT cache hits
    private long searchStartTime;
    private long positionsEvaluated; // Terminal evaluations

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
        positionsEvaluated = 0;
    }

    public void incrementNodes() {
        nodesVisited++;
    }

    public void incrementQNodes() {
        qNodesVisited++;
    }

    public void incrementTranspositionHits() {
        transpositionHits++;
    }

    public void incrementPositions() {
        positionsEvaluated++;
    }

    public SearchResult endSearch() {
        long duration = System.currentTimeMillis() - searchStartTime;
        return new SearchResult(
                duration,
                positionsEvaluated,
                nodesVisited,
                qNodesVisited,
                transpositionHits
        );
    }

    public record SearchResult(
            long timeMs,
            long positions,
            long nodes,
            long qNodes,
            long ttHits
    ) {
        public void logPerformance() {
            long totalNodes = nodes + qNodes;
            double nps = (totalNodes * 1000.0) / (timeMs + 1); // +1 to avoid div by zero

            System.out.printf(
                    "Nodes: %,d (%,.1f knps)%n" +
                            "Quiescence: %,d (%.1f%%)%n" +
                            "TT Hits: %,d (%.1f%%)%n" +
                            "Evaluations: %,d%n",
                    totalNodes, nps / 1000,
                    qNodes, (qNodes * 100.0) / totalNodes,
                    ttHits, (ttHits * 100.0) / totalNodes,
                    positions
            );
        }
    }
}

