package com.chess.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class NewGameDialog extends Dialog<NewGameSettings> {

    public NewGameDialog(boolean isEngineAvailable) {
        setTitle("New Game");
        setHeaderText(null);

        ButtonType startButtonType = new ButtonType("Start", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, startButtonType);

        javafx.scene.Node startButton = getDialogPane().lookupButton(startButtonType);
        if (startButton != null) {
            startButton.getStyleClass().add("start-button");
        }

        // Load CSS
        getDialogPane().getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        getDialogPane().getStyleClass().add("new-game-dialog");

        // --- Main Layout ---
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setPrefWidth(400);

        // --- Game Settings Section ---
        GridPane gameSettingsGrid = new GridPane();
        gameSettingsGrid.setHgap(10);
        gameSettingsGrid.setVgap(15);
        gameSettingsGrid.setAlignment(Pos.CENTER_LEFT);

        // Opposing Player
        Label opponentLabel = new Label("Opposing Player");
        ComboBox<String> opponentCombo = new ComboBox<>();
        opponentCombo.getStyleClass().add("flat-combo");
        opponentCombo.getItems().add("Human");
        if (isEngineAvailable) {
            opponentCombo.getItems().add("Serendipity");
        }
        // Default to Serendipity if available, else Human
        if (isEngineAvailable) {
            opponentCombo.setValue("Serendipity");
        } else {
            opponentCombo.setValue("Human");
        }
        opponentCombo.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(opponentCombo, javafx.scene.layout.Priority.ALWAYS);

        gameSettingsGrid.add(opponentLabel, 0, 0);
        gameSettingsGrid.add(opponentCombo, 1, 0);

        // Play As
        Label playAsLabel = new Label("Play As");
        ComboBox<String> playAsCombo = new ComboBox<>();
        playAsCombo.getStyleClass().add("flat-combo");
        playAsCombo.getItems().addAll("White", "Black");
        playAsCombo.setValue("White");
        playAsCombo.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(playAsCombo, javafx.scene.layout.Priority.ALWAYS);

        gameSettingsGrid.add(playAsLabel, 0, 1);
        gameSettingsGrid.add(playAsCombo, 1, 1);

        // Difficulty
        Label difficultyLabel = new Label("Difficulty");
        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getStyleClass().add("flat-combo");
        difficultyCombo.getItems().addAll("Easy", "Medium", "Hard");
        difficultyCombo.setValue("Easy");
        difficultyCombo.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(difficultyCombo, javafx.scene.layout.Priority.ALWAYS);

        gameSettingsGrid.add(difficultyLabel, 0, 2);
        gameSettingsGrid.add(difficultyCombo, 1, 2);

        // Disable Play As and Difficulty if Human vs Human
        opponentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isComputer = "Serendipity".equals(newVal);
            difficultyCombo.setDisable(!isComputer);
        });
        // Initial state
        difficultyCombo.setDisable(!"Serendipity".equals(opponentCombo.getValue()));

        // --- Time Limit Section ---
        VBox timeLimitBox = new VBox(15);

        Label timeLimitHeader = new Label("Time Limit");
        timeLimitHeader.setFont(Font.font("System", FontWeight.BOLD, 14));

        GridPane timeSettingsGrid = new GridPane();
        timeSettingsGrid.setHgap(10);
        timeSettingsGrid.setVgap(15);
        timeSettingsGrid.setAlignment(Pos.CENTER_LEFT);

        // Use Time Limit Toggle
        Label useTimeLimitLabel = new Label("Use Time Limit");
        CheckBox useTimeLimitToggle = new CheckBox();
        useTimeLimitToggle.getStyleClass().add("flat-checkbox");
        useTimeLimitToggle.setSelected(false);

        // Align toggle to right
        HBox toggleBox = new HBox(useTimeLimitToggle);
        toggleBox.setAlignment(Pos.CENTER_RIGHT);

        timeSettingsGrid.add(useTimeLimitLabel, 0, 0);
        timeSettingsGrid.add(toggleBox, 1, 0);
        GridPane.setHgrow(toggleBox, javafx.scene.layout.Priority.ALWAYS);

        // Minutes Per Side
        Label minutesLabel = new Label("Minutes Per Side");
        Spinner<Integer> minutesSpinner = new Spinner<>();
        minutesSpinner.getStyleClass().add("flat-spinner");
        minutesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 180, 5));
        minutesSpinner.setEditable(true);
        minutesSpinner.setMaxWidth(Double.MAX_VALUE);

        timeSettingsGrid.add(minutesLabel, 0, 1);
        timeSettingsGrid.add(minutesSpinner, 1, 1);
        // Enable/Disable time settings based on toggle
        minutesSpinner.disableProperty().bind(useTimeLimitToggle.selectedProperty().not());

        timeLimitBox.getChildren().addAll(timeLimitHeader, timeSettingsGrid);

        // Add all to main layout
        mainLayout.getChildren().addAll(gameSettingsGrid, new javafx.scene.control.Separator(), timeLimitBox);

        getDialogPane().setContent(mainLayout);

        // Fix window decoration button issues on Linux
        // Configure stage properties before dialog is shown
        javafx.stage.Stage dialogStage = (javafx.stage.Stage) getDialogPane().getScene().getWindow();
        if (dialogStage != null) {
            dialogStage.setResizable(false);
            // Set explicit size constraints to reinforce non-resizable state
            dialogStage.setMinWidth(dialogStage.getWidth());
            dialogStage.setMinHeight(dialogStage.getHeight());
            dialogStage.setMaxWidth(dialogStage.getWidth());
            dialogStage.setMaxHeight(dialogStage.getHeight());
        } else {
            // If stage is not available yet, set it up when the dialog is shown
            getDialogPane().sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null && newScene.getWindow() != null) {
                    javafx.stage.Stage stage = (javafx.stage.Stage) newScene.getWindow();
                    stage.setResizable(false);
                    // Use a listener to set size constraints after stage is fully initialized
                    stage.widthProperty().addListener((obsWidth, oldWidth, newWidth) -> {
                        if (newWidth.doubleValue() > 0) {
                            stage.setMinWidth(newWidth.doubleValue());
                            stage.setMaxWidth(newWidth.doubleValue());
                        }
                    });
                    stage.heightProperty().addListener((obsHeight, oldHeight, newHeight) -> {
                        if (newHeight.doubleValue() > 0) {
                            stage.setMinHeight(newHeight.doubleValue());
                            stage.setMaxHeight(newHeight.doubleValue());
                        }
                    });
                }
            });
        }

        // Convert result
        setResultConverter(dialogButton -> {
            if (dialogButton == startButtonType) {
                return new NewGameSettings(
                        "Serendipity".equals(opponentCombo.getValue()),
                        "White".equals(playAsCombo.getValue()),
                        difficultyCombo.getValue(),
                        useTimeLimitToggle.isSelected(),
                        minutesSpinner.getValue(),
                        0,
                        "standard");
            }
            return null;
        });
    }
}
