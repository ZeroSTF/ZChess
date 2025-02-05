package tn.zeros.zchess.engine.util;

public class SearchUtils {
    public static int MAX_MOVES = 218;
    public static int MAX_EVAL = 999999999;
    public static int MIN_EVAL = -999999999;
    public static int CHECKMATE_EVAL = 9999999;
    public static int MAX_DEPTH = 100;
    public static int ADJUSTED_MATE_SCORE = CHECKMATE_EVAL - MAX_DEPTH;

    public static int adjustMateScore(int score, int ply) {
        if (score > ADJUSTED_MATE_SCORE) {
            return score + ply;
        } else if (score < -ADJUSTED_MATE_SCORE) {
            return score - ply;
        }
        return score;
    }

    public static int unadjustMateScore(int score, int ply) {
        if (score > ADJUSTED_MATE_SCORE) {
            return score - ply;
        } else if (score < -ADJUSTED_MATE_SCORE) {
            return score + ply;
        }
        return score;
    }
}
