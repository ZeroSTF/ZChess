package tn.zeros.zchess.core.movegen;

import tn.zeros.zchess.core.board.Bitboard;
import tn.zeros.zchess.core.board.ChessBoard;
import tn.zeros.zchess.core.moves.Move;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    private final ChessBoard board;

    public MoveGenerator(ChessBoard board) {
        this.board = board;
    }

    public List<Move> generatePawnMoves(int color) {
        List<Move> moves = new ArrayList<>();
        long pawns = board.getPieceBitboard(ChessBoard.PAWN) & board.getColorBitboard(color);

        int direction = (color == ChessBoard.WHITE) ? -8 : 8;
        long singlePush = (color == ChessBoard.WHITE) ? Bitboard.northOne(pawns) : Bitboard.southOne(pawns);

        while (pawns != 0) {
            int fromSquare = Long.numberOfTrailingZeros(pawns);
            int toSquare = fromSquare + direction;

            // Generate single push moves
            if (isValidSquare(toSquare) && board.getPieceAt(toSquare) == -1) {
                moves.add(new Move(fromSquare, toSquare, ChessBoard.PAWN, Move.NORMAL));
            }

            pawns &= pawns - 1; // Clear least significant bit
        }
        return moves;
    }

    private boolean isValidSquare(int square) {
        return square >= 0 && square < 64;
    }
}
