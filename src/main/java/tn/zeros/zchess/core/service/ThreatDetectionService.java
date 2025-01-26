package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.util.PrecomputedMoves;

import static tn.zeros.zchess.core.util.ChessConstants.*;

public class ThreatDetectionService {

    public static boolean isSquareAttacked(BoardState state, int square, boolean byWhite) {
        int attackerColor = byWhite ? WHITE : BLACK;
        long attackers = state.getColorBitboard(attackerColor);

        // Pawn attacks
        long pawns = state.getPieceBitboard(PAWN) & attackers;
        long pawnAttacks = !byWhite ? PrecomputedMoves.WHITE_PAWN_ATTACKS[square] : PrecomputedMoves.BLACK_PAWN_ATTACKS[square];
        if ((pawns & pawnAttacks) != 0) return true;

        // Knight attacks
        long knights = state.getPieceBitboard(KNIGHT) & attackers;
        if ((knights & PrecomputedMoves.KNIGHT_MOVES[square]) != 0) return true;

        // King attacks
        long kings = state.getPieceBitboard(KING) & attackers;
        if ((kings & PrecomputedMoves.KING_MOVES[square]) != 0) return true;

        // Sliding pieces
        return checkSliderAttacks(state, square, attackerColor, BISHOP, QUEEN, new int[]{7, 9, -7, -9}) ||
                checkSliderAttacks(state, square, attackerColor, ROOK, QUEEN, new int[]{8, 1, -8, -1});
    }

    private static boolean checkSliderAttacks(BoardState state, int square, int attackerColor, int pieceType, int queenType, int[] directions) {
        long sliders = (state.getPieceBitboard(pieceType) | state.getPieceBitboard(queenType)) & state.getColorBitboard(attackerColor);
        long occupied = state.getAllPieces();

        for (int dir : directions) {
            int current = square + dir;
            while (current >= 0 && current < 64) {
                int prev = current - dir;
                int currentFile = current % 8;
                int prevFile = prev % 8;

                // Prevent wrapping around board edges
                if (Math.abs(currentFile - prevFile) > 1) break;

                if ((sliders & (1L << current)) != 0) return true;
                if ((occupied & (1L << current)) != 0) break;
                current += dir;
            }
        }
        return false;
    }

    public static boolean isInCheck(BoardState state, boolean whiteKing) {
        int kingSquare = state.getKingSquare(whiteKing);
        return isSquareAttacked(state, kingSquare, !whiteKing);
    }

}
