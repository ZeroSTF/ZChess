package tn.zeros.zchess.ui.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.zeros.zchess.engine.harness.TestHarness;
import tn.zeros.zchess.engine.models.EngineModel;
import tn.zeros.zchess.engine.models.ModelV1;
import tn.zeros.zchess.engine.models.RandomMoveModel;
import tn.zeros.zchess.ui.controller.ChessController;
import tn.zeros.zchess.ui.matchmaker.GameMode;

import java.io.IOException;
import java.nio.file.Path;

import static tn.zeros.zchess.ui.util.UIConstants.DEFAULT_SEARCH_TIME_MS;

public class SettingsPanel extends VBox {
    private final ChessController controller;
    private ComboBox<GameMode> gameModeCombo;
    private ComboBox<String> whiteModelCombo;
    private ComboBox<String> blackModelCombo;
    private ToggleGroup modelColorGroup;
    private RadioButton modelColorWhite;
    private RadioButton modelColorBlack;
    private Spinner<Integer> timeSpinner;
    private Label timeLabel;

    public SettingsPanel(ChessController controller) {
        this.controller = controller;
        initialize();
    }

    private void initialize() {
        gameModeCombo = new ComboBox<>();
        gameModeCombo.getItems().addAll(GameMode.values());
        gameModeCombo.getSelectionModel().select(GameMode.HUMAN_VS_MODEL);
        gameModeCombo.setOnAction(e -> updateGameModeSettings());

        whiteModelCombo = new ComboBox<>();
        whiteModelCombo.getItems().setAll("V1", "Random");
        whiteModelCombo.getSelectionModel().select("V1");

        blackModelCombo = new ComboBox<>();
        blackModelCombo.getItems().setAll("V1", "Random");
        blackModelCombo.getSelectionModel().select("V1");

        modelColorGroup = new ToggleGroup();
        modelColorWhite = new RadioButton("White");
        modelColorBlack = new RadioButton("Black");
        modelColorWhite.setToggleGroup(modelColorGroup);
        modelColorBlack.setToggleGroup(modelColorGroup);
        modelColorGroup.selectToggle(modelColorBlack);
        modelColorGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updateGameModeSettings());

        timeLabel = new Label("Search Time:");
        timeSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20000, DEFAULT_SEARCH_TIME_MS));
        timeSpinner.setEditable(true);

        timeSpinner.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                timeSpinner.getEditor().setText(oldVal);
            }
        });

        Button applyButton = new Button("Apply Settings");
        applyButton.setOnAction(e -> applySettings());

        Button testSuiteButton = new Button("Run Test Suite");
        testSuiteButton.setOnAction(e -> runTestSuite());

        getChildren().addAll(
                new Label("Game Mode:"),
                gameModeCombo,
                new Label("White Model:"),
                whiteModelCombo,
                new Label("Black Model:"),
                blackModelCombo,
                timeLabel,
                timeSpinner,
                new Label("Model Plays:"),
                modelColorWhite,
                modelColorBlack,
                applyButton,
                testSuiteButton
        );

        setSpacing(10);
        setPadding(new Insets(10));
        setPrefWidth(250);
        updateGameModeSettings();
    }

    private void updateGameModeSettings() {
        GameMode mode = gameModeCombo.getValue();
        boolean isHumanVsModel = mode == GameMode.HUMAN_VS_MODEL;
        boolean isModelVsModel = mode == GameMode.MODEL_VS_MODEL;

        whiteModelCombo.setVisible(isModelVsModel || isHumanVsModel);
        blackModelCombo.setVisible(isModelVsModel);
        modelColorWhite.setVisible(isHumanVsModel);
        modelColorBlack.setVisible(isHumanVsModel);

        if (isHumanVsModel) {
            boolean whiteSelected = modelColorGroup.getSelectedToggle() == modelColorWhite;
            whiteModelCombo.setVisible(whiteSelected);
            blackModelCombo.setVisible(!whiteSelected);
        }
        boolean showDepth = mode != GameMode.HUMAN_VS_HUMAN;
        timeLabel.setVisible(showDepth);
        timeSpinner.setVisible(showDepth);
    }

    private void applySettings() {
        long time = timeSpinner.getValue().longValue();
        GameMode mode = gameModeCombo.getValue();
        String whiteModelType = whiteModelCombo.getValue();
        String blackModelType = blackModelCombo.getValue();
        boolean modelColor = modelColorGroup.getSelectedToggle() == modelColorWhite;

        EngineModel whiteModel = createEngineFromString(whiteModelType, time);
        EngineModel blackModel = createEngineFromString(blackModelType, time);

        controller.setGameMode(mode, whiteModel, blackModel, modelColor);
    }

    private EngineModel createEngineFromString(String engineType, long time) {
        return switch (engineType) {
            case "V1" -> new ModelV1(time);
            case "Random" -> new RandomMoveModel();
            default -> new ModelV1(time);
        };
    }

    private void runTestSuite() {
        new Thread(() -> {
            try {
                Path testPath = Path.of("test_suites/WAC.epd");
                EngineModel model = createTestModel();
                TestHarness.runTestSuite(model, testPath);
            } catch (IOException ex) {
                showErrorAlert("Test Suite Failed", ex.getMessage());
            }
        }).start();
    }

    private EngineModel createTestModel() {
        long time = timeSpinner.getValue();
        String modelType = blackModelCombo.getValue();
        return createEngineFromString(modelType, time);
    }

    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

}