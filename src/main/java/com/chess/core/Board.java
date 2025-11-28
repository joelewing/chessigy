package com.chess.core;

public class Board {
    private static final PieceType[] BACK_RANK = {
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK };

    private Piece[][] grid;

    public Board() {
        grid = new Piece[8][8];
        resetBoard();
    }

    public void resetBoard() {
        grid = new Piece[8][8];
        moveHistory.clear();
        setupSide(PieceColor.BLACK);
        setupSide(PieceColor.WHITE);
    }

    private void setupSide(PieceColor color) {
        int backRank = color == PieceColor.WHITE ? 7 : 0;
        int pawnRank = color == PieceColor.WHITE ? 6 : 1;
        setupBackRank(backRank, color);
        setupPawnRank(pawnRank, color);
    }

    private void setupBackRank(int rank, PieceColor color) {
        for (int file = 0; file < BACK_RANK.length; file++) {
            grid[file][rank] = new Piece(BACK_RANK[file], color);
        }
    }

    private void setupPawnRank(int rank, PieceColor color) {
        for (int file = 0; file < grid.length; file++) {
            grid[file][rank] = new Piece(PieceType.PAWN, color);
        }
    }

    public Piece getPiece(int x, int y) {
        if (BoardCoordinates.isValid(x, y)) {
            return grid[x][y];
        }
        return null;
    }

    public void setPiece(int x, int y, Piece piece) {
        if (BoardCoordinates.isValid(x, y)) {
            grid[x][y] = piece;
        }
    }

    private java.util.Stack<Move> moveHistory = new java.util.Stack<>();

    public Move getLastMove() {
        return moveHistory.isEmpty() ? null : moveHistory.peek();
    }

    public void movePiece(Move move) {
        Piece p = grid[move.getStartX()][move.getStartY()];

        // Track first move
        if (p != null) {
            move.setWasFirstMove(!p.hasMoved());
        }

        grid[move.getEndX()][move.getEndY()] = p;
        grid[move.getStartX()][move.getStartY()] = null;
        if (p != null) {
            p.setHasMoved(true);
        }

        // Handle Castling
        if (move.isCastling()) {
            int y = move.getStartY();
            boolean kingside = move.getEndX() > move.getStartX();
            int rookStartX = kingside ? 7 : 0;
            int rookEndX = kingside ? 5 : 3;
            Piece rook = grid[rookStartX][y];
            grid[rookEndX][y] = rook;
            grid[rookStartX][y] = null;
            if (rook != null)
                rook.setHasMoved(true);
        }

        // Handle En Passant Capture
        if (move.isEnPassant()) {
            // The captured pawn is at [endX, startY]
            grid[move.getEndX()][move.getStartY()] = null;
        }

        moveHistory.push(move);
    }

    public void undoMove(Move move) {
        grid[move.getStartX()][move.getStartY()] = move.getMovedPiece();

        // Restore captured piece
        if (move.isEnPassant()) {
            // For en passant, the captured piece was at [endX, startY]
            grid[move.getEndX()][move.getEndY()] = null; // Clear destination
            grid[move.getEndX()][move.getStartY()] = move.getCapturedPiece();
        } else {
            grid[move.getEndX()][move.getEndY()] = move.getCapturedPiece();
        }

        // Restore hasMoved
        if (move.wasFirstMove() && move.getMovedPiece() != null) {
            move.getMovedPiece().setHasMoved(false);
        }

        // Handle Castling Undo
        if (move.isCastling()) {
            int y = move.getStartY();
            boolean kingside = move.getEndX() > move.getStartX();
            int rookStartX = kingside ? 7 : 0;
            int rookEndX = kingside ? 5 : 3;
            Piece rook = grid[rookEndX][y];
            grid[rookStartX][y] = rook;
            grid[rookEndX][y] = null;
            if (rook != null)
                rook.setHasMoved(false);
        }

        if (!moveHistory.isEmpty()) {
            moveHistory.pop();
        }
    }

    public String getFenPiecePlacement() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < 8; y++) {
            int emptyCount = 0;
            for (int x = 0; x < 8; x++) {
                Piece p = grid[x][y];
                if (p == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount);
                        emptyCount = 0;
                    }
                    sb.append(PieceRepresentation.toFenChar(p));
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount);
            }
            if (y < 7) {
                sb.append('/');
            }
        }
        return sb.toString();
    }

}
