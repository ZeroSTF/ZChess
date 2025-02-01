package tn.zeros.zchess.core.util;

import java.util.Random;

public class MagicNumbersGenerator {

    public static final int[] RBits = {
            12, 11, 11, 11, 11, 11, 11, 12,
            11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11,
            12, 11, 11, 11, 11, 11, 11, 12
    };
    public static final int[] BBits = {
            6, 5, 5, 5, 5, 5, 5, 6,
            5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 7, 7, 7, 7, 5, 5,
            5, 5, 7, 9, 9, 7, 5, 5,
            5, 5, 7, 9, 9, 7, 5, 5,
            5, 5, 7, 7, 7, 7, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 5,
            6, 5, 5, 5, 5, 5, 5, 6
    };
    private static final Random random = new Random();
    private static final int[] BitTable = {
            63, 30, 3, 32, 25, 41, 22, 33, 15, 50, 42, 13, 11, 53, 19, 34, 61, 29, 2,
            51, 21, 43, 45, 10, 18, 47, 1, 54, 9, 57, 0, 35, 62, 31, 40, 4, 49, 5, 52,
            26, 60, 6, 23, 44, 46, 27, 56, 16, 7, 39, 48, 24, 59, 14, 12, 55, 38, 28,
            58, 20, 37, 17, 36, 8
    };

    private static long random_uint64() {
        long u1 = random.nextInt() & 0xFFFFL;
        long u2 = random.nextInt() & 0xFFFFL;
        long u3 = random.nextInt() & 0xFFFFL;
        long u4 = random.nextInt() & 0xFFFFL;
        return u1 | (u2 << 16) | (u3 << 32) | (u4 << 48);
    }

    private static long random_uint64_fewbits() {
        return random_uint64() & random_uint64() & random_uint64();
    }

    public static int count_1s(long b) {
        int r = 0;
        while (b != 0) {
            r++;
            b &= b - 1;
        }
        return r;
    }

    private static int[] pop_1st_bit(long bb) {
        long b = bb ^ (bb - 1);
        int fold = (int) ((b & 0xFFFFFFFFL) ^ (b >>> 32));
        long newBB = bb & (bb - 1);
        int index = BitTable[(fold * 0x783a9b23) >>> 26];
        return new int[]{index, (int) newBB};
    }

    public static long index_to_uint64(int index, int bits, long m) {
        long result = 0L;
        for (int i = 0; i < bits; i++) {
            int[] popResult = pop_1st_bit(m);
            int j = popResult[0];
            m = popResult[1];
            if ((index & (1 << i)) != 0) {
                result |= (1L << j);
            }
        }
        return result;
    }

    public static long rmask(int sq) {
        long result = 0L;
        int rk = sq / 8, fl = sq & 7;
        for (int r = rk + 1; r <= 6; r++) {
            result |= (1L << (fl + r * 8));
        }
        for (int r = rk - 1; r >= 1; r--) {
            result |= (1L << (fl + r * 8));
        }
        for (int f = fl + 1; f <= 6; f++) {
            result |= (1L << (f + rk * 8));
        }
        for (int f = fl - 1; f >= 1; f--) {
            result |= (1L << (f + rk * 8));
        }
        return result;
    }

    public static long bmask(int sq) {
        long result = 0L;
        int rk = sq / 8, fl = sq & 7;
        for (int r = rk + 1, f = fl + 1; r <= 6 && f <= 6; r++, f++) {
            result |= (1L << (f + r * 8));
        }
        for (int r = rk + 1, f = fl - 1; r <= 6 && f >= 1; r++, f--) {
            result |= (1L << (f + r * 8));
        }
        for (int r = rk - 1, f = fl + 1; r >= 1 && f <= 6; r--, f++) {
            result |= (1L << (f + r * 8));
        }
        for (int r = rk - 1, f = fl - 1; r >= 1 && f >= 1; r--, f--) {
            result |= (1L << (f + r * 8));
        }
        return result;
    }

    public static long ratt(int sq, long block) {
        long result = 0L;
        int rk = sq / 8, fl = sq & 7;
        for (int r = rk + 1; r <= 7; r++) {
            long bit = 1L << (fl + r * 8);
            result |= bit;
            if ((block & bit) != 0) break;
        }
        for (int r = rk - 1; r >= 0; r--) {
            long bit = 1L << (fl + r * 8);
            result |= bit;
            if ((block & bit) != 0) break;
        }
        for (int f = fl + 1; f <= 7; f++) {
            long bit = 1L << (f + rk * 8);
            result |= bit;
            if ((block & bit) != 0) break;
        }
        for (int f = fl - 1; f >= 0; f--) {
            long bit = 1L << (f + rk * 8);
            result |= bit;
            if ((block & bit) != 0) break;
        }
        return result;
    }

    public static long batt(int sq, long block) {
        long result = 0L;
        int rk = sq / 8, fl = sq & 7;
        int r, f;
        for (r = rk + 1, f = fl + 1; r <= 7 && f <= 7; r++, f++) {
            long bit = 1L << (f + r * 8);
            result |= bit;
            if ((block & bit) != 0) break;
        }
        for (r = rk + 1, f = fl - 1; r <= 7 && f >= 0; r++, f--) {
            long bit = 1L << (f + r * 8);
            result |= bit;
            if ((block & bit) != 0) break;
        }
        for (r = rk - 1, f = fl + 1; r >= 0 && f <= 7; r--, f++) {
            long bit = 1L << (f + r * 8);
            result |= bit;
            if ((block & bit) != 0) break;
        }
        for (r = rk - 1, f = fl - 1; r >= 0 && f >= 0; r--, f--) {
            long bit = 1L << (f + r * 8);
            result |= bit;
            if ((block & bit) != 0) break;
        }
        return result;
    }

    public static int transform(long b, long magic, int bits) {
        int bLow = (int) b;
        int magicLow = (int) magic;
        int product1 = bLow * magicLow;

        int bHigh = (int) (b >>> 32);
        int magicHigh = (int) (magic >>> 32);
        int product2 = bHigh * magicHigh;

        int combined = product1 ^ product2;
        return combined >>> (32 - bits);
    }

    private static long find_magic(int sq, int m, boolean bishop) {
        long mask = bishop ? bmask(sq) : rmask(sq);
        int n = count_1s(mask);
        int size = 1 << n;

        long[] b = new long[size];
        long[] a = new long[size];

        for (int i = 0; i < size; i++) {
            b[i] = index_to_uint64(i, n, mask);
            a[i] = bishop ? batt(sq, b[i]) : ratt(sq, b[i]);
        }

        for (int k = 0; k < 100_000_000; k++) {
            long magic = random_uint64_fewbits();
            long test = (mask * magic) & 0xFF00000000000000L;
            if (count_1s(test) < 6) continue;

            long[] used = new long[4096];
            boolean fail = false;

            for (int i = 0; !fail && i < size; i++) {
                int j = transform(b[i], magic, m);
                if (used[j] == 0) {
                    used[j] = a[i];
                } else if (used[j] != a[i]) {
                    fail = true;
                }
            }

            if (!fail) {
                return magic;
            }
        }

        System.out.println("***Failed***");
        return 0L;
    }

    public static void main(String[] args) {
        System.out.println("public static final long[64] ROOK_MAGICS = {");
        for (int square = 0; square < 64; square++) {
            long magic = find_magic(square, RBits[square], false);
            System.out.printf("    0x%016xL, // %d%n", magic, square);
        }
        System.out.println("};\n\n");

        System.out.println("public static final long[64] BISHOP_MAGICS = {");
        for (int square = 0; square < 64; square++) {
            long magic = find_magic(square, BBits[square], true);
            System.out.printf("    0x%016xL, // %d%n", magic, square);
        }
        System.out.println("};");
    }
}