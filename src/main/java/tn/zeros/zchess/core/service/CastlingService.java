package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;

public class CastlingService {
    public static boolean canCastle(BoardState state, int fromSquare, int toSquare) {
        if (!state.isWhiteToMove()) {
            if (fromSquare == 4 && toSquare == 6) // White kingside
                return (state.getCastlingRights() & ChessConstants.WHITE_KINGSIDE) != 0;
            if (fromSquare == 4 && toSquare == 2) // White queenside
                return (state.getCastlingRights() & ChessConstants.WHITE_QUEENSIDE) != 0;
        } else {
            if (fromSquare == 60 && toSquare == 62) // Black kingside
                return (state.getCastlingRights() & ChessConstants.BLACK_KINGSIDE) != 0;
            if (fromSquare == 60 && toSquare == 58) // Black queenside
                return (state.getCastlingRights() & ChessConstants.BLACK_QUEENSIDE) != 0;
        }
        return false;
    }

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
