package com.chess.core;

/**
 * Utility methods for working with board coordinates to avoid repeated
 * boundary checks scattered across the codebase.
 */
public final class BoardCoordinates {
    private static final int BOARD_SIZE = 8;
    private static final char FILE_START = 'a';

    private BoardCoordinates() {
        // Utility class
    }

    public static boolean isValid(int x, int y) {
        return inRange(x) && inRange(y);
    }

    public static String toAlgebraic(int x, int y) {
        requireValidSquare(x, y);
        return new StringBuilder(2)
                .append(fileFromX(x))
                .append(rankFromY(y))
                .toString();
    }

    public static char fileFromX(int x) {
        if (!inRange(x)) {
            throw new IllegalArgumentException("File index out of range: " + x);
        }
        return (char) (FILE_START + x);
    }

    public static int xFromFile(char file) {
        int normalized = Character.toLowerCase(file) - FILE_START;
        if (!inRange(normalized)) {
            throw new IllegalArgumentException("File character out of range: " + file);
        }
        return normalized;
    }

    public static int rankFromY(int y) {
        if (!inRange(y)) {
            throw new IllegalArgumentException("Rank index out of range: " + y);
        }
        return BOARD_SIZE - y;
    }

    public static int yFromRank(int rank) {
        if (rank < 1 || rank > BOARD_SIZE) {
            throw new IllegalArgumentException("Rank value out of range: " + rank);
        }
        return BOARD_SIZE - rank;
    }

    public static int yFromRankChar(char rankChar) {
        return yFromRank(Character.getNumericValue(rankChar));
    }

    private static void requireValidSquare(int x, int y) {
        if (!isValid(x, y)) {
            throw new IllegalArgumentException(
                    String.format("Invalid square coordinates: (%d,%d)", x, y));
        }
    }

    private static boolean inRange(int value) {
        return value >= 0 && value < BOARD_SIZE;
    }
}
