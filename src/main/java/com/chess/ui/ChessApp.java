package com.chess.ui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.chess.core.Game;
import com.chess.core.ChessClock;

public class ChessApp extends Application {

    private com.chess.engine.EngineService engineService = new com.chess.engine.EngineService();
    private String enginePath = "Serendipity.jar";
    private boolean isEngineEnabled = false;
    private com.chess.core.PieceColor engineColor = com.chess.core.PieceColor.BLACK;

    private String engineDifficulty = "Easy";
    private ChessClock chessClock = null;

    /**
     * Returns the default engine directory based on the operating system.
     * Linux/macOS: ~/.chess/engines/
     * Windows: %USERPROFILE%\.chess\engines\
     * 
     * @return the default engine directory File object
     */
    private java.io.File getDefaultEngineDirectory() {
        String userHome = System.getProperty("user.home");
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            return new java.io.File(userHome, ".chess\\engines");
        } else {
            return new java.io.File(userHome, ".chess/engines");
        }
    }

    /**
     * Returns the default engine path based on the operating system.
     * Linux/macOS: ~/.chess/engines/Serendipity.jar
     * Windows: %USERPROFILE%\.chess\engines\Serendipity.jar
     * 
     * @return the default engine path, or null if it doesn't exist
     */
    private String getDefaultEnginePath() {
        java.io.File engineFile = new java.io.File(getDefaultEngineDirectory(), "Serendipity.jar");

        if (engineFile.exists() && engineFile.isFile()) {
            return engineFile.getAbsolutePath();
        }

        return null;
    }

    /**
     * Copies the selected engine JAR to the default engine location.
     * Creates the directory structure if it doesn't exist.
     * 
     * @param sourceJar the source JAR file to copy
     * @return true if copy was successful, false otherwise
     */
    private boolean copyEngineToDefaultLocation(java.io.File sourceJar) {
        try {
            java.io.File engineDir = getDefaultEngineDirectory();

            // Create directory if it doesn't exist
            if (!engineDir.exists()) {
                engineDir.mkdirs();
            }

            // Destination file
            java.io.File destFile = new java.io.File(engineDir, "Serendipity.jar");

            // Copy the file
            java.nio.file.Files.copy(
                    sourceJar.toPath(),
                    destFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Copied engine to: " + destFile.getAbsolutePath());
            return true;
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Check for default engine location
        String defaultEnginePath = getDefaultEnginePath();
        if (defaultEnginePath != null) {
            enginePath = defaultEnginePath;
            System.out.println("Found engine at default location: " + enginePath);
        }

        BorderPane root = new BorderPane();

        Game game = new Game();

        // Create views first
        BoardView boardView = new BoardView(game);
        boardView.setAlignment(Pos.CENTER);

        SidebarView sidebarView = new SidebarView(game);

        // Define callbacks
        Runnable refreshAll = () -> {
            boardView.refresh();
            sidebarView.refresh();

            com.chess.core.GameState state = game.getGameState();
            if (state != com.chess.core.GameState.IN_PROGRESS) {
                // Stop clock if game is over
                if (chessClock != null) {
                    chessClock.stop();
                }

                String title = state == com.chess.core.GameState.CHECKMATE ? "Checkmate!" : "Stalemate";
                String content = "";
                if (state == com.chess.core.GameState.CHECKMATE) {
                    String winner = game.getCurrentTurn() == com.chess.core.PieceColor.WHITE ? "Black" : "White";
                    content = winner + " wins by Checkmate!";
                } else {
                    content = "Game drawn by Stalemate.";
                }

                // Alert alert = new Alert(Alert.AlertType.INFORMATION);
                // alert.setTitle(title);
                // alert.setHeaderText(null);
                // alert.setContentText(content);
                // alert.showAndWait();
            }
        };

        Runnable onSidebarNavigate = () -> {
            boardView.clearSelection();
            boardView.setHighlightLastMove(false);
            boardView.refresh();
            // Pause clock during navigation
            if (chessClock != null) {
                chessClock.pause();
            }
            // SidebarView refreshes itself
        };

        // Engine callback
        engineService.setOnBestMove(moveStr -> {
            javafx.application.Platform.runLater(() -> {
                System.out.println("Engine played: " + moveStr);
                boolean moved = game.makeMoveFromUCI(moveStr);
                if (moved) {
                    refreshAll.run();
                    // Switch clock to opponent's turn (same as user move callback)
                    if (chessClock != null && game.getGameState() == com.chess.core.GameState.IN_PROGRESS) {
                        chessClock.switchTurn();
                    }
                    // Check for game over or other logic here
                } else {
                    System.err.println("Engine made illegal move: " + moveStr);
                }
            });
        });

        // Set callbacks
        boardView.setOnMoveMade(() -> {
            refreshAll.run();
            // Switch clock to opponent's turn
            if (chessClock != null && game.getGameState() == com.chess.core.GameState.IN_PROGRESS) {
                chessClock.switchTurn();
            }
            if (isEngineEnabled && game.getCurrentTurn() == engineColor) {
                triggerEngine(game);
            }
        });
        sidebarView.setOnNavigate(onSidebarNavigate);

        root.setCenter(boardView);
        root.setRight(sidebarView);

        MenuBar menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem newGameItem = new MenuItem("New Game");
        MenuItem openItem = new MenuItem("Open PGN");
        MenuItem saveItem = new MenuItem("Save PGN");
        MenuItem exitItem = new MenuItem("Exit");

        fileMenu.getItems().addAll(newGameItem, new SeparatorMenuItem(), openItem, saveItem, new SeparatorMenuItem(),
                exitItem);

        // Move Menu
        Menu moveMenu = new Menu("Move");
        MenuItem previousMoveItem = new MenuItem("Previous Move");
        MenuItem nextMoveItem = new MenuItem("Next Move");
        MenuItem firstMoveItem = new MenuItem("First Move");
        MenuItem lastMoveItem = new MenuItem("Last Move");

        moveMenu.getItems().addAll(previousMoveItem, nextMoveItem, new SeparatorMenuItem(), firstMoveItem,
                lastMoveItem);

        // Engine Menu
        Menu engineMenu = new Menu("Engine");
        MenuItem loadEngineItem = new MenuItem("Load Engine JAR...");
        MenuItem startEngineItem = new MenuItem("Start Engine");
        MenuItem stopEngineItem = new MenuItem("Stop Engine");
        javafx.scene.control.CheckMenuItem playWhiteItem = new javafx.scene.control.CheckMenuItem(
                "Play as White (Engine Black)");
        javafx.scene.control.CheckMenuItem playBlackItem = new javafx.scene.control.CheckMenuItem(
                "Play as Black (Engine White)");

        playWhiteItem.setSelected(true);

        playWhiteItem.setOnAction(e -> {
            playBlackItem.setSelected(false);
            engineColor = com.chess.core.PieceColor.BLACK;
            isEngineEnabled = startEngineItem.isDisable(); // If engine started, enable play
            if (isEngineEnabled && game.getCurrentTurn() == engineColor) {
                triggerEngine(game);
            }
        });

        playBlackItem.setOnAction(e -> {
            playWhiteItem.setSelected(false);
            engineColor = com.chess.core.PieceColor.WHITE;
            isEngineEnabled = startEngineItem.isDisable();
            if (isEngineEnabled && game.getCurrentTurn() == engineColor) {
                triggerEngine(game);
            }
        });

        engineMenu.getItems().addAll(loadEngineItem, new SeparatorMenuItem(), startEngineItem, stopEngineItem,
                new SeparatorMenuItem(), playWhiteItem, playBlackItem);

        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        MenuItem githubItem = new MenuItem("GitHub Page");

        helpMenu.getItems().addAll(aboutItem, new SeparatorMenuItem(), githubItem);

        menuBar.getMenus().addAll(fileMenu, moveMenu, engineMenu, helpMenu);

        // Actions
        newGameItem.setOnAction(e -> {
            boolean engineAvailable = getDefaultEnginePath() != null || new java.io.File(enginePath).exists();
            NewGameDialog dialog = new NewGameDialog(engineAvailable);
            java.util.Optional<NewGameSettings> result = dialog.showAndWait();

            result.ifPresent(settings -> {
                game.reset();
                refreshAll.run();

                // Stop and clear any existing clock
                if (chessClock != null) {
                    chessClock.stop();
                    chessClock = null;
                    sidebarView.setClock(null);
                }

                // Create clock if time limit is enabled
                if (settings.isUseTimeLimit()) {
                    chessClock = new ChessClock(settings.getTimeMinutes());
                    sidebarView.setClock(chessClock);

                    // Set up clock callbacks
                    chessClock.setOnTimeUpdate(() -> {
                        javafx.application.Platform.runLater(() -> {
                            sidebarView.updateClockDisplay();
                        });
                    });

                    chessClock.setOnTimeExpired(expiredColor -> {
                        javafx.application.Platform.runLater(() -> {
                            chessClock.stop();
                            String winner = expiredColor == com.chess.core.PieceColor.WHITE ? "Black" : "White";
                            sidebarView.setStatusText("TIME OUT! " + winner + " wins!");
                        });
                    });

                    // Start clock for White (first player)
                    chessClock.start(com.chess.core.PieceColor.WHITE);
                }

                if (settings.isVsComputer()) {
                    isEngineEnabled = true;
                    engineDifficulty = settings.getDifficulty();
                    // Disable start engine menu item since we are auto-starting
                    startEngineItem.setDisable(true);
                    stopEngineItem.setDisable(false);

                    // Ensure engine is running
                    if (!engineService.isRunning()) {
                        try {
                            // Use default path if available, otherwise current enginePath
                            String path = getDefaultEnginePath();
                            if (path == null)
                                path = enginePath;

                            engineService.startEngine(path);
                        } catch (java.io.IOException ex) {
                            ex.printStackTrace();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Engine Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Failed to start engine: " + ex.getMessage());
                            alert.showAndWait();
                            return; // Stop here
                        }
                    }

                    if (settings.isPlayAsWhite()) {
                        engineColor = com.chess.core.PieceColor.BLACK;
                        playWhiteItem.setSelected(true);
                        playBlackItem.setSelected(false);
                    } else {
                        engineColor = com.chess.core.PieceColor.WHITE;
                        playWhiteItem.setSelected(false);
                        playBlackItem.setSelected(true);
                    }

                    if (game.getCurrentTurn() == engineColor) {
                        triggerEngine(game);
                    }
                } else {
                    // Human vs Human
                    isEngineEnabled = false;
                }

                // If playing as Black, flip the board.
                boardView.clearSelection();
                if (!settings.isPlayAsWhite()) {
                    boardView.setFlipped(true);
                } else {
                    boardView.setFlipped(false);
                }
            });
        });

        openItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PGN Files", "*.pgn"));
            java.io.File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    java.util.List<String> moves = com.chess.core.PGNService.loadGame(file);
                    boolean success = game.loadFromPGN(moves);
                    if (success) {
                        refreshAll.run();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("PGN Loaded");
                        alert.setHeaderText(null);
                        alert.setContentText("Successfully loaded " + moves.size() + " moves.");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Load Error");
                        alert.setHeaderText(null);
                        alert.setContentText(
                                "Failed to load PGN file. The file may be corrupted or contain invalid moves.");
                        alert.showAndWait();
                    }
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("File Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error reading PGN file: " + ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        saveItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PGN Files", "*.pgn"));
            java.io.File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    com.chess.core.PGNService.saveGame(game, file);
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("File Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error saving PGN file: " + ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        previousMoveItem.setOnAction(e -> {
            boardView.clearSelection();
            boardView.setHighlightLastMove(false);
            game.previousMove();
            refreshAll.run();
        });

        nextMoveItem.setOnAction(e -> {
            boardView.clearSelection();
            boardView.setHighlightLastMove(false);
            game.nextMove();
            refreshAll.run();
        });

        firstMoveItem.setOnAction(e -> {
            boardView.clearSelection();
            boardView.setHighlightLastMove(false);
            game.goToFirstMove();
            refreshAll.run();
        });

        lastMoveItem.setOnAction(e -> {
            boardView.clearSelection();
            boardView.setHighlightLastMove(false);
            game.goToLastMove();
            refreshAll.run();
        });

        loadEngineItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JAR Files", "*.jar"));
            java.io.File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                // Copy the JAR to default location
                boolean copied = copyEngineToDefaultLocation(file);

                if (copied) {
                    // Update engine path to the default location
                    enginePath = getDefaultEnginePath();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Engine Loaded");
                    alert.setHeaderText(null);
                    alert.setContentText("Engine JAR has been copied to default location:\n" + enginePath
                            + "\n\nThe engine will be automatically loaded on future launches.");
                    alert.showAndWait();
                } else {
                    // If copy failed, use the original path
                    enginePath = file.getAbsolutePath();

                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Copy Failed");
                    alert.setHeaderText(null);
                    alert.setContentText(
                            "Failed to copy engine to default location.\nUsing selected path: " + enginePath);
                    alert.showAndWait();
                }
            }
        });

        startEngineItem.setOnAction(e -> {
            try {
                engineService.startEngine(enginePath);
                isEngineEnabled = true;
                startEngineItem.setDisable(true);
                stopEngineItem.setDisable(false);
                if (game.getCurrentTurn() == engineColor) {
                    triggerEngine(game);
                }
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Engine Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to start engine: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        stopEngineItem.setOnAction(e -> {
            engineService.stopEngine();
            isEngineEnabled = false;
            startEngineItem.setDisable(false);
            stopEngineItem.setDisable(true);
        });
        stopEngineItem.setDisable(true);

        aboutItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Chessigy\nVersion 0.1\n\nCreated by Joel Ewing\n\nHorsey chess piece icons created by cham, michael1241");

            // Use custom icon
            try {
                Image icon = new Image(getClass().getResourceAsStream("/com/chess/icon/icon_64.png"));
                alert.setGraphic(new ImageView(icon));
                // Also set the window icon for the alert stage
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(icon);
            } catch (Exception ex) {
                System.err.println("Failed to load about dialog icon: " + ex.getMessage());
            }

            alert.showAndWait();
        });

        githubItem.setOnAction(e -> getHostServices().showDocument("https://github.com/joelewing/chessigy"));

        exitItem.setOnAction(e -> {
            engineService.stopEngine();
            javafx.application.Platform.exit();
        });

        root.setTop(menuBar);

        Scene scene = new Scene(root, 1100, 800);

        // Load CSS
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        // Configure stage properties BEFORE setting scene to help window manager
        primaryStage.setTitle("Chessigy");
        primaryStage.setResizable(false);

        // Set explicit size constraints to reinforce non-resizable state
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(800);
        primaryStage.setMaxWidth(1100);
        primaryStage.setMaxHeight(800);

        // Set App Icons
        try {
            primaryStage.getIcons().addAll(
                    new javafx.scene.image.Image(getClass().getResourceAsStream("/com/chess/icon/icon_16.png")),
                    new javafx.scene.image.Image(getClass().getResourceAsStream("/com/chess/icon/icon_32.png")),
                    new javafx.scene.image.Image(getClass().getResourceAsStream("/com/chess/icon/icon_64.png")),
                    new javafx.scene.image.Image(getClass().getResourceAsStream("/com/chess/icon/icon_128.png")),
                    new javafx.scene.image.Image(getClass().getResourceAsStream("/com/chess/icon/icon_256.png")),
                    new javafx.scene.image.Image(getClass().getResourceAsStream("/com/chess/icon/icon_512.png")));
        } catch (Exception e) {
            System.err.println("Failed to load app icons: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> engineService.stopEngine());
    }

    private void triggerEngine(Game game) {
        String fen = game.getFen();
        engineService.sendCommand("position fen " + fen);

        String goCommand = "go movetime 2000"; // Default
        if ("Easy".equals(engineDifficulty)) {
            goCommand = "go depth 2";
        } else if ("Medium".equals(engineDifficulty)) {
            goCommand = "go depth 6";
        } else if ("Hard".equals(engineDifficulty)) {
            goCommand = "go movetime 3000";
        }

        engineService.sendCommand(goCommand);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
