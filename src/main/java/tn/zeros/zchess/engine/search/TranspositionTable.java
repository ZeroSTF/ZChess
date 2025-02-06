package tn.zeros.zchess.engine.search;

public class TranspositionTable {
    public final Entry[] table;
    public final int size;
    private final int indexMask;

    public TranspositionTable(int size) {
        this.size = nearestPowerOfTwo(size);
        this.table = new Entry[this.size];
        this.indexMask = this.size - 1;
    }

    private static int nearestPowerOfTwo(int n) {
        if (n <= 0) return 1; // Handle cases where size is 0 or negative, return minimum size 1
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
        if (SearchUtils.isTimeout(score)) return;
        int index = (int) (key & indexMask);
        Entry currentEntry = table[index];

        if (currentEntry == null) { // Slot is empty, always replace
            table[index] = new Entry(key, depth, score, type, bestMove, age);
        } else if (currentEntry.key == key) { // Same position
            if (depth >= currentEntry.depth) { // Deeper or same depth, replace
                table[index] = new Entry(key, depth, score, type, bestMove, age);
            }
        } else { // Hash collision: Replacement strategy
            if (type == TTEntryType.EXACT) { // Always prioritize exact scores
                table[index] = new Entry(key, depth, score, type, bestMove, age);
            } else if (currentEntry.type != TTEntryType.EXACT) { // If current is not exact, replace if new depth is greater
                if (depth >= currentEntry.depth) {
                    table[index] = new Entry(key, depth, score, type, bestMove, age);
                }
            } // Else: if current is EXACT, and new is BOUND, keep the EXACT one
        }
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            table[i] = null;
        }
    }

    public int getOccupancy() {
        int count = 0;
        for (Entry entry : table) {
            if (entry != null) count++;
        }
        return count;
    }

    public double getSizeMB() {
        final int ENTRY_SIZE_BYTES = Integer.BYTES * 4 + Long.BYTES + 1;
        return (getOccupancy() * ENTRY_SIZE_BYTES) / (1024.0 * 1024.0);
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