package com.chess.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PGNService {

    public static void saveGame(Game game, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write headers (simplified)
            writer.write("[Event \"Local Game\"]\n");
            writer.write("[Site \"Local\"]\n");
            writer.write("[Date \"" + java.time.LocalDate.now() + "\"]\n");
            writer.write("[White \"Player 1\"]\n");
            writer.write("[Black \"Player 2\"]\n");
            writer.write("[Result \"*\"]\n\n");

            // Write moves in proper algebraic notation
            List<Move> history = game.getMoveHistory();
            Board board = new Board(); // Use a separate board to track state for notation
            int moveNum = 1;
            for (int i = 0; i < history.size(); i++) {
                Move move = history.get(i);
                PieceColor color = (i % 2 == 0) ? PieceColor.WHITE : PieceColor.BLACK;

                if (i % 2 == 0) {
                    writer.write(moveNum + ". ");
                }

                // Convert to algebraic notation
                String notation = move.toAlgebraicNotation(board, color);
                writer.write(notation + " ");

                // Apply the move to our tracking board
                board.movePiece(move);

                if (i % 2 == 1) {
                    moveNum++;
                }
            }
            writer.write("*\n");
        }
    }

    public static List<String> loadGame(File file) throws IOException {
        // This is a very basic PGN parser. It assumes standard formatting.
        // Returns a list of move strings (e.g. "e2e4", "e7e5") or algebraic.
        List<String> moves = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            StringBuilder pgnBody = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("["))
                    continue;
                if (line.trim().isEmpty())
                    continue;
                pgnBody.append(line).append(" ");
            }

            String body = pgnBody.toString();
            // Remove comments {}
            body = body.replaceAll("\\{.*?\\}", "");
            // Remove move numbers 1. 2. etc
            body = body.replaceAll("\\d+\\.", "");
            // Remove result
            body = body.replace("*", "");

            String[] tokens = body.trim().split("\\s+");
            for (String token : tokens) {
                if (!token.isEmpty()) {
                    moves.add(token);
                }
            }
        }
        return moves;
    }

}
