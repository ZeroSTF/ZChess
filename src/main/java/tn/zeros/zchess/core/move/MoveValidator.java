package tn.zeros.zchess.core.move;

import tn.zeros.zchess.core.board.BitboardPosition;
import tn.zeros.zchess.core.piece.Piece;

import java.util.List;

public class MoveValidator {
    private final BitboardPosition position;

    public List<Move> getValidMoves() {
        return new MoveGenerator(position).generateLegalMoves();
    }

    // Precalculated move masks
    public static final long[] KNIGHT_MOVES = new long[64];
    public static final long[] KING_MOVES = new long[64];
    public static final long[][] RAY_MOVES = new long[64][8]; // For sliding pieces

    // Direction offsets for rays (N, NE, E, SE, S, SW, W, NW)
    private static final int[] DIRECTION_OFFSETS = {8, 9, 1, -7, -8, -9, -1, 7};

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

    public MoveValidator(BitboardPosition position) {
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
        BitboardPosition testPosition = position.clone();
        testPosition.makeMove(new Move(fromSquare, toSquare, piece, targetPiece, false, false, false, piece));
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
        // Check if move is on diagonal
        int rankDiff = Math.abs((toSquare / 8) - (fromSquare / 8));
        int fileDiff = Math.abs((toSquare % 8) - (fromSquare % 8));
        if (rankDiff != fileDiff) return false;

        // Get direction index (NE=1, SE=3, SW=5, NW=7)
        int direction = (toSquare > fromSquare ? 1 : 7) + (((toSquare % 8) - (fromSquare % 8)) > 0 ? 0 : 2);

        return !isPathBlocked(fromSquare, toSquare, direction);
    }

    private boolean isLegalRookMove(int fromSquare, int toSquare) {
        // Check if move is on rank or file
        boolean onSameRank = (toSquare / 8) == (fromSquare / 8);
        boolean onSameFile = (toSquare % 8) == (fromSquare % 8);
        if (!onSameRank && !onSameFile) return false;

        // Get direction index (N=0, E=2, S=4, W=6)
        int direction;
        if (onSameRank) {
            direction = 2 + (toSquare < fromSquare ? 4 : 0);
        } else {
            direction = 0 + (toSquare < fromSquare ? 4 : 0);
        }

        return !isPathBlocked(fromSquare, toSquare, direction);
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
            // Check if path is clear and not under attack
            int step = (toSquare > fromSquare) ? 1 : -1;
            int current = fromSquare + step;
            int end = toSquare;

            while (current != end) {
                if (position.getPieceAt(current) != Piece.NONE ||
                        position.isSquareAttacked(current, position.isWhiteToMove())) {
                    return false;
                }
                current += step;
            }

            return true;
        }

        return false;
    }

    private boolean isPathBlocked(int fromSquare, int toSquare, int direction) {
        long path = RAY_MOVES[fromSquare][direction];
        long toMask = 1L << toSquare;
        long between = path & ~(toMask | (1L << fromSquare));
        return (between & position.getAllPieces()) != 0;
    }
}
