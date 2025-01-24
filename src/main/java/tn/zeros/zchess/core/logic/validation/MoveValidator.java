package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;

public class MoveValidator {
    private final BoardState position;

    // Precalculated move masks
    public static final long[] KNIGHT_MOVES = new long[64];
    public static final long[] KING_MOVES = new long[64];
    public static final long[][] RAY_MOVES = new long[64][8]; // For sliding pieces

    // Direction offsets for rays (N, NE, E, SE, S, SW, W, NW)
    private static final int[] DIRECTION_OFFSETS = {-8, -7, 1, 9, 8, 7, -1, -9};

    static {
        initializeMovePatterns();
    }

    private static void initializeMovePatterns() {
        // Initialize knight moves
        int[] knightOffsets = {6, 10, 15, 17, -6, -10, -15, -17};
        for (int square = 0; square < 64; square++) {
            int rank = square / 8;
            int file = square % 8;

            for (int offset : knightOffsets) {
                int targetSquare = square + offset;
                int targetRank = targetSquare / 8;
                int targetFile = targetSquare % 8;

                if (targetSquare >= 0 && targetSquare < 64 &&
                        Math.abs(targetFile - file) + Math.abs(targetRank - rank) == 3) {
                    KNIGHT_MOVES[square] |= 1L << targetSquare;
                }
            }
        }

        // Initialize king moves
        int[] kingOffsets = {-9, -8, -7, -1, 1, 7, 8, 9};
        for (int square = 0; square < 64; square++) {
            int rank = square / 8;
            int file = square % 8;

            for (int offset : kingOffsets) {
                int targetSquare = square + offset;
                int targetRank = targetSquare / 8;
                int targetFile = targetSquare % 8;

                if (targetSquare >= 0 && targetSquare < 64 &&
                        Math.abs(targetFile - file) <= 1 &&
                        Math.abs(targetRank - rank) <= 1) {
                    KING_MOVES[square] |= 1L << targetSquare;
                }
            }
        }

        // Sliding pieces
        for (int square = 0; square < 64; square++) {
            int rank = square / 8;
            int file = square % 8;

            for (int dir = 0; dir < 8; dir++) {
                long ray = 0L;
                int current = square;

                while (true) {
                    int next = current + DIRECTION_OFFSETS[dir];
                    int nextRank = next / 8;
                    int nextFile = next % 8;

                    // Check boundaries
                    if (next < 0 || next >= 64) break;
                    if (Math.abs(nextFile - file) > 1) break;
                    if (Math.abs(nextRank - rank) > 1) break;

                    ray |= 1L << next;
                    current = next;
                    rank = nextRank;
                    file = nextFile;
                }

                RAY_MOVES[square][dir] = ray;
            }
        }
    }

    public MoveValidator(BoardState position) {
        this.position = position;
    }

    public boolean isLegalMove(int fromSquare, int toSquare) {
        Piece piece = position.getPieceAt(fromSquare);
        if (piece == Piece.NONE) return false;
        if (piece.isWhite() != position.isWhiteToMove()) return false;

        // Check if target square has friendly piece
        Piece targetPiece = position.getPieceAt(toSquare);
        if (targetPiece != Piece.NONE && targetPiece.isWhite() == piece.isWhite()) {
            return false;
        }

        boolean isLegalPieceMove = switch (piece) {
            case WHITE_PAWN, BLACK_PAWN -> isLegalPawnMove(fromSquare, toSquare, piece);
            case WHITE_KNIGHT, BLACK_KNIGHT -> isLegalKnightMove(fromSquare, toSquare);
            case WHITE_BISHOP, BLACK_BISHOP -> isLegalBishopMove(fromSquare, toSquare);
            case WHITE_ROOK, BLACK_ROOK -> isLegalRookMove(fromSquare, toSquare);
            case WHITE_QUEEN, BLACK_QUEEN -> isLegalQueenMove(fromSquare, toSquare);
            case WHITE_KING, BLACK_KING -> isLegalKingMove(fromSquare, toSquare);
            default -> false;
        };

        if (!isLegalPieceMove) return false;

        // Verify move doesn't leave king in check
        BoardState testPosition = position.clone();
        testPosition.makeMove(new Move(fromSquare, toSquare, piece, targetPiece, false, false, false, piece));

        // Preserve original turn to check correct king's safety
        boolean originalTurn = position.isWhiteToMove();
        testPosition.setWhiteToMove(originalTurn);
        return !testPosition.isInCheck();
    }

    private boolean isLegalPawnMove(int fromSquare, int toSquare, Piece pawn) {
        int direction = pawn.isWhite() ? 1 : -1;
        int rank = fromSquare / 8;
        int file = fromSquare % 8;
        int targetRank = toSquare / 8;
        int targetFile = toSquare % 8;

        // Basic one square forward move
        if (file == targetFile && targetRank == rank + direction) {
            return position.getPieceAt(toSquare) == Piece.NONE;
        }

        // Initial two square move
        if (file == targetFile && ((pawn.isWhite() && rank == 1) || (!pawn.isWhite() && rank == 6))
                && targetRank == rank + 2 * direction) {
            int intermediateSquare = fromSquare + 8 * direction;
            return position.getPieceAt(intermediateSquare) == Piece.NONE
                    && position.getPieceAt(toSquare) == Piece.NONE;
        }

        // Captures (including en passant)
        if (Math.abs(file - targetFile) == 1 && targetRank == rank + direction) {
            if (position.getPieceAt(toSquare) != Piece.NONE) {
                return true; // Regular capture
            }
            return toSquare == position.getEnPassantSquare(); // En passant capture
        }

        return false;
    }

    private boolean isLegalKnightMove(int fromSquare, int toSquare) {
        return (KNIGHT_MOVES[fromSquare] & (1L << toSquare)) != 0;
    }

    private boolean isLegalBishopMove(int fromSquare, int toSquare) {
        int fromRank = fromSquare / 8;
        int fromFile = fromSquare % 8;
        int toRank = toSquare / 8;
        int toFile = toSquare % 8;

        int dr = toRank - fromRank;
        int df = toFile - fromFile;

        if (Math.abs(dr) != Math.abs(df)) {
            return false;
        }

        int direction = getBishopDirection(dr, df);
        return direction != -1 && !isPathBlocked(fromSquare, toSquare, direction);
    }

    private int getBishopDirection(int dr, int df) {
        if (dr > 0) { // Moving south (down in internal coordinates)
            return df > 0 ? 3 : 5; // SE (3) or SW (5)
        } else { // Moving north (up in internal coordinates)
            return df > 0 ? 1 : 7; // NE (1) or NW (7)
        }
    }

    private boolean isLegalRookMove(int fromSquare, int toSquare) {
        int fromRank = fromSquare / 8;
        int fromFile = fromSquare % 8;
        int toRank = toSquare / 8;
        int toFile = toSquare % 8;

        if (fromRank != toRank && fromFile != toFile) {
            return false;
        }

        int direction = getRookDirection(fromRank, toRank, fromFile, toFile);
        return direction != -1 && !isPathBlocked(fromSquare, toSquare, direction);
    }

    private int getRookDirection(int fromRank, int toRank, int fromFile, int toFile) {
        if (fromRank == toRank) { // Horizontal move
            return toFile > fromFile ? 2 : 6; // E (2) or W (6)
        } else { // Vertical move
            return toRank > fromRank ? 4 : 0; // S (4) or N (0)
        }
    }

    private boolean isLegalQueenMove(int fromSquare, int toSquare) {
        // Queen combines bishop and rook moves
        return isLegalBishopMove(fromSquare, toSquare) || isLegalRookMove(fromSquare, toSquare);
    }

    private boolean isLegalKingMove(int fromSquare, int toSquare) {
        // Normal king move
        if ((KING_MOVES[fromSquare] & (1L << toSquare)) != 0) {
            return true;
        }

        // Castling
        if (position.canCastle(fromSquare, toSquare)) {
            int direction = (toSquare > fromSquare) ? 1 : -1;
            int rookFrom = direction == 1 ? fromSquare + 3 : fromSquare - 4;

            // 1. Check if rook exists and hasn't moved
            Piece expectedRook = position.isWhiteToMove() ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
            if (position.getPieceAt(rookFrom) != expectedRook) {
                return false;
            }

            // 2. Check if squares between king and rook are empty
            int current = fromSquare + direction;
            while (current != rookFrom) {
                if (position.getPieceAt(current) != Piece.NONE) {
                    return false;
                }
                current += direction;
            }

            // 3. Check king doesn't move through check
            current = fromSquare;
            for (int i=0; i<2; i++) {
                current += direction;
                if (position.isSquareAttacked(current, !position.isWhiteToMove())) {
                    return false;
                }
            }
            return true;
        }


        return false;
    }

    private boolean isPathBlocked(int fromSquare, int toSquare, int direction) {
        int step = DIRECTION_OFFSETS[direction];
        int current = fromSquare + step;

        while (current != toSquare) {
            if (current < 0 || current >= 64) {
                return true;
            }

            if ((position.getAllPieces() & (1L << current)) != 0) {
                return true;
            }

            // Prevent wrapping around board edges
            int prev = current - step;
            if (Math.abs((current % 8) - (prev % 8)) > 1 || Math.abs((current / 8) - (prev / 8)) > 1) {
                return true;
            }

            current += step;
        }

        return false;
    }
}
