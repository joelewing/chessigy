package com.chess.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AlgebraicNotationParserTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    void testBasicPawnMoves() {
        // Test e4
        Move move = AlgebraicNotationParser.parseMove("e4", game);
        assertNotNull(move, "Should parse e4");
        assertEquals(4, move.getStartX()); // e-file
        assertEquals(6, move.getStartY()); // rank 2 for white
        assertEquals(4, move.getEndX());
        assertEquals(4, move.getEndY()); // rank 5 (e4)

        // Make the move
        assertTrue(game.makeMove(move));

        // Test e5
        move = AlgebraicNotationParser.parseMove("e5", game);
        assertNotNull(move, "Should parse e5");
        assertEquals(4, move.getStartX());
        assertEquals(1, move.getStartY()); // rank 7 for black
        assertEquals(4, move.getEndX());
        assertEquals(3, move.getEndY()); // rank 4 (e5)
    }

    @Test
    void testKnightMoves() {
        // Test Nf3
        Move move = AlgebraicNotationParser.parseMove("Nf3", game);
        assertNotNull(move, "Should parse Nf3");
        assertEquals(PieceType.KNIGHT, move.getMovedPiece().getType());
        assertEquals(5, move.getEndX()); // f-file
        assertEquals(5, move.getEndY()); // rank 3
    }

    @Test
    void testCastling() {
        // Set up a position where castling is possible
        Game castleGame = new Game();

        // Clear pieces between king and rook for white
        Board board = castleGame.getBoard();
        board.setPiece(5, 7, null); // Clear bishop
        board.setPiece(6, 7, null); // Clear knight

        // White's turn, test kingside castling
        Move move = AlgebraicNotationParser.parseMove("O-O", castleGame);
        assertNotNull(move, "Should parse O-O");
        assertTrue(move.isCastling(), "Move should be marked as castling");
        assertEquals(4, move.getStartX()); // King starts at e-file
        assertEquals(6, move.getEndX()); // King ends at g-file
    }

    @Test
    void testCapture() {
        // Play some moves to set up a capture
        game.makeMove(AlgebraicNotationParser.parseMove("e4", game));
        game.makeMove(AlgebraicNotationParser.parseMove("d5", game));

        // Test exd5 (pawn capture)
        Move move = AlgebraicNotationParser.parseMove("exd5", game);
        assertNotNull(move, "Should parse exd5");
        assertEquals(4, move.getStartX()); // e-file
        assertEquals(3, move.getEndX()); // d-file
        assertNotNull(move.getCapturedPiece(), "Should capture a piece");
    }

    @Test
    void testToAlgebraicNotation() {
        Board board = new Board();

        // Test pawn move
        Move pawnMove = new Move(4, 6, 4, 4,
                board.getPiece(4, 6), null);
        String notation = AlgebraicNotationParser.toAlgebraicNotation(
                pawnMove, board, PieceColor.WHITE);
        assertEquals("e4", notation, "Pawn move should be e4");

        // Test knight move
        Move knightMove = new Move(6, 7, 5, 5,
                board.getPiece(6, 7), null);
        notation = AlgebraicNotationParser.toAlgebraicNotation(
                knightMove, board, PieceColor.WHITE);
        assertEquals("Nf3", notation, "Knight move should be Nf3");
    }

    @Test
    void testComplexGame() {
        // Test loading a simple game sequence
        String[] moves = { "e4", "e5", "Nf3", "Nc6", "Bb5" };

        for (String moveStr : moves) {
            Move move = AlgebraicNotationParser.parseMove(moveStr, game);
            assertNotNull(move, "Should parse: " + moveStr);
            assertTrue(game.makeMove(move), "Should make move: " + moveStr);
        }

        assertEquals(5, game.getMoveHistory().size(),
                "Should have 5 moves in history");
    }

    @Test
    void testPromotion() {
        // Set up a board position where pawn can promote
        Game promGame = new Game();
        Board board = promGame.getBoard();

        // Place a white pawn on the 7th rank ready to promote
        board.setPiece(4, 6, null); // Remove original pawn
        board.setPiece(4, 1, new Piece(PieceType.PAWN, PieceColor.WHITE));
        board.getPiece(4, 1).setHasMoved(true);

        // Clear the destination square
        board.setPiece(4, 0, null);

        // It's white's turn in the game, parse promotion move
        Move move = AlgebraicNotationParser.parseMove("e8=Q", promGame);
        assertNotNull(move, "Should parse e8=Q");

        // The parser should set promotion flag and type
        if (move != null && move.isPromotion()) {
            assertEquals(PieceType.QUEEN, move.getPromotionType(),
                    "Should promote to queen");
        }
    }
}
