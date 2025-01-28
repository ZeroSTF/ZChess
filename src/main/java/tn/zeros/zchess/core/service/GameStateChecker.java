package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.logic.generation.LegalMoveFilter;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Piece;

import java.util.List;

public class GameStateChecker {

    /**
     * Checks if the game is over.
     *
     * @param boardState The current board state.
     * @return true if the game is over, false otherwise.
     */
    public static boolean isGameOver(BoardState boardState) {
        if (isCheckmate(boardState) || isStalemate(boardState)) {
            return true;
        }
        if (isInsufficientMaterial(boardState) || isFiftyMoveRule(boardState)) {
            return true;
        }
        if (isThreefoldRepetition(boardState)) {
            return true;
        }
        return false;
    }

    private static boolean isCheckmate(BoardState boardState) {
        List<Integer> legalMoves = MoveGenerator.generateAllMoves(boardState);
        return legalMoves.isEmpty() && isKingInCheck(boardState);
    }

    private static boolean isStalemate(BoardState boardState) {
        List<Integer> legalMoves = MoveGenerator.generateAllMoves(boardState);
        return legalMoves.isEmpty() && !isKingInCheck(boardState);
    }

    private static boolean isKingInCheck(BoardState boardState) {
        return LegalMoveFilter.inCheck(boardState, boardState.isWhiteToMove());
    }

    private static boolean isInsufficientMaterial(BoardState boardState) {
        long allPieces = boardState.getAllPieces();
        long whitePieces = boardState.getFriendlyPieces(true);
        long blackPieces = boardState.getFriendlyPieces(false);
        long bishops = boardState.getPiecesOfType(Piece.BISHOP);
        long knights = boardState.getPiecesOfType(Piece.KNIGHT);

        if (Long.bitCount(allPieces) > 4) {
            return false; // Too many pieces for insufficient material
        }

        // Check specific scenarios
        return isKingAndMinorPieceOnly(whitePieces, bishops, knights) && isKingAndMinorPieceOnly(blackPieces, bishops, knights);
    }

    private static boolean isKingAndMinorPieceOnly(long pieces, long bishops, long knights) {
        return Long.bitCount(pieces) == 1 || (Long.bitCount(pieces) == 2 && ((pieces & bishops) != 0 || (pieces & knights) != 0));
    }

    private static boolean isFiftyMoveRule(BoardState boardState) {
        return boardState.getHalfMoveClock() >= 50;
    }

    private static boolean isThreefoldRepetition(BoardState boardState) {
        return false; // TODO Implement using a position history or Zobrist hashing.
    }
}
