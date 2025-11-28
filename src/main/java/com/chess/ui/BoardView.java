package com.chess.ui;

import com.chess.core.Board;
import com.chess.core.Game;
import com.chess.core.Move;
import com.chess.core.Piece;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

public class BoardView extends GridPane {
    private final Game game;
    private final double TILE_SIZE = 80;
    private StackPane[][] squares = new StackPane[8][8];
    private int selectedX = -1;
    private int selectedY = -1;
    private Runnable onMoveMade;

    private boolean highlightLastMove = true;
    private boolean isFlipped = false;

    public BoardView(Game game) {
        this.game = game;
        initializeBoard();
        refresh();
    }

    public void setFlipped(boolean flipped) {
        this.isFlipped = flipped;
        initializeBoard();
        refresh();
    }

    public void setOnMoveMade(Runnable onMoveMade) {
        this.onMoveMade = onMoveMade;
    }

    public void setHighlightLastMove(boolean highlight) {
        this.highlightLastMove = highlight;
    }

    public void clearSelection() {
        this.selectedX = -1;
        this.selectedY = -1;
    }

    private void initializeBoard() {
        // setStyle("-fx-background-color: #D9D9D9;");

        // Clear existing constraints
        getColumnConstraints().clear();
        getRowConstraints().clear();
        getChildren().clear(); // Clear existing children (squares and labels)

        // Setup constraints
        // Columns: 40, 80, 80, ..., 80, 40
        getColumnConstraints().add(new ColumnConstraints(40));
        for (int i = 0; i < 8; i++) {
            getColumnConstraints().add(new ColumnConstraints(TILE_SIZE));
        }
        getColumnConstraints().add(new ColumnConstraints(40));

        // Rows: 40, 80, 80, ..., 80, 40
        getRowConstraints().add(new RowConstraints(40));
        for (int i = 0; i < 8; i++) {
            getRowConstraints().add(new RowConstraints(TILE_SIZE));
        }
        getRowConstraints().add(new RowConstraints(40));

        // Add Labels
        String[] files = { "a", "b", "c", "d", "e", "f", "g", "h" };
        String[] ranks = { "8", "7", "6", "5", "4", "3", "2", "1" };
        String[] blanks = { "", "", "", "", "", "", "", "", "" };

        for (int i = 0; i < 8; i++) {
            // Top and Bottom Files
            // If flipped, 'h' is on left (col 1), 'a' is on right (col 8)
            // Normal: 'a' at col 1, 'h' at col 8
            String fileLabel = isFlipped ? files[7 - i] : files[i];
            addLabel(blanks[i], i + 1, 0);
            addLabel(fileLabel, i + 1, 9);

            // Left and Right Ranks
            // If flipped, '1' is at top (row 1), '8' is at bottom (row 8)
            // Normal: '8' at top (row 1), '1' at bottom (row 8)
            String rankLabel = isFlipped ? ranks[7 - i] : ranks[i];
            addLabel(rankLabel, 0, i + 1);
            addLabel(blanks[i], 9, i + 1);

            // Corner labels
            addLabel(blanks[i], 0, 0);
            addLabel(blanks[i], 9, 0);
            addLabel(blanks[i], 0, 9);
            addLabel(blanks[i], 9, 9);
        }

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                StackPane square = new StackPane();
                square.getStyleClass().add("board-square");
                square.getStyleClass().add(colorClassForSquare(x, y));

                Rectangle bg = new Rectangle(TILE_SIZE, TILE_SIZE);
                applySquareBackgroundStyle(bg, x, y);

                square.getChildren().add(bg);

                final int finalX = x;
                final int finalY = y;
                square.setOnMouseClicked(e -> handleSquareClick(finalX, finalY));

                squares[x][y] = square;

                // Map logical coordinates (x,y) to grid coordinates (col, row)
                // If flipped:
                // We want Rank 1 at visual top (row 1). Rank 8 at visual bottom (row 8).
                // We want File h at visual left (col 1). File a at visual right (col 8).

                // So if isFlipped:
                // Visual Col = (7 - x) + 1
                // Visual Row = (7 - y) + 1

                int gridCol = isFlipped ? (7 - x) + 1 : x + 1;
                int gridRow = isFlipped ? (7 - y) + 1 : y + 1;

                add(square, gridCol, gridRow);
            }
        }
    }

    private void addLabel(String text, int col, int row) {
        Label label = new Label(text);
        label.setAlignment(javafx.geometry.Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setStyle("-fx-background-color: #D9D9D9");
        add(label, col, row);
    }

    private void handleSquareClick(int x, int y) {
        if (selectedX == -1) {
            // Select piece
            Piece p = game.getBoard().getPiece(x, y);
            if (p != null && p.getColor() == game.getCurrentTurn()) {
                selectedX = x;
                selectedY = y;
                highlightLegalMoves(x, y);
            }
        } else {
            // Move piece
            Move move = new Move(selectedX, selectedY, x, y, game.getBoard().getPiece(selectedX, selectedY),
                    game.getBoard().getPiece(x, y));
            if (game.makeMove(move)) {
                // Animate
                StackPane sourceSquare = squares[selectedX][selectedY];

                // We need to find the piece view in the source square
                PieceView pieceView = null;
                for (javafx.scene.Node node : sourceSquare.getChildren()) {
                    if (node instanceof PieceView) {
                        pieceView = (PieceView) node;
                        break;
                    }
                }

                if (pieceView != null) {
                    // We need to find the piece view in the source square
                    // Bring source square to front so the piece renders on top of other squares
                    // during animation
                    sourceSquare.toFront();

                    // Calculate translation
                    double deltaX = (x - selectedX) * TILE_SIZE;
                    double deltaY = (y - selectedY) * TILE_SIZE;

                    if (isFlipped) {
                        deltaX = -deltaX;
                        deltaY = -deltaY;
                    }

                    TranslateTransition tt = new TranslateTransition(Duration.millis(200), pieceView);
                    tt.setByX(deltaX);
                    tt.setByY(deltaY);
                    tt.setOnFinished(evt -> resetSelectionAndRefresh());
                    tt.play();
                } else {
                    resetSelectionAndRefresh();
                }
            } else {
                // Deselect or select new piece
                Piece p = game.getBoard().getPiece(x, y);
                if (p != null && p.getColor() == game.getCurrentTurn()) {
                    selectedX = x;
                    selectedY = y;
                    refresh(); // Clear previous highlights
                    highlightLegalMoves(x, y);
                } else {
                    selectedX = -1;
                    selectedY = -1;
                    refresh();
                }
            }
        }
    }

    private void highlightLegalMoves(int x, int y) {
        refresh(); // Clear old highlights
        // Highlight selected square
        Rectangle bg = (Rectangle) squares[x][y].getChildren().get(0);
        bg.getStyleClass().add("selected");

        List<Move> moves = game.getLegalMoves(x, y);
        for (Move m : moves) {
            StackPane square = squares[m.getEndX()][m.getEndY()];
            Rectangle targetBg = (Rectangle) square.getChildren().get(0);
            // Highlight target
            targetBg.getStyleClass().add(legalMoveStyle(m.getEndX(), m.getEndY()));

        }
    }

    public void refresh() {
        Board board = game.getBoard();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                StackPane square = squares[x][y];
                square.getChildren().removeIf(node -> node instanceof PieceView);

                // Reset colors
                Rectangle bg = (Rectangle) square.getChildren().get(0);
                applySquareBackgroundStyle(bg, x, y);

                // Add piece
                Piece p = board.getPiece(x, y);
                if (p != null) {
                    square.getChildren().add(new PieceView(p));
                }
            }
        }

        // Highlight last move
        if (highlightLastMove) {
            List<Move> history = game.getMoveHistory();
            if (!history.isEmpty()) {
                Move last = history.get(history.size() - 1);
                highlightSquare(last.getStartX(), last.getStartY(), "highlight-move-source");
                highlightSquare(last.getEndX(), last.getEndY(), "highlight-move-target");
            }
        }
    }

    private void highlightSquare(int x, int y, String styleClass) {
        Rectangle bg = (Rectangle) squares[x][y].getChildren().get(0);
        bg.getStyleClass().add(styleClass);
    }

    private void resetSelectionAndRefresh() {
        selectedX = -1;
        selectedY = -1;
        highlightLastMove = true;
        refresh();
        if (onMoveMade != null) {
            onMoveMade.run();
        }
    }

    private void applySquareBackgroundStyle(Rectangle bg, int x, int y) {
        bg.getStyleClass().clear();
        bg.getStyleClass().add("board-square");
        bg.getStyleClass().add(colorClassForSquare(x, y));
    }

    private String colorClassForSquare(int x, int y) {
        return isLightSquare(x, y) ? "light" : "dark";
    }

    private String legalMoveStyle(int x, int y) {
        return isLightSquare(x, y) ? "legal-move-target-light" : "legal-move-target-dark";
    }

    private boolean isLightSquare(int x, int y) {
        return (x + y) % 2 == 0;
    }
}
