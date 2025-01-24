package tn.zeros.zchess.core.model;

import tn.zeros.zchess.core.logic.validation.MoveValidator;

import java.util.Stack;

public class BoardState {
    // Piece types
    private static final int PAWN = 0, KNIGHT = 1, BISHOP = 2, ROOK = 3, QUEEN = 4, KING = 5;
    // Colors
    private static final int WHITE = 0, BLACK = 1;
    // Bitboard masks
    private static final long FILE_A = 0x0101010101010101L;
    private static final long FILE_H = 0x8080808080808080L;
    private static final long RANK_1 = 0xFFL;
    private static final long RANK_8 = 0xFF00000000000000L;
    // Castling rights
    private static final int WHITE_KINGSIDE = 1;
    private static final int WHITE_QUEENSIDE = 2;
    private static final int BLACK_KINGSIDE = 4;
    private static final int BLACK_QUEENSIDE = 8;

    private final long[] pieceBitboards; // Indexed by piece type
    private final long[] colorBitboards; // Indexed by color
    private boolean whiteToMove;
    private int castlingRights;
    private int enPassantSquare;
    private int halfMoveClock;
    private int fullMoveNumber;
    private final Stack<GameState> gameStateHistory;

    public BoardState() {
        pieceBitboards = new long[6];
        colorBitboards = new long[2];
        gameStateHistory = new Stack<>();
        initializeStartingPosition();
    }

    private void initializeStartingPosition() {
        // Clear all bitboards
        for (int i = 0; i < 6; i++) pieceBitboards[i] = 0L;
        colorBitboards[WHITE] = 0L;
        colorBitboards[BLACK] = 0L;

        // Set up white pieces
        setRank(PAWN, WHITE, 1);
        setPiece(ROOK, WHITE, 0, 0);
        setPiece(KNIGHT, WHITE, 1, 0);
        setPiece(BISHOP, WHITE, 2, 0);
        setPiece(QUEEN, WHITE, 3, 0);
        setPiece(KING, WHITE, 4, 0);
        setPiece(BISHOP, WHITE, 5, 0);
        setPiece(KNIGHT, WHITE, 6, 0);
        setPiece(ROOK, WHITE, 7, 0);

        // Set up black pieces
        setRank(PAWN, BLACK, 6);
        setPiece(ROOK, BLACK, 0, 7);
        setPiece(KNIGHT, BLACK, 1, 7);
        setPiece(BISHOP, BLACK, 2, 7);
        setPiece(QUEEN, BLACK, 3, 7);
        setPiece(KING, BLACK, 4, 7);
        setPiece(BISHOP, BLACK, 5, 7);
        setPiece(KNIGHT, BLACK, 6, 7);
        setPiece(ROOK, BLACK, 7, 7);

        whiteToMove = true;
        castlingRights = WHITE_KINGSIDE | WHITE_QUEENSIDE | BLACK_KINGSIDE | BLACK_QUEENSIDE;
        enPassantSquare = -1;
        halfMoveClock = 0;
        fullMoveNumber = 1;
    }

    private void setRank(int pieceType, int color, int rank) {
        for (int file = 0; file < 8; file++) {
            setPiece(pieceType, color, file, rank);
        }
    }

    private void setPiece(int pieceType, int color, int file, int rank) {
        int square = rank * 8 + file;
        pieceBitboards[pieceType] |= 1L << square;
        colorBitboards[color] |= 1L << square;
    }

    public String getFEN() {
        StringBuilder fen = new StringBuilder();

        // Piece placement
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                Piece piece = getPieceAt(square);

                if (piece == Piece.NONE) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(piece.getSymbol());
                }
            }
            if (emptyCount > 0) fen.append(emptyCount);
            if (rank > 0) fen.append('/');
        }

        // Active color
        fen.append(whiteToMove ? " w " : " b ");

        // Castling rights
        if (castlingRights == 0) {
            fen.append('-');
        } else {
            if ((castlingRights & WHITE_KINGSIDE) != 0) fen.append('K');
            if ((castlingRights & WHITE_QUEENSIDE) != 0) fen.append('Q');
            if ((castlingRights & BLACK_KINGSIDE) != 0) fen.append('k');
            if ((castlingRights & BLACK_QUEENSIDE) != 0) fen.append('q');
        }

        // En passant
        fen.append(' ').append(enPassantSquare == -1 ? "-" : squareToAlgebraic(enPassantSquare));

        // Move clocks
        fen.append(' ').append(halfMoveClock).append(' ').append(fullMoveNumber);

        return fen.toString();
    }

    public void setFEN(String fen) {
        //TODO
    }

    public Piece getPieceAt(int square) {
        long squareMask = 1L << square;
        for (int color = WHITE; color <= BLACK; color++) {
            if ((colorBitboards[color] & squareMask) != 0) {
                for (int pieceType = PAWN; pieceType <= KING; pieceType++) {
                    if ((pieceBitboards[pieceType] & squareMask) != 0) {
                        return Piece.values()[color * 6 + pieceType];
                    }
                }
            }
        }
        return Piece.NONE;
    }

    public boolean isInCheck() {
        long kingBitboard = pieceBitboards[KING] & colorBitboards[whiteToMove ? WHITE : BLACK];
        int kingSquare = Long.numberOfTrailingZeros(kingBitboard);
        return isSquareAttacked(kingSquare, !whiteToMove);
    }

    public boolean isGameOver() {
        // TODO Implementation would check for checkmate/stalemate
        return false;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public void makeMove(Move move) {
        GameState prevState = new GameState(
                pieceBitboards.clone(),
                colorBitboards.clone(),
                castlingRights,
                enPassantSquare,
                halfMoveClock,
                fullMoveNumber,
                whiteToMove
        );
        gameStateHistory.push(prevState);

        int from = move.getFromSquare();
        int to = move.getToSquare();
        Piece piece = move.getPiece();
        int color = piece.isWhite() ? WHITE : BLACK;

        // Clear en passant
        enPassantSquare = -1;

        // Handle pawn double move to set en passant
        if (move.getPiece().isPawn()) {
            if (Math.abs(to/8 - from/8) == 2) { // Pawn moved 2 squares
                enPassantSquare = from + (move.getPiece().isWhite() ? 8 : -8);
            }
        }

        // Handle special moves
        if (move.isEnPassant()) {
            handleEnPassant(move, color);
        } else if (move.isCastling()) {
            handleCastling(move, color);
        } else {
            // Remove captured piece FIRST if present
            if (move.getCapturedPiece() != Piece.NONE) {
                removePiece(to, move.getCapturedPiece());
            }
            movePiece(from, to, piece);
            if (move.isPromotion()) {
                handlePromotion(to, color, move.getPromotionPiece());
            }
        }

        // Update castling rights
        updateCastlingRights(piece, from);

        // Update move counters
        if (piece.isPawn() || move.getCapturedPiece() != Piece.NONE) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }

        if (color == BLACK) fullMoveNumber++;
        whiteToMove = !whiteToMove;
    }
    private void handleEnPassant(Move move, int color) {
        int from = move.getFromSquare();
        int to = move.getToSquare();
        int capturedSquare = to + (color == WHITE ? -8 : 8);

        movePiece(from, to, move.getPiece());
        removePiece(capturedSquare, move.getCapturedPiece());
    }

    private void handleCastling(Move move, int color) {
        int from = move.getFromSquare();
        int to = move.getToSquare();
        boolean kingside = (to % 8) > (from % 8);

        // White kingside: e1(4) → g1(6), rook h1(7) → f1(5)
        // Black kingside: e8(60) → g8(62), rook h8(63) → f8(61)
        int rookFrom = kingside ? from + 3 : from - 4;
        int rookTo = kingside ? to - 1 : to + 1;

        // Debug output
        System.out.printf("Castling: King %d→%d, Rook %d→%d%n",
                from, to, rookFrom, rookTo);

        movePiece(from, to, move.getPiece());
        movePiece(rookFrom, rookTo, color == WHITE ? Piece.WHITE_ROOK : Piece.BLACK_ROOK);
    }

    private void handlePromotion(int square, int color, Piece promotionPiece) {
        removePiece(square, color == WHITE ? Piece.WHITE_PAWN : Piece.BLACK_PAWN);
        setPiece(promotionPiece.ordinal() % 6, color, square % 8, square / 8);
    }

    private void updateCastlingRights(Piece piece, int fromSquare) {
        if (piece.isKing()) {
            castlingRights &= piece.isWhite() ? ~(WHITE_KINGSIDE | WHITE_QUEENSIDE) : ~(BLACK_KINGSIDE | BLACK_QUEENSIDE);
        } else if (piece.isRook()) {
            if (fromSquare == 0) castlingRights &= ~WHITE_QUEENSIDE;
            else if (fromSquare == 7) castlingRights &= ~WHITE_KINGSIDE;
            else if (fromSquare == 56) castlingRights &= ~BLACK_QUEENSIDE;
            else if (fromSquare == 63) castlingRights &= ~BLACK_KINGSIDE;
        }
    }

    public boolean isSquareAttacked(int square, boolean byWhite) {
        long squareMask = 1L << square;
        int attackerColor = byWhite ? WHITE : BLACK;
        long attackers = colorBitboards[attackerColor];

        // Pawn attacks
        long pawns = pieceBitboards[PAWN] & attackers;
        if (byWhite) {
            if (((pawns << 7) & ~FILE_A & squareMask) != 0) return true;
            if (((pawns << 9) & ~FILE_H & squareMask) != 0) return true;
        } else {
            if (((pawns >>> 7) & ~FILE_H & squareMask) != 0) return true;
            if (((pawns >>> 9) & ~FILE_A & squareMask) != 0) return true;
        }

        // Knight attacks
        long knights = pieceBitboards[KNIGHT] & attackers;
        if ((knights & MoveValidator.KNIGHT_MOVES[square]) != 0) return true;

        // King attacks
        long kings = pieceBitboards[KING] & attackers;
        if ((kings & MoveValidator.KING_MOVES[square]) != 0) return true;

        // Sliding pieces
        return checkSliderAttacks(square, attackerColor, BISHOP, QUEEN, new int[]{7, 9, -7, -9}) ||
                checkSliderAttacks(square, attackerColor, ROOK, QUEEN, new int[]{8, 1, -8, -1});
    }

    private boolean checkSliderAttacks(int square, int attackerColor, int pieceType, int queenType, int[] directions) {
        long sliders = (pieceBitboards[pieceType] | pieceBitboards[queenType]) & colorBitboards[attackerColor];
        long occupied = getAllPieces();

        for (int dir : directions) {
            int current = square + dir;
            while (current >= 0 && current < 64) {
                int prev = current - dir;
                int currentFile = current % 8;
                int prevFile = prev % 8;

                // Prevent wrapping around board edges
                if (Math.abs(currentFile - prevFile) > 1) break;

                if ((sliders & (1L << current)) != 0) return true;
                if ((occupied & (1L << current)) != 0) break;
                current += dir;
            }
        }
        return false;
    }

    public long getAllPieces() {
        return colorBitboards[WHITE] | colorBitboards[BLACK];
    }

    private String squareToAlgebraic(int square) {
        char file = (char) ('a' + (square % 8));
        int rank = (square / 8) + 1;
        System.out.println(file + rank);
        return "" + file + rank;
    }

    private void movePiece(int from, int to, Piece piece) {
        int type = piece.ordinal() % 6;
        int color = piece.isWhite() ? WHITE : BLACK;
        long mask = 1L << from;

        // Remove from original square
        pieceBitboards[type] &= ~mask;
        colorBitboards[color] &= ~mask;

        // Add to new square
        mask = 1L << to;
        pieceBitboards[type] |= mask;
        colorBitboards[color] |= mask;
    }

    private void removePiece(int square, Piece piece) {
        if (piece == Piece.NONE) return;
        int type = piece.ordinal() % 6;
        int color = piece.isWhite() ? WHITE : BLACK;
        long mask = ~(1L << square);
        pieceBitboards[type] &= mask;
        colorBitboards[color] &= mask;
    }

    @Override
    public BoardState clone() {
        BoardState clone = new BoardState();
        System.arraycopy(pieceBitboards, 0, clone.pieceBitboards, 0, 6);
        System.arraycopy(colorBitboards, 0, clone.colorBitboards, 0, 2);
        clone.whiteToMove = whiteToMove;
        clone.castlingRights = castlingRights;
        clone.enPassantSquare = enPassantSquare;
        clone.halfMoveClock = halfMoveClock;
        clone.fullMoveNumber = fullMoveNumber;
        return clone;
    }

    public int getEnPassantSquare() {
        return enPassantSquare;
    }

    public boolean canCastle(int fromSquare, int toSquare) {
        if (whiteToMove) {
            // White castling
            if (fromSquare == 4 && toSquare == 6) // Kingside (e1 → g1)
                return (castlingRights & WHITE_KINGSIDE) != 0;
            if (fromSquare == 4 && toSquare == 2) // Queenside (e1 → c1)
                return (castlingRights & WHITE_QUEENSIDE) != 0;
        } else {
            // Black castling
            if (fromSquare == 60 && toSquare == 62) // Kingside (e8 → g8)
                return (castlingRights & BLACK_KINGSIDE) != 0;
            if (fromSquare == 60 && toSquare == 58) // Queenside (e8 → c8)
                return (castlingRights & BLACK_QUEENSIDE) != 0;
        }
        return false;
    }

    public void setWhiteToMove(boolean whiteToMove) {
        this.whiteToMove = whiteToMove;
    }
}
