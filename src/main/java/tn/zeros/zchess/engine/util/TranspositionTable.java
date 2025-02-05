package tn.zeros.zchess.engine.util;

public class TranspositionTable {
    private final Entry[] table;
    private final int size;
    private final int indexMask;

    public TranspositionTable(int size) {
        this.size = nearestPowerOfTwo(size);
        this.table = new Entry[this.size];
        this.indexMask = this.size - 1;
    }

    private static int nearestPowerOfTwo(int n) {
        n--;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        return n + 1;
    }

    public Entry get(long key) {
        int index = (int) (key & indexMask);
        Entry entry = table[index];
        return (entry != null && entry.key == key) ? entry : null;
    }

    public void put(long key, int depth, int score, TTEntryType type, int bestMove, int age) {
        int index = (int) (key & indexMask);
        Entry current = table[index];
        if (current == null || age > current.age || (age == current.age && depth >= current.depth)) {
            table[index] = new Entry(key, depth, score, type, bestMove, age);
        }
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            table[i] = null;
        }
    }

    public static class Entry {
        public long key;
        public TTEntryType type;
        public int depth, score, bestMove, age;

        public Entry(long key, int depth, int score, TTEntryType type, int bestMove, int age) {
            this.key = key;
            this.depth = depth;
            this.score = score;
            this.type = type;
            this.bestMove = bestMove;
            this.age = age;
        }
    }
}