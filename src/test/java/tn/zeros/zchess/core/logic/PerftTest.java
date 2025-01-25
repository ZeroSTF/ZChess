package tn.zeros.zchess.core.logic;

import org.junit.jupiter.api.Test;
import tn.zeros.zchess.core.logic.validation.*;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.BoardStateCloner;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.core.service.FenService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PerftTest {
    private final MoveValidator validator = new CompositeMoveValidator();

    @Test
    void testPerftPositions() {
        testPerft("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 3, 8902);
        testPerft("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 1, 44);
        testPerft("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 2, 1486);
        testPerft("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3, 62379);
        testPerft("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 4, 2103487);
        testPerft("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 5, 89941194);
    }

    @Test
    void debugProblemPosition() {
        String fen = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
        debugPerft(fen, 5);
    }

    public void testPerft(String fen, int depth, long expectedNodes) {
        BoardState state = FenService.parseFEN(fen);
        long startTime = System.nanoTime();

        long nodeCount = perft(state, depth);

        long duration = System.nanoTime() - startTime;
        printResults(fen, depth, nodeCount, duration);

        assertEquals(expectedNodes, nodeCount, "Perft mismatch for FEN: " + fen);
    }

    private long perft(BoardState state, int depth) {
        if (depth == 0) return 1L;

        long nodes = 0;
        List<Move> moves = generateAllMoves(state);

        for (Move move : moves) {
            BoardState cloned = BoardStateCloner.clone(state);
            MoveExecutor.executeMove(cloned, move);
            cloned.setWhiteToMove(!cloned.isWhiteToMove());
            nodes += perft(cloned, depth - 1);
        }
        return nodes;
    }

    private void printResults(String fen, int depth, long nodes, long nanos) {
        double ms = nanos / 1_000_000.0;
        double npms = (nodes / ms);

        System.out.printf("\nFEN: %s\nDepth %d: %,d nodes in %.3f ms (%,.1f N/ms)\n",
                fen, depth, nodes, ms, npms);
    }

    public void debugPerft(String fen, int depth) {
        BoardState state = FenService.parseFEN(fen);
        System.out.println("Debugging FEN: " + fen);
        System.out.println("For Depth " + depth);
        long total = divide(state, depth, depth);
        System.out.println("Total nodes: " + total);
    }

    private long divide(BoardState state, int currentDepth, int maxDepth) {
        if (currentDepth == 0) return 1L;

        long total = 0;
        List<Move> moves = generateAllMoves(state);

        for (Move move : moves) {
            BoardState cloned = BoardStateCloner.clone(state);
            MoveExecutor.executeMove(cloned, move);
            cloned.setWhiteToMove(!cloned.isWhiteToMove());

            long nodes = perft(cloned, currentDepth - 1);
            total += nodes;

            if (currentDepth == maxDepth) {
                System.out.printf("%-6s %,d%n", moveToUCI(move), nodes);
            }
        }
        return total;
    }

    private String moveToUCI(Move move) {
        String from = squareToAlgebraic(move.fromSquare());
        String to = squareToAlgebraic(move.toSquare());
        String promotion = move.isPromotion() ?
                Character.toString(Character.toLowerCase(move.promotionPiece().getSymbol())) : "";
        return from + to + promotion;
    }

    private String squareToAlgebraic(int square) {
        char file = (char) ('a' + (square % 8));
        int rank = (square / 8) + 1;
        return "" + file + rank;
    }

    private List<Move> generateAllMoves(BoardState state) {
        List<Move> moves = new ArrayList<>();
        for (int from = 0; from < 64; from++) {
            generateMovesForSquare(state, from, moves);
        }
        return moves;
    }

    private void generateMovesForSquare(BoardState state, int from, List<Move> moves) {
        Piece piece = state.getPieceAt(from);
        if (piece == Piece.NONE || piece.isWhite() != state.isWhiteToMove()) return;

        for (int to = 0; to < 64; to++) {
            if (from == to) continue;

            if (piece.isPawn() && isPromotionSquare(piece, to)) {
                addPromotionMoves(state, from, to, piece, moves);
            } else {
                addRegularMove(state, from, to, piece, moves);
            }
        }
    }

    private void addPromotionMoves(BoardState state, int from, int to, Piece piece, List<Move> moves) {
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

    private void addRegularMove(BoardState state, int from, int to, Piece piece, List<Move> moves) {
        Move move = createMove(state, from, to, piece, null);
        if (validator.validate(state, move).isValid()) {
            moves.add(move);
        }
    }

    private boolean isPromotionSquare(Piece piece, int to) {
        return piece.isPawn() && ((piece.isWhite() && to/8 == 7) || (!piece.isWhite() && to/8 == 0));
    }

    private Move createMove(BoardState state, int from, int to, Piece piece, Piece promotion) {
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