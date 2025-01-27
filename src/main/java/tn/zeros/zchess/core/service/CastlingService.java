package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;

public class CastlingService {

    public static void updateCastlingRights(BoardState state, Piece movedPiece, int fromSquare) {
        int rights = state.getCastlingRights();

        if (movedPiece.isKing()) {
            rights &= movedPiece.isWhite() ?
                    ~(ChessConstants.WHITE_KINGSIDE | ChessConstants.WHITE_QUEENSIDE) :
                    ~(ChessConstants.BLACK_KINGSIDE | ChessConstants.BLACK_QUEENSIDE);
        } else if (movedPiece.isRook()) {
            if (fromSquare == 0) rights &= ~ChessConstants.WHITE_QUEENSIDE;
            else if (fromSquare == 7) rights &= ~ChessConstants.WHITE_KINGSIDE;
            else if (fromSquare == 56) rights &= ~ChessConstants.BLACK_QUEENSIDE;
            else if (fromSquare == 63) rights &= ~ChessConstants.BLACK_KINGSIDE;
        }

        state.setCastlingRights(rights);
    }
}
