package com.chess.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PGNServiceTest {

    @TempDir
    Path tempDir;

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    void testSaveAndLoadSimpleGame() throws IOException {
        // Play a simple game
        game.makeMove(AlgebraicNotationParser.parseMove("e4", game));
        game.makeMove(AlgebraicNotationParser.parseMove("e5", game));
        game.makeMove(AlgebraicNotationParser.parseMove("Nf3", game));
        game.makeMove(AlgebraicNotationParser.parseMove("Nc6", game));

        // Save to file
        File pgnFile = tempDir.resolve("test_game.pgn").toFile();
        PGNService.saveGame(game, pgnFile);

        assertTrue(pgnFile.exists(), "PGN file should be created");

        // Read the file and verify it contains proper algebraic notation
        String content = Files.readString(pgnFile.toPath());
        assertTrue(content.contains("e4"), "Should contain e4");
        assertTrue(content.contains("e5"), "Should contain e5");
        assertTrue(content.contains("Nf3"), "Should contain Nf3");
        assertTrue(content.contains("Nc6"), "Should contain Nc6");

        // Load the game back
        List<String> moves = PGNService.loadGame(pgnFile);
        assertEquals(4, moves.size(), "Should load 4 moves");

        // Create a new game and load the moves
        Game loadedGame = new Game();
        assertTrue(loadedGame.loadFromPGN(moves), "Should successfully load PGN");

        // Verify the board state matches
        Board originalBoard = game.getBoard();
        Board loadedBoard = loadedGame.getBoard();

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece original = originalBoard.getPiece(x, y);
                Piece loaded = loadedBoard.getPiece(x, y);

                if (original == null) {
                    assertNull(loaded, "Square " + x + "," + y + " should be empty");
                } else {
                    assertNotNull(loaded, "Square " + x + "," + y + " should have a piece");
                    assertEquals(original.getType(), loaded.getType(),
                            "Piece types should match at " + x + "," + y);
                    assertEquals(original.getColor(), loaded.getColor(),
                            "Piece colors should match at " + x + "," + y);
                }
            }
        }
    }

    @Test
    void testLoadStandardPGN() throws IOException {
        // Create a standard PGN file
        String pgnContent = """
                [Event "Test Game"]
                [Site "Local"]
                [Date "2025.11.23"]
                [White "Player 1"]
                [Black "Player 2"]
                [Result "*"]

                1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 *
                """;

        File pgnFile = tempDir.resolve("standard.pgn").toFile();
        Files.writeString(pgnFile.toPath(), pgnContent);

        // Load the moves
        List<String> moves = PGNService.loadGame(pgnFile);
        assertEquals(6, moves.size(), "Should parse 6 moves");
        assertEquals("e4", moves.get(0));
        assertEquals("e5", moves.get(1));
        assertEquals("Nf3", moves.get(2));
        assertEquals("Nc6", moves.get(3));
        assertEquals("Bb5", moves.get(4));
        assertEquals("a6", moves.get(5));

        // Load into a game
        Game newGame = new Game();
        assertTrue(newGame.loadFromPGN(moves), "Should load successfully");
        assertEquals(6, newGame.getMoveHistory().size(),
                "Move history should have 6 moves");
    }

    @Test
    void testRoundTrip() throws IOException {
        // Play a game with various move types
        playTestGame(game);

        // Save the game
        File pgnFile = tempDir.resolve("roundtrip.pgn").toFile();
        PGNService.saveGame(game, pgnFile);

        // Load it back
        List<String> moves = PGNService.loadGame(pgnFile);
        Game reloadedGame = new Game();
        assertTrue(reloadedGame.loadFromPGN(moves),
                "Round-trip should succeed");

        // Verify same number of moves
        assertEquals(game.getMoveHistory().size(),
                reloadedGame.getMoveHistory().size(),
                "Move counts should match");
    }

    private void playTestGame(Game g) {
        // Scholar's Mate opening
        String[] moves = {
                "e4", "e5",
                "Bc4", "Nc6",
                "Qh5", "Nf6"
        };

        for (String moveStr : moves) {
            Move move = AlgebraicNotationParser.parseMove(moveStr, g);
            if (move != null) {
                g.makeMove(move);
            }
        }
    }
}
