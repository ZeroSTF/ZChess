package tn.zeros.zchess.engine.util;

public class TranspositionTable {
    public static final int EXACT = 0, LOWER_BOUND = 1, UPPER_BOUND = 2;
    private final Entry[] table;
    private final int size;

    public TranspositionTable(int size) {
        this.size = size;
        this.table = new Entry[size];
    }

    public Entry get(long key) {
        int index = (int) (key % size);
        Entry entry = table[index];
        return (entry != null && entry.key == key) ? entry : null;
    }

    public void put(long key, int depth, int score, int type, int bestMove) {
        int index = (int) (key % size);
        table[index] = new Entry(key, depth, score, type, bestMove);
    }

    public static class Entry {
        public long key;
        public int depth, score, type, bestMove;

        public Entry(long key, int depth, int score, int type, int bestMove) {
            this.key = key;
            this.depth = depth;
            this.score = score;
            this.type = type;
            this.bestMove = bestMove;
        }
    }
}