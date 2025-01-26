package tn.zeros.zchess.core.logic.Generation;

import tn.zeros.zchess.core.logic.validation.CompositeMoveValidator;
import tn.zeros.zchess.core.logic.validation.MoveValidator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    private static final MoveValidator validator = new CompositeMoveValidator();

    public static List<Move> generateAllMoves(BoardState state) {
        List<Move> moves = new ArrayList<>(128);
        long ourPieces = state.getColorBitboard(state.isWhiteToMove() ? ChessConstants.WHITE : ChessConstants.BLACK);

        while (ourPieces != 0) {
            int from = Long.numberOfTrailingZeros(ourPieces);
            Piece piece = state.getPieceAt(from);
            generateMovesForPiece(state, from, piece, moves);
            ourPieces ^= 1L << from;
        }
        return moves;
    }

    private static void generateMovesForPiece(BoardState state, int from, Piece piece, List<Move> moves) {
        for (int to = 0; to < 64; to++) {
            if (from == to) continue;

            if (piece.isPawn() && isPromotionSquare(piece, to)) {
                addPromotionMoves(state, from, to, piece, moves);
            } else {
                addRegularMove(state, from, to, piece, moves);
            }
        }
    }

    private static void addPromotionMoves(BoardState state, int from, int to, Piece piece, List<Move> moves) {
        Piece[] promotions = piece.isWhite() ?
                new Piece[]{Piece.WHITE_QUEEN, Piece.WHITE_ROOK, Piece.WHITE_BISHOP, Piece.WHITE_KNIGHT} :
                new Piece[]{Piece.BLACK_QUEEN, Piece.BLACK_ROOK, Piece.BLACK_BISHOP, Piece.BLACK_KNIGHT};

        for (Piece promo : promotions) {
            Move move = createMove(state, from, to, piece, promo);
            if (validator.validate(state, move).isValid()) {
                moves.add(move);
            }
        }
    }

    private static void addRegularMove(BoardState state, int from, int to, Piece piece, List<Move> moves) {
        Move move = createMove(state, from, to, piece, null);
        if (validator.validate(state, move).isValid()) {
            moves.add(move);
        }
    }

    private static boolean isPromotionSquare(Piece piece, int to) {
        return piece.isPawn() && ((piece.isWhite() && to / 8 == 7) || (!piece.isWhite() && to / 8 == 0));
    }

    private static Move createMove(BoardState state, int from, int to, Piece piece, Piece promotion) {
        boolean isEnPassant = piece.isPawn() && to == state.getEnPassantSquare();
        boolean isCastling = piece.isKing() && Math.abs(from - to) == 2;
        boolean isPromotion = promotion != null;

        Piece captured = state.getPieceAt(to);
        if (isEnPassant) {
            captured = piece.isWhite() ? Piece.BLACK_PAWN : Piece.WHITE_PAWN;
        }

        return new Move(
                from, to, piece, captured,
                isPromotion, isCastling, isEnPassant,
                isPromotion ? promotion : Piece.NONE
        );
    }
}