package com.chess.core;

import java.util.ArrayList;
import java.util.List;

public class MoveValidator {
    private final Board board;

    private static final int[][] ORTHOGONAL_DIRECTIONS = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
    private static final int[][] DIAGONAL_DIRECTIONS = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
    private static final int[][] QUEEN_DIRECTIONS = {
            { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 },
            { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };

    public MoveValidator(Board board) {
        this.board = board;
    }

    public List<Move> getLegalMoves(PieceColor color) {
        List<Move> moves = new ArrayList<>();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece p = board.getPiece(x, y);
                if (p != null && p.getColor() == color) {
                    moves.addAll(getPseudoLegalMoves(x, y, p, true));
                }
            }
        }

        // Filter out moves that leave the king in check
        List<Move> legalMoves = new ArrayList<>();
        for (Move move : moves) {
            if (!leavesKingInCheck(move, color)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    private List<Move> getPseudoLegalMoves(int x, int y, Piece p, boolean includeCastling) {
        List<Move> moves = new ArrayList<>();
        switch (p.getType()) {
            case PAWN:
                addPawnMoves(x, y, p, moves);
                break;
            case ROOK:
                addSlidingMoves(x, y, p, moves, ORTHOGONAL_DIRECTIONS);
                break;
            case BISHOP:
                addSlidingMoves(x, y, p, moves, DIAGONAL_DIRECTIONS);
                break;
            case QUEEN:
                addSlidingMoves(x, y, p, moves, QUEEN_DIRECTIONS);
                break;
            case KNIGHT:
                addKnightMoves(x, y, p, moves);
                break;
            case KING:
                addKingMoves(x, y, p, moves, includeCastling);
                break;
        }
        return moves;
    }

    private void addPawnMoves(int x, int y, Piece p, List<Move> moves) {
        int direction = p.getColor() == PieceColor.WHITE ? -1 : 1;
        int startRank = p.getColor() == PieceColor.WHITE ? 6 : 1;

        // Move forward 1
        int nextY = y + direction;
        if (BoardCoordinates.isValid(x, nextY) && board.getPiece(x, nextY) == null) {
            moves.add(new Move(x, y, x, nextY, p, null));

            // Move forward 2
            int nextY2 = y + (direction * 2);
            if (y == startRank && BoardCoordinates.isValid(x, nextY2) && board.getPiece(x, nextY2) == null) {
                moves.add(new Move(x, y, x, nextY2, p, null));
            }
        }

        // Captures
        int[] captureOffsets = { -1, 1 };
        for (int offset : captureOffsets) {
            int captureX = x + offset;
            if (BoardCoordinates.isValid(captureX, nextY)) {
                Piece target = board.getPiece(captureX, nextY);
                if (target != null && target.getColor() != p.getColor()) {
                    moves.add(new Move(x, y, captureX, nextY, p, target));
                }
            }
        }

        addEnPassantMoves(x, y, p, moves);
    }

    private void addEnPassantMoves(int x, int y, Piece p, List<Move> moves) {
        // 1. Rank requirement: White on rank 5 (index 3), Black on rank 4 (index 4)
        int requiredRank = p.getColor() == PieceColor.WHITE ? 3 : 4;
        if (y != requiredRank) {
            return;
        }

        // 2. Check last move
        Move lastMove = board.getLastMove();
        if (lastMove == null) {
            return;
        }

        // 3. Last move must be a double pawn push
        Piece lastMovedPiece = lastMove.getMovedPiece();
        if (lastMovedPiece.getType() != PieceType.PAWN || Math.abs(lastMove.getStartY() - lastMove.getEndY()) != 2) {
            return;
        }

        // 4. Positioning: Target pawn must be horizontally adjacent
        if (lastMove.getEndY() != y || Math.abs(lastMove.getEndX() - x) != 1) {
            return;
        }

        // 5. Add en passant move
        int direction = p.getColor() == PieceColor.WHITE ? -1 : 1;
        int targetX = lastMove.getEndX();
        int targetY = y + direction;

        // The captured piece is the one that just moved
        Piece capturedPiece = lastMovedPiece;

        Move epMove = new Move(x, y, targetX, targetY, p, capturedPiece);
        epMove.setEnPassant(true);
        moves.add(epMove);
    }

    private void addSlidingMoves(int x, int y, Piece p, List<Move> moves, int[][] directions) {
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            while (BoardCoordinates.isValid(nx, ny)) {
                Piece target = board.getPiece(nx, ny);
                if (target == null) {
                    moves.add(new Move(x, y, nx, ny, p, null));
                } else {
                    if (target.getColor() != p.getColor()) {
                        moves.add(new Move(x, y, nx, ny, p, target));
                    }
                    break;
                }
                nx += dir[0];
                ny += dir[1];
            }
        }
    }

    private void addKnightMoves(int x, int y, Piece p, List<Move> moves) {
        int[][] offsets = { { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 }, { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 } };
        for (int[] o : offsets) {
            int nx = x + o[0];
            int ny = y + o[1];
            if (BoardCoordinates.isValid(nx, ny)) {
                Piece target = board.getPiece(nx, ny);
                if (target == null || target.getColor() != p.getColor()) {
                    moves.add(new Move(x, y, nx, ny, p, target));
                }
            }
        }
    }

    private void addKingMoves(int x, int y, Piece p, List<Move> moves, boolean includeCastling) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                int nx = x + dx;
                int ny = y + dy;
                if (BoardCoordinates.isValid(nx, ny)) {
                    Piece target = board.getPiece(nx, ny);
                    if (target == null || target.getColor() != p.getColor()) {
                        moves.add(new Move(x, y, nx, ny, p, target));
                    }
                }
            }
        }

        if (includeCastling) {
            addCastlingMoves(x, y, p, moves);
        }
    }

    private void addCastlingMoves(int x, int y, Piece p, List<Move> moves) {
        if (p.hasMoved())
            return;

        // Kingside
        if (canCastle(x, y, p, true)) {
            Move m = new Move(x, y, x + 2, y, p, null);
            m.setCastling(true);
            moves.add(m);
        }
        // Queenside
        if (canCastle(x, y, p, false)) {
            Move m = new Move(x, y, x - 2, y, p, null);
            m.setCastling(true);
            moves.add(m);
        }
    }

    private boolean canCastle(int x, int y, Piece p, boolean kingside) {
        // Check if King is in check
        if (isKingInCheck(p.getColor()))
            return false;

        int rookX = kingside ? 7 : 0;
        int direction = kingside ? 1 : -1;

        // Check Rook
        Piece rook = board.getPiece(rookX, y);
        if (rook == null || rook.getType() != PieceType.ROOK || rook.getColor() != p.getColor() || rook.hasMoved()) {
            return false;
        }

        // Check empty squares between
        // For kingside (x=4 to 7), check 5, 6.
        // For queenside (x=4 to 0), check 3, 2, 1.
        int steps = Math.abs(rookX - x);
        for (int i = 1; i < steps; i++) {
            if (board.getPiece(x + (i * direction), y) != null)
                return false;
        }

        // Check if path is attacked (current square checked above, need to check
        // squares king crosses)
        // King moves 2 squares.
        if (isSquareAttacked(x + direction, y, p.getColor()))
            return false;
        if (isSquareAttacked(x + (direction * 2), y, p.getColor()))
            return false;

        return true;
    }

    private boolean leavesKingInCheck(Move move, PieceColor color) {
        // Make move
        board.movePiece(move);

        // Check if king is in check
        boolean inCheck = isKingInCheck(color);

        // Undo move
        board.undoMove(move);

        return inCheck;
    }

    public boolean isKingInCheck(PieceColor color) {
        // Find king
        int kx = -1, ky = -1;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece p = board.getPiece(x, y);
                if (p != null && p.getType() == PieceType.KING && p.getColor() == color) {
                    kx = x;
                    ky = y;
                    break;
                }
            }
        }

        if (kx == -1)
            return false; // Should not happen

        return isSquareAttacked(kx, ky, color);
    }

    public boolean isSquareAttacked(int targetX, int targetY, PieceColor friendlyColor) {
        PieceColor opponent = friendlyColor.opposite();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece p = board.getPiece(x, y);
                if (p != null && p.getColor() == opponent) {
                    // Get pseudo moves for this piece, excluding castling to avoid recursion
                    List<Move> attacks = getPseudoLegalMoves(x, y, p, false);
                    for (Move m : attacks) {
                        if (m.getEndX() == targetX && m.getEndY() == targetY) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
