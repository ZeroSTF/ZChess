package tn.zeros.zchess.engine.search;


import tn.zeros.zchess.core.model.Move;

import java.util.concurrent.atomic.AtomicInteger;

public class SearchMetrics {
    private final SearchDebugConfig config;
    private final long startTime;
    private final AtomicInteger nodesEvaluated;
    private final AtomicInteger ttHits;
    private volatile int currentDepth;
    private volatile int bestMove;
    private volatile int bestEval;

    public SearchMetrics() {
        this.config = SearchDebugConfig.getInstance();
        this.startTime = System.currentTimeMillis();
        this.nodesEvaluated = new AtomicInteger(0);
        this.ttHits = new AtomicInteger(0);
        this.bestMove = Move.NULL_MOVE;
        this.bestEval = 0;
    }

    public void incrementNodes() {
        if (config.isMetricsEnabled()) {
            nodesEvaluated.incrementAndGet();
        }
    }

    public void incrementTTHits() {
        if (config.isMetricsEnabled()) {
            ttHits.incrementAndGet();
        }
    }

    // Getters with metric checking
    public long getElapsedMs() {
        return System.currentTimeMillis() - startTime;
    }

    public double getNodesPerSecond() {
        long elapsed = getElapsedMs();
        return elapsed > 0 ? (nodesEvaluated.get() * 1000.0) / elapsed : 0;
    }

    public double getTTHitRate() {
        int nodes = nodesEvaluated.get();
        return nodes > 0 ? (ttHits.get() * 100.0) / nodes : 0;
    }

    public int getCurrentDepth() {
        return currentDepth;
    }

    // Other getters/setters
    public void setCurrentDepth(int depth) {
        this.currentDepth = depth;
    }

    public int getBestMove() {
        return bestMove;
    }

    public void setBestMove(int move) {
        this.bestMove = move;
    }

    public int getBestEval() {
        return bestEval;
    }

    public void setBestEval(int eval) {
        this.bestEval = eval;
    }

    public int getNodesEvaluated() {
        return nodesEvaluated.get();
    }

    public int getTTHits() {
        return ttHits.get();
    }
}