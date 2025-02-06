package tn.zeros.zchess.engine.search;

public class SearchUtils {
    public static final int TIMEOUT_VALUE = Integer.MIN_VALUE + 1;
    public static final int MAX_MOVES = 218;
    public static final int MAX_EVAL = 1_000_000;
    public static final int MIN_EVAL = -MAX_EVAL;
    public static final int CHECKMATE_EVAL = 200_000;
    public static final int MAX_DEPTH = 100;
    public static final int MATE_THRESHOLD = CHECKMATE_EVAL - MAX_DEPTH;
    public static final int LOOKUP_FAILED = Integer.MIN_VALUE + 2;

    public static int adjustMateScore(int score, int ply) {
        if (score > MATE_THRESHOLD) { // Positive mate (current player)
            return score - ply; // Convert to root-relative
        } else if (score < -MATE_THRESHOLD) { // Negative mate (opponent)
            return score + ply; // Convert to root-relative
        }
        return score;
    }

    public static int unadjustMateScore(int adjustedScore, int currentPly) {
        if (adjustedScore > MATE_THRESHOLD) { // Positive mate (root-relative)
            return adjustedScore + currentPly; // Convert to current-ply-relative
        } else if (adjustedScore < -MATE_THRESHOLD) { // Negative mate (root-relative)
            return adjustedScore - currentPly; // Convert to current-ply-relative
        }
        return adjustedScore;
    }

    public static boolean isMateScore(int score) {
        return Math.abs(score) > MATE_THRESHOLD;
    }

    public static boolean isTimeout(int value) {
        return value == TIMEOUT_VALUE;
    }
}
