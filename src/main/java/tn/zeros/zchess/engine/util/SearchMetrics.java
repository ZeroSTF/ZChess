package tn.zeros.zchess.engine.util;

public class SearchMetrics {
    private static SearchMetrics instance;
    private long positionsEvaluated;
    private long searchStartTime;

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
        positionsEvaluated = 0;
    }

    public void incrementPositions() {
        positionsEvaluated++;
    }

    public SearchResult endSearch() {
        long duration = System.currentTimeMillis() - searchStartTime;
        return new SearchResult(duration, positionsEvaluated);
    }
}

