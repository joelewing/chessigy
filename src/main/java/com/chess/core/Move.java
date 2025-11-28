package com.chess.core;

public class Move {
    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;
    private final Piece movedPiece;
    private final Piece capturedPiece;
    private boolean isCastling;
    private boolean isEnPassant;
    private boolean isPromotion;
    private PieceType promotionType;
    private boolean wasFirstMove;

    public Move(int startX, int startY, int endX, int endY, Piece movedPiece, Piece capturedPiece) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public Piece getMovedPiece() {
        return movedPiece;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public boolean isCastling() {
        return isCastling;
    }

    public void setCastling(boolean castling) {
        isCastling = castling;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public void setEnPassant(boolean enPassant) {
        isEnPassant = enPassant;
    }

    public boolean isPromotion() {
        return isPromotion;
    }

    public void setPromotion(boolean promotion) {
        isPromotion = promotion;
    }

    public PieceType getPromotionType() {
        return promotionType;
    }

    public void setPromotionType(PieceType promotionType) {
        this.promotionType = promotionType;
    }

    public boolean wasFirstMove() {
        return wasFirstMove;
    }

    public void setWasFirstMove(boolean wasFirstMove) {
        this.wasFirstMove = wasFirstMove;
    }

    @Override
    public String toString() {
        // Simple coordinate notation for UI display
        return new StringBuilder()
                .append(BoardCoordinates.fileFromX(startX))
                .append(BoardCoordinates.rankFromY(startY))
                .append(" -> ")
                .append(BoardCoordinates.fileFromX(endX))
                .append(BoardCoordinates.rankFromY(endY))
                .toString();
    }

    /**
     * Converts this move to standard algebraic notation for PGN export.
     * 
     * @param board The current board state
     * @param color The color of the player making the move
     * @return The algebraic notation string
     */
    public String toAlgebraicNotation(Board board, PieceColor color) {
        return AlgebraicNotationParser.toAlgebraicNotation(this, board, color);
    }

}
