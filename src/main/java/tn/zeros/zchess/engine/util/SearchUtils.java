package tn.zeros.zchess.engine.util;

public class SearchUtils {
    public static final int TIMEOUT_VALUE = Integer.MIN_VALUE + 1;
    public static final int MAX_MOVES = 218;
    public static final int MAX_EVAL = 20_000;
    public static final int MIN_EVAL = -MAX_EVAL;
    public static final int CHECKMATE_EVAL = 1_000_000;
    public static final int MAX_DEPTH = 100;
    public static final int MATE_THRESHOLD = CHECKMATE_EVAL - MAX_DEPTH;

    public static int adjustMateScore(int score, int ply) {
        if (score > MATE_THRESHOLD) {
            return score + ply;
        } else if (score < -MATE_THRESHOLD) {
            return score - ply;
        }
        return score;
    }

    public static int unadjustMateScore(int score, int ply) {
        if (score > MATE_THRESHOLD) {
            return score - ply;
        } else if (score < -MATE_THRESHOLD) {
            return score + ply;
        }
        return score;
    }

    public static boolean isMateScore(int score) {
        return Math.abs(score) > CHECKMATE_EVAL - MAX_DEPTH;
    }

    public static boolean isTimeout(int value) {
        return value == TIMEOUT_VALUE;
    }
}
