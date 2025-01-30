package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class KingMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList, long checkers) {
        boolean isWhite = state.isWhiteToMove();
        int kingPiece = state.getPieceAt(from);
        long friendlyPieces = state.getFriendlyPieces(isWhite);

        // Temporarily remove king to compute enemy attacks
        state.removePiece(from, kingPiece);
        long enemyAttacks = calculateAllEnemyAttacks(state, !isWhite);
        state.addPiece(from, kingPiece);

        // Generate & filter regular moves
        long possibleMoves = PrecomputedMoves.getKingMoves(from, friendlyPieces) & ~enemyAttacks;
        processRegularMoves(state, from, moveList, possibleMoves, kingPiece, isWhite);

        // Generate castling if not in check
        if (checkers == 0 && state.getCastlingRights() != 0) {
            generateCastlingMoves(state, from, isWhite, moveList);
        }
    }

    private static void processRegularMoves(BoardState state, int from, MoveGenerator.MoveList moveList,
                                            long possibleMoves, int kingPiece, boolean isWhite) {
        while (possibleMoves != 0) {
            int to = Long.numberOfTrailingZeros(possibleMoves);
            int captured = state.getPieceAt(to);
            if (captured == Piece.NONE || Piece.isWhite(captured) != isWhite) {
                moveList.add(Move.createMove(from, to, kingPiece, captured, 0, Piece.NONE));
            }
            possibleMoves &= possibleMoves - 1;
        }
    }

    private static long calculateAllEnemyAttacks(BoardState state, boolean enemyIsWhite) {
        long attacks = 0;
        long allPieces = state.getAllPieces(); // King is already removed
        int color = enemyIsWhite ? Piece.WHITE : Piece.BLACK;

        // Pawn attacks
        long pawns = state.getPieces(Piece.PAWN, color);
        while (pawns != 0) {
            int sq = Long.numberOfTrailingZeros(pawns);
            attacks |= PrecomputedMoves.getPawnAttacks(sq, enemyIsWhite);
            pawns &= pawns - 1;
        }

        // Knight attacks
        long knights = state.getPieces(Piece.KNIGHT, color);
        while (knights != 0) {
            int sq = Long.numberOfTrailingZeros(knights);
            attacks |= PrecomputedMoves.getKnightMoves(sq, 0L);
            knights &= knights - 1;
        }

        // Bishop/Queen attacks
        long bishopsQueens = state.getPieces(Piece.BISHOP, color) |
                state.getPieces(Piece.QUEEN, color);
        while (bishopsQueens != 0) {
            int sq = Long.numberOfTrailingZeros(bishopsQueens);
            attacks |= PrecomputedMoves.getMagicBishopAttack(sq, allPieces);
            bishopsQueens &= bishopsQueens - 1;
        }

        // Rook/Queen attacks
        long rooksQueens = state.getPieces(Piece.ROOK, color) |
                state.getPieces(Piece.QUEEN, color);
        while (rooksQueens != 0) {
            int sq = Long.numberOfTrailingZeros(rooksQueens);
            attacks |= PrecomputedMoves.getMagicRookAttack(sq, allPieces);
            rooksQueens &= rooksQueens - 1;
        }

        // King attacks
        long enemyKing = state.getPieces(Piece.KING, color);
        if (enemyKing != 0) {
            int sq = Long.numberOfTrailingZeros(enemyKing);
            attacks |= PrecomputedMoves.getKingMoves(sq, 0L);
        }

        return attacks;
    }

    private static void generateCastlingMoves(BoardState state, int from, boolean isWhite,
                                              MoveGenerator.MoveList moveList) {
        if (from != (isWhite ? 4 : 60)) return; // Verify king's starting position

        int castlingRights = state.getCastlingRights();
        long allPieces = state.getAllPieces();

        // White castling checks
        if (isWhite) {
            // Kingside (0b0001)
            if ((castlingRights & ChessConstants.WHITE_KINGSIDE) != 0) {
                long path = 0x60L; // f1 and g1
                if ((allPieces & path) == 0 && isPathSafe(state, path, true)) {
                    int to = 6; // g1
                    moveList.add(Move.createCastling(from, to, state.getPieceAt(from)));
                }
            }

            // Queenside (0b0010)
            if ((castlingRights & ChessConstants.WHITE_QUEENSIDE) != 0) {
                long emptyPath = 0x0EL; // b1, c1, d1
                long safetyPath = 0x0CL; // c1, d1 (king's path)
                if ((allPieces & emptyPath) == 0 && isPathSafe(state, safetyPath, true)) {
                    int to = 2; // c1
                    moveList.add(Move.createCastling(from, to, state.getPieceAt(from)));
                }
            }
        }
        // Black castling checks
        else {
            // Kingside (0b0100)
            if ((castlingRights & ChessConstants.BLACK_KINGSIDE) != 0) {
                long path = 0x60L << 56; // f8 and g8
                if ((allPieces & path) == 0 && isPathSafe(state, path, false)) {
                    int to = 62; // g8
                    moveList.add(Move.createCastling(from, to, state.getPieceAt(from)));
                }
            }

            // Queenside (0b1000)
            if ((castlingRights & ChessConstants.BLACK_QUEENSIDE) != 0) {
                long emptyPath = 0x0EL << 56; // b8, c8, d8
                long safetyPath = 0x0CL << 56; // c8, d8 (king's path)
                if ((allPieces & emptyPath) == 0 && isPathSafe(state, safetyPath, false)) {
                    int to = 58; // c8
                    moveList.add(Move.createCastling(from, to, state.getPieceAt(from)));
                }
            }
        }
    }

    private static boolean isPathSafe(BoardState state, long path, boolean isWhite) {
        long temp = path;
        while (temp != 0) {
            int sq = Long.numberOfTrailingZeros(temp);
            if (LegalMoveFilter.isSquareAttacked(state, sq, !isWhite)) return false;
            temp &= temp - 1;
        }
        return true;
    }
}