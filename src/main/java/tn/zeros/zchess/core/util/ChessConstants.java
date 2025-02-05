package tn.zeros.zchess.core.util;

import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;

import java.util.List;

public class ChessConstants {
    // Castling Rights
    public static final int WHITE_KINGSIDE = 0b0001;
    public static final int WHITE_QUEENSIDE = 0b0010;
    public static final int BLACK_KINGSIDE = 0b0100;
    public static final int BLACK_QUEENSIDE = 0b1000;

    // Board Geometry Masks
    public static final long RANK_1 = 0xFFL;
    public static final long RANK_8 = 0xFF00000000000000L;
    public static final long FILE_A = 0x0101010101010101L;
    public static final long FILE_H = 0x8080808080808080L;
    public static final int COLOR_SHIFT = 3;

    // Positions
    public static final String DEFAULT_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public static final String POSITION_2_FEN = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
    public static final String POSITION_3_FEN = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1";
    public static final String POSITION_4_FEN = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
    public static final String POSITION_5_FEN = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
    public static final String SEARCH_FEN = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
    public static final String QUEEN_VS_PAWN_FEN = "8/3K4/4P3/8/8/8/6k1/7q w - - 0 1";
    public static final String LASKER_ENDGAME_FEN = "k7/8/3p4/p2P1p2/P2P1P2/8/8/K7 b - - 0 1";
    public static final String TRANSPOSITION_TEST_FEN = "8/6pk/8/3Qbq1P/5P2/4B1p1/p1p5/5K2 w - - 0 1";

    // Fen constants
    public static final String FEN_DELIMITER = " ";
    public static final String FEN_WHITE_ACTIVE = "w";
    public static final String FEN_BLACK_ACTIVE = "b";
    public static final String FEN_SEPARATOR = "/";

    // Generated magic numbers
    // Rook Magics
    public static final long[] ROOK_MAGICS = {
            0xA8002C000108020L, 0x6C00049B0002001L, 0x100200010090040L, 0x2480041000800801L,
            0x280028004000800L, 0x900410008040022L, 0x280020001001080L, 0x2880002041000080L,
            0xA000800080400034L, 0x4808020004000L, 0x2290802004801000L, 0x411000D00100020L,
            0x402800800040080L, 0xB000401004208L, 0x2409000100040200L, 0x1002100004082L,
            0x22878001E24000L, 0x1090810021004010L, 0x801030040200012L, 0x500808008001000L,
            0xA08018014000880L, 0x8000808004000200L, 0x201008080010200L, 0x801020000441091L,
            0x800080204005L, 0x1040200040100048L, 0x120200402082L, 0xD14880480100080L,
            0x12040280080080L, 0x100040080020080L, 0x9020010080800200L, 0x813241200148449L,
            0x491604001800080L, 0x100401000402001L, 0x4820010021001040L, 0x400402202000812L,
            0x209009005000802L, 0x810800601800400L, 0x4301083214000150L, 0x204026458E001401L,
            0x40204000808000L, 0x8001008040010020L, 0x8410820820420010L, 0x1003001000090020L,
            0x804040008008080L, 0x12000810020004L, 0x1000100200040208L, 0x430000A044020001L,
            0x280009023410300L, 0xE0100040002240L, 0x200100401700L, 0x2244100408008080L,
            0x8000400801980L, 0x2000810040200L, 0x8010100228810400L, 0x2000009044210200L,
            0x4080008040102101L, 0x40002080411D01L, 0x2005524060000901L, 0x502001008400422L,
            0x489A000810200402L, 0x1004400080A13L, 0x4000011008020084L, 0x26002114058042L
    };

    // Bishop Magics
    public static final long[] BISHOP_MAGICS = {
            0x89A1121896040240L, 0x2004844802002010L, 0x2068080051921000L, 0x62880A0220200808L,
            0x4042004000000L, 0x100822020200011L, 0xC00444222012000AL, 0x28808801216001L,
            0x400492088408100L, 0x201C401040C0084L, 0x840800910A0010L, 0x82080240060L,
            0x2000840504006000L, 0x30010C4108405004L, 0x1008005410080802L, 0x8144042209100900L,
            0x208081020014400L, 0x4800201208CA00L, 0xF18140408012008L, 0x1004002802102001L,
            0x841000820080811L, 0x40200200A42008L, 0x800054042000L, 0x88010400410C9000L,
            0x520040470104290L, 0x1004040051500081L, 0x2002081833080021L, 0x400C00C010142L,
            0x941408200C002000L, 0x658810000806011L, 0x188071040440A00L, 0x4800404002011C00L,
            0x104442040404200L, 0x511080202091021L, 0x4022401120400L, 0x80C0040400080120L,
            0x8040010040820802L, 0x480810700020090L, 0x102008E00040242L, 0x809005202050100L,
            0x8002024220104080L, 0x431008804142000L, 0x19001802081400L, 0x200014208040080L,
            0x3308082008200100L, 0x41010500040C020L, 0x4012020C04210308L, 0x208220A202004080L,
            0x111040120082000L, 0x6803040141280A00L, 0x2101004202410000L, 0x8200000041108022L,
            0x21082088000L, 0x2410204010040L, 0x40100400809000L, 0x822088220820214L,
            0x40808090012004L, 0x910224040218C9L, 0x402814422015008L, 0x90014004842410L,
            0x1000042304105L, 0x10008830412A00L, 0x2520081090008908L, 0x40102000A0A60140L
    };

    // Bitboard printing
    public static void printBitboard(long bitboard) {
        System.out.println("  a b c d e f g h");
        System.out.println("  ---------------");
        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + "| ");

            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                System.out.print(((bitboard >> square) & 1) == 1 ? "1 " : ". ");
            }
            System.out.println("|");
        }
        System.out.println("  ---------------");
    }

    public static String squareName(int square) {
        char file = (char) ('a' + (square & 7));
        int rank = (square >> 3) + 1;
        return "" + file + rank;
    }

    public static String moveToUCI(int move) {
        String from = squareName(Move.getFrom(move));
        String to = squareName(Move.getTo(move));
        String promotion = Move.isPromotion(move) ?
                Character.toString(Character.toLowerCase(Piece.getSymbol(Move.getPromotionPiece(move)))) : "";
        return from + to + promotion;
    }

    public static void moveListToUCI(List<Integer> moves) {
        for (int move : moves) {
            String from = squareName(Move.getFrom(move));
            String to = squareName(Move.getTo(move));
            String promotion = Move.isPromotion(move) ?
                    Character.toString(Character.toLowerCase(Piece.getSymbol(Move.getPromotionPiece(move)))) : "";
            System.out.println(from + to + promotion);
        }
    }

}
