package com.chess.ui;

import com.chess.core.Game;
import com.chess.core.Move;
import com.chess.core.ChessClock;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class SidebarView extends VBox {
    private final Game game;
    private final VBox historyContainer;
    private Runnable onNavigate;
    private final Label statusLabel;
    private ChessClock clock;
    private VBox clockContainer;
    private Label whiteClockLabel;
    private Label blackClockLabel;

    public SidebarView(Game game) {
        this.game = game;

        setPadding(new Insets(24));
        setSpacing(20);
        setPrefWidth(300);
        setStyle("-fx-background-color: #ffffff;");

        // Status Area
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        statusLabel.setWrapText(true);

        // Clock Container (initially hidden)
        clockContainer = new VBox(10);
        clockContainer.setPadding(new Insets(10));
        clockContainer.setStyle("-fx-background-color: #FFFFFF; -fx-border-radius: 3px;");
        clockContainer.setAlignment(Pos.CENTER);
        clockContainer.setVisible(false);
        clockContainer.setManaged(false);

        // White Clock
        HBox whiteClockBox = new HBox(10);
        whiteClockLabel = new Label("5:00");
        whiteClockLabel.getStyleClass().add("clock-label");
        whiteClockLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
        whiteClockBox.getChildren().addAll(whiteClockLabel);

        // Black Clock
        HBox blackClockBox = new HBox(10);
        blackClockLabel = new Label("5:00");
        blackClockLabel.getStyleClass().add("clock-label");
        blackClockLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
        blackClockBox.getChildren().addAll(blackClockLabel);

        // Container for both clocks (side by side)
        HBox clocksRow = new HBox(10);
        clocksRow.setAlignment(Pos.CENTER);
        clocksRow.getChildren().addAll(whiteClockBox, blackClockBox);

        clockContainer.getChildren().add(clocksRow);

        // History Area
        historyContainer = new VBox();
        historyContainer.setSpacing(10);
        ScrollPane scrollPane = new ScrollPane(historyContainer);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        scrollPane.getStyleClass().add("history-scroll-pane");
        scrollPane.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: transparent; -fx-padding: 10px;");

        // Controls
        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);
        controls.setStyle(
                "-fx-background-color: #EFEFEF; -fx-border-color: transparent; -fx-padding: 10px;");

        Button btnFirst = new Button("⏪");
        Button btnPrev = new Button("◀");
        Button btnNext = new Button("▶");
        Button btnLast = new Button("⏩");

        btnFirst.setOnAction(e -> {
            game.goToFirstMove();
            if (onNavigate != null)
                onNavigate.run();
            refresh();
        });

        btnPrev.setOnAction(e -> {
            game.previousMove();
            if (onNavigate != null)
                onNavigate.run();
            refresh();
        });

        btnNext.setOnAction(e -> {
            game.nextMove();
            if (onNavigate != null)
                onNavigate.run();
            refresh();
        });

        btnLast.setOnAction(e -> {
            game.goToLastMove();
            if (onNavigate != null)
                onNavigate.run();
            refresh();
        });

        controls.getChildren().addAll(btnFirst, btnPrev, btnNext, btnLast);

        getChildren().addAll(statusLabel, clockContainer, scrollPane, controls);

    }

    public void refresh() {
        historyContainer.getChildren().clear();
        List<Move> history = game.getMoveHistory();

        int moveNum = 1;
        HBox currentLine = null;

        for (int i = 0; i < history.size(); i++) {
            Move move = history.get(i);
            if (i % 2 == 0) {
                currentLine = new HBox(20);
                Label num = new Label(moveNum + ".");
                num.setPrefWidth(30);
                currentLine.getChildren().add(num);
                historyContainer.getChildren().add(currentLine);
                moveNum++;
            }

            Label moveStr = new Label(move.toString());
            moveStr.setPrefWidth(80);
            if (i == game.getCurrentMoveIndex()) {
                moveStr.getStyleClass().add("current-move");
            }
            currentLine.getChildren().add(moveStr);
        }

        // Update Status
        com.chess.core.GameState state = game.getGameState();
        if (state == com.chess.core.GameState.CHECKMATE) {
            String winner = game.getCurrentTurn() == com.chess.core.PieceColor.WHITE ? "Black" : "White";
            statusLabel.setText("CHECKMATE! " + winner + " wins!");
            statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        } else if (state == com.chess.core.GameState.STALEMATE) {
            statusLabel.setText("STALEMATE! Draw.");
            statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        } else if (state == com.chess.core.GameState.TIME_OUT) {
        } else {
            String turn = game.getCurrentTurn() == com.chess.core.PieceColor.WHITE ? "White" : "Black";
            statusLabel.setText(turn + "'s Turn");
            statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        }
    }

    public void setOnNavigate(Runnable onNavigate) {
        this.onNavigate = onNavigate;
    }

    public void setClock(ChessClock clock) {
        this.clock = clock;
        if (clock != null) {
            clockContainer.setVisible(true);
            clockContainer.setManaged(true);
            updateClockDisplay();
        } else {
            clockContainer.setVisible(false);
            clockContainer.setManaged(false);
        }
    }

    public void updateClockDisplay() {
        if (clock != null) {
            whiteClockLabel.setText(clock.formatTime(com.chess.core.PieceColor.WHITE));
            whiteClockLabel.setStyle("-fx-text-fill: #333; -fx-background-color: #fff; -fx-padding: 16px;");
            blackClockLabel.setText(clock.formatTime(com.chess.core.PieceColor.BLACK));
            blackClockLabel.setStyle("-fx-text-fill: #fff; -fx-background-color: #333; -fx-padding: 16px;");
        }
    }

    public void setStatusText(String text) {
        statusLabel.setText(text);
    }
}
