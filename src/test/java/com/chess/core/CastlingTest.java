package com.chess.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class CastlingTest {

    @Test
    public void testWhiteKingsideCastling() {
        Board board = new Board();
        // Clear path
        board.setPiece(5, 7, null); // Bishop
        board.setPiece(6, 7, null); // Knight

        MoveValidator validator = new MoveValidator(board);
        List<Move> moves = validator.getLegalMoves(PieceColor.WHITE);

        Move castlingMove = null;
        for (Move m : moves) {
            if (m.isCastling() && m.getEndX() == 6 && m.getEndY() == 7) {
                castlingMove = m;
                break;
            }
        }

        assertNotNull(castlingMove, "White Kingside castling move should be available");

        // Execute
        board.movePiece(castlingMove);

        // Verify King
        Piece k = board.getPiece(6, 7);
        assertNotNull(k, "King should be at 6,7");
        assertEquals(PieceType.KING, k.getType());

        // Verify Rook
        Piece r = board.getPiece(5, 7);
        assertNotNull(r, "Rook should be at 5,7");
        assertEquals(PieceType.ROOK, r.getType());

        // Undo
        board.undoMove(castlingMove);
        assertEquals(PieceType.KING, board.getPiece(4, 7).getType(), "Undo should restore King");
        assertEquals(PieceType.ROOK, board.getPiece(7, 7).getType(), "Undo should restore Rook");
    }

    @Test
    public void testWhiteQueensideCastling() {
        Board board = new Board();
        board.setPiece(1, 7, null);
        board.setPiece(2, 7, null);
        board.setPiece(3, 7, null);

        MoveValidator validator = new MoveValidator(board);
        List<Move> moves = validator.getLegalMoves(PieceColor.WHITE);

        Move castlingMove = null;
        for (Move m : moves) {
            if (m.isCastling() && m.getEndX() == 2 && m.getEndY() == 7) {
                castlingMove = m;
                break;
            }
        }
        assertNotNull(castlingMove, "White Queenside castling move should be available");

        board.movePiece(castlingMove);
        assertEquals(PieceType.KING, board.getPiece(2, 7).getType());
        assertEquals(PieceType.ROOK, board.getPiece(3, 7).getType());

        board.undoMove(castlingMove);
        assertEquals(PieceType.KING, board.getPiece(4, 7).getType());
        assertEquals(PieceType.ROOK, board.getPiece(0, 7).getType());
    }

    @Test
    public void testRightsLost() {
        Board board = new Board();
        board.setPiece(5, 7, null);
        board.setPiece(6, 7, null);

        Piece king = board.getPiece(4, 7);
        king.setHasMoved(true);

        MoveValidator validator = new MoveValidator(board);
        List<Move> moves = validator.getLegalMoves(PieceColor.WHITE);

        for (Move m : moves) {
            assertFalse(m.isCastling(), "Castling should not be allowed after King moved");
        }
    }

    @Test
    public void testPathBlocked() {
        Board board = new Board();
        board.setPiece(5, 7, null);
        // Knight at 6,7 still there

        MoveValidator validator = new MoveValidator(board);
        List<Move> moves = validator.getLegalMoves(PieceColor.WHITE);

        for (Move m : moves) {
            if (m.isCastling() && m.getEndX() == 6) {
                fail("Castling allowed through block");
            }
        }
    }

    @Test
    public void testInCheck() {
        Board board = new Board();
        board.setPiece(5, 7, null);
        board.setPiece(6, 7, null);

        // Place enemy rook attacking king
        // We need to clear the pawn in front of the king first (at 4,6)
        board.setPiece(4, 6, null);
        board.setPiece(4, 5, new Piece(PieceType.ROOK, PieceColor.BLACK));

        MoveValidator validator = new MoveValidator(board);
        assertTrue(validator.isKingInCheck(PieceColor.WHITE), "King should be in check");

        List<Move> moves = validator.getLegalMoves(PieceColor.WHITE);
        for (Move m : moves) {
            assertFalse(m.isCastling(), "Castling should not be allowed while in check");
        }
    }

    @Test
    public void testThroughCheck() {
        Board board = new Board();
        board.setPiece(5, 7, null);
        board.setPiece(6, 7, null);

        // Place enemy rook attacking square 5,7 (which king crosses)
        // We need to clear the pawn at 5,6 so the rook can attack 5,7
        board.setPiece(5, 6, null);
        board.setPiece(5, 5, new Piece(PieceType.ROOK, PieceColor.BLACK));

        MoveValidator validator = new MoveValidator(board);
        List<Move> moves = validator.getLegalMoves(PieceColor.WHITE);
        for (Move m : moves) {
            if (m.isCastling() && m.getEndX() == 6) {
                fail("Castling allowed through check");
            }
        }
    }
}
