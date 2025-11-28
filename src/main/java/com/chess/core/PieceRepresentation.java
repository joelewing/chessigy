package com.chess.core;

/**
 * Shared helpers for translating between {@link PieceType} instances and their
 * textual representations in FEN/SAN notation. Centralizing this logic keeps
 * the mappings consistent across the codebase.
 */
public final class PieceRepresentation {
    private PieceRepresentation() {
        // Utility class
    }

    public static char toFenChar(Piece piece) {
        if (piece == null) {
            return ' ';
        }
        char base = baseChar(piece.getType());
        return piece.getColor() == PieceColor.WHITE ? Character.toUpperCase(base) : base;
    }

    public static char toNotationSymbol(PieceType type) {
        if (type == null) {
            return ' ';
        }
        return Character.toUpperCase(baseChar(type));
    }

    public static PieceType fromNotationSymbol(char symbol) {
        switch (Character.toUpperCase(symbol)) {
            case 'K':
                return PieceType.KING;
            case 'Q':
                return PieceType.QUEEN;
            case 'R':
                return PieceType.ROOK;
            case 'B':
                return PieceType.BISHOP;
            case 'N':
                return PieceType.KNIGHT;
            case 'P':
                return PieceType.PAWN;
            default:
                return null;
        }
    }

    private static char baseChar(PieceType type) {
        switch (type) {
            case PAWN:
                return 'p';
            case ROOK:
                return 'r';
            case KNIGHT:
                return 'n';
            case BISHOP:
                return 'b';
            case QUEEN:
                return 'q';
            case KING:
                return 'k';
            default:
                return ' ';
        }
    }
}
