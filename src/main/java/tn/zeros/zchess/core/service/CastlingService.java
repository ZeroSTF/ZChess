package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Piece;

import static tn.zeros.zchess.core.util.ChessConstants.*;

public class CastlingService {
    public static void updateCastlingRights(BoardState state, int movedPiece,
                                            int fromSquare, int capturedPiece,
                                            int toSquare) {
        int rights = state.getCastlingRights();

        // Handle moved pieces
        if (Piece.isKing(movedPiece)) {
            rights &= Piece.isWhite(movedPiece) ?
                    ~(WHITE_KINGSIDE | WHITE_QUEENSIDE) :
                    ~(BLACK_KINGSIDE | BLACK_QUEENSIDE);
        } else if (Piece.isRook(movedPiece)) {
            if (fromSquare == 0) rights &= ~WHITE_QUEENSIDE;
            else if (fromSquare == 7) rights &= ~WHITE_KINGSIDE;
            else if (fromSquare == 56) rights &= ~BLACK_QUEENSIDE;
            else if (fromSquare == 63) rights &= ~BLACK_KINGSIDE;
        }

        // Handle captured rooks
        if (capturedPiece != Piece.NONE && Piece.isRook(capturedPiece)) {
            switch (toSquare) {
                case 0 -> rights &= ~WHITE_QUEENSIDE;
                case 7 -> rights &= ~WHITE_KINGSIDE;
                case 56 -> rights &= ~BLACK_QUEENSIDE;
                case 63 -> rights &= ~BLACK_KINGSIDE;
            }
        }

        state.setCastlingRights(rights);
    }
}
