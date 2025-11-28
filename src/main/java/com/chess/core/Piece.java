package com.chess.core;

public class Piece {
    private final PieceType type;
    private final PieceColor color;
    private boolean hasMoved;

    public Piece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
        this.hasMoved = false;
    }

    public PieceType getType() {
        return type;
    }

    public PieceColor getColor() {
        return color;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public Piece copy() {
        Piece p = new Piece(this.type, this.color);
        p.setHasMoved(this.hasMoved);
        return p;
    }

    @Override
    public String toString() {
        return color.toString().charAt(0) + "" + type.toString().charAt(0);
    }
}
