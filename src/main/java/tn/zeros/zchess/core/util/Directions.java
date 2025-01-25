package tn.zeros.zchess.core.util;

public class Directions {
    public static final int NORTH = 0;
    public static final int NORTHEAST = 1;
    public static final int EAST = 2;
    public static final int SOUTHEAST = 3;
    public static final int SOUTH = 4;
    public static final int SOUTHWEST = 5;
    public static final int WEST = 6;
    public static final int NORTHWEST = 7;

    public static final int[] BISHOP = {NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST};
    public static final int[] ROOK = {NORTH, EAST, SOUTH, WEST};
    public static final int[] QUEEN = {NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST};
}