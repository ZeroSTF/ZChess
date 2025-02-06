package tn.zeros.zchess.engine.search;


import tn.zeros.zchess.core.model.Move;

import java.util.ArrayList;
import java.util.List;

public class SearchMetrics {
    private long startTime;
    private int currentDepth;
    private int nodesEvaluated;
    private int transpositionTableHits;
    private int bestMove;
    private int bestEval;
    private List<Integer> principalVariation;
    private int positionsEvaluated;

    public void reset() {
        startTime = System.currentTimeMillis();
        currentDepth = 0;
        nodesEvaluated = 0;
        transpositionTableHits = 0;
        bestMove = Move.NULL_MOVE;
        bestEval = 0;
        principalVariation = new ArrayList<>();
        positionsEvaluated = 0;
    }

    public void incrementNodesEvaluated() {
        nodesEvaluated++;
        positionsEvaluated++;
    }

    public void incrementTranspositionTableHits() {
        transpositionTableHits++;
    }

    // Getters and Setters
    public long getStartTime() {
        return startTime;
    }

    public int getCurrentDepth() {
        return currentDepth;
    }

    public void setCurrentDepth(int depth) {
        this.currentDepth = depth;
    }

    public int getNodesEvaluated() {
        return nodesEvaluated;
    }

    public int getTranspositionTableHits() {
        return transpositionTableHits;
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

    public List<Integer> getPrincipalVariation() {
        return principalVariation;
    }

    public void setPrincipalVariation(List<Integer> pv) {
        this.principalVariation = pv;
    }

    public int getPositionsEvaluated() {
        return positionsEvaluated;
    }
}