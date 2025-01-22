package tn.zeros.zchess.core.board;

import tn.zeros.zchess.core.move.Move;
import tn.zeros.zchess.core.piece.Piece;

import java.util.List;

public interface Board {
    void makeMove(Move move);
    void unmakeMove();
    boolean isLegalMove(int fromSquare, int toSquare);
    List<Move> getLegalMoves();
    Piece getPieceAt(int square);
    boolean isInCheck();
    boolean isGameOver();
    boolean isWhiteToMove();
    String getFEN();
    void setFEN(String fen);
}
