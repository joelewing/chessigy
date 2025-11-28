package com.chess.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class EnPassantTest {

    @Test
    public void testWhiteEnPassant() {
        Board board = new Board();
        board.resetBoard();

        // Setup: White pawn at 3,3 (rank 5), Black pawn moves 4,1 to 4,3
        // Clear board for simplicity
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                board.setPiece(x, y, null);
            }
        }

        Piece whitePawn = new Piece(PieceType.PAWN, PieceColor.WHITE);
        whitePawn.setHasMoved(true);
        board.setPiece(3, 3, whitePawn);

        Piece blackPawn = new Piece(PieceType.PAWN, PieceColor.BLACK);
        board.setPiece(4, 1, blackPawn);

        // Black moves double
        Move blackMove = new Move(4, 1, 4, 3, blackPawn, null);
        board.movePiece(blackMove);

        MoveValidator validator = new MoveValidator(board);
        List<Move> moves = validator.getLegalMoves(PieceColor.WHITE);

        Move epMove = null;
        for (Move m : moves) {
            if (m.isEnPassant()) {
                epMove = m;
                break;
            }
        }

        assertNotNull(epMove, "En passant move should be available");
        assertEquals(3, epMove.getStartX());
        assertEquals(3, epMove.getStartY());
        assertEquals(4, epMove.getEndX());
        assertEquals(2, epMove.getEndY()); // Target square behind black pawn

        // Execute
        board.movePiece(epMove);
        assertNull(board.getPiece(4, 3), "Captured pawn should be removed");
        assertEquals(whitePawn, board.getPiece(4, 2), "White pawn should be at destination");

        // Undo
        board.undoMove(epMove);
        assertEquals(blackPawn, board.getPiece(4, 3), "Captured pawn should be restored");
        assertEquals(whitePawn, board.getPiece(3, 3), "White pawn should be back");
    }

    @Test
    public void testBlackEnPassant() {
        Board board = new Board();
        // Clear board
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                board.setPiece(x, y, null);
            }
        }

        Piece blackPawn = new Piece(PieceType.PAWN, PieceColor.BLACK);
        blackPawn.setHasMoved(true);
        board.setPiece(4, 4, blackPawn); // Rank 4

        Piece whitePawn = new Piece(PieceType.PAWN, PieceColor.WHITE);
        board.setPiece(3, 6, whitePawn);

        // White moves double
        Move whiteMove = new Move(3, 6, 3, 4, whitePawn, null);
        board.movePiece(whiteMove);

        MoveValidator validator = new MoveValidator(board);
        List<Move> moves = validator.getLegalMoves(PieceColor.BLACK);

        Move epMove = null;
        for (Move m : moves) {
            if (m.isEnPassant()) {
                epMove = m;
                break;
            }
        }

        assertNotNull(epMove, "En passant move should be available for Black");
        assertEquals(4, epMove.getStartX());
        assertEquals(4, epMove.getStartY());
        assertEquals(3, epMove.getEndX());
        assertEquals(5, epMove.getEndY()); // Target square behind white pawn

        board.movePiece(epMove);
        assertNull(board.getPiece(3, 4));
        assertEquals(blackPawn, board.getPiece(3, 5));
    }

    @Test
    public void testEnPassantExpired() {
        Board board = new Board();
        // Clear board
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                board.setPiece(x, y, null);
            }
        }

        Piece whitePawn = new Piece(PieceType.PAWN, PieceColor.WHITE);
        whitePawn.setHasMoved(true);
        board.setPiece(3, 3, whitePawn);

        Piece blackPawn = new Piece(PieceType.PAWN, PieceColor.BLACK);
        board.setPiece(4, 1, blackPawn);

        // Black moves double
        Move blackMove = new Move(4, 1, 4, 3, blackPawn, null);
        board.movePiece(blackMove);

        // White makes a different move (e.g. king move)
        Piece king = new Piece(PieceType.KING, PieceColor.WHITE);
        board.setPiece(0, 0, king);
        Move otherMove = new Move(0, 0, 0, 1, king, null);
        board.movePiece(otherMove);

        MoveValidator validator = new MoveValidator(board);
        // It's Black's turn now technically, but let's check White's moves as if it
        // were their turn (ignoring turn order for unit test of move generation)
        List<Move> moves = validator.getLegalMoves(PieceColor.WHITE);
        for (Move m : moves) {
            assertFalse(m.isEnPassant(), "En passant should be expired");
        }
    }

    @Test
    public void testEnPassantWrongRank() {
        Board board = new Board();
        // Clear board
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                board.setPiece(x, y, null);
            }
        }

        Piece whitePawn = new Piece(PieceType.PAWN, PieceColor.WHITE);
        whitePawn.setHasMoved(true);
        board.setPiece(3, 2, whitePawn); // Rank 6 (too far back)

        Piece blackPawn = new Piece(PieceType.PAWN, PieceColor.BLACK);
        board.setPiece(4, 1, blackPawn);

        // Black moves double
        Move blackMove = new Move(4, 1, 4, 3, blackPawn, null);
        board.movePiece(blackMove);

        MoveValidator validator = new MoveValidator(board);
        List<Move> moves = validator.getLegalMoves(PieceColor.WHITE);

        for (Move m : moves) {
            assertFalse(m.isEnPassant(), "En passant should not be available from wrong rank");
        }
    }
}
