package tn.zeros.zchess.ui.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.zeros.zchess.engine.models.*;
import tn.zeros.zchess.engine.search.SearchService;
import tn.zeros.zchess.engine.util.TestHarness;
import tn.zeros.zchess.ui.controller.ChessController;
import tn.zeros.zchess.ui.matchmaker.GameMode;

import java.io.IOException;
import java.nio.file.Path;

public class SettingsPanel extends VBox {
    private final ChessController controller;
    private ComboBox<GameMode> gameModeCombo;
    private ComboBox<String> whiteModelCombo;
    private ComboBox<String> blackModelCombo;
    private ToggleGroup modelColorGroup;
    private RadioButton modelColorWhite;
    private RadioButton modelColorBlack;
    private Spinner<Integer> depthSpinner;
    private Label depthLabel;

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
        whiteModelCombo.getItems().setAll("AlphaBeta", "OrderedAlphaBeta", "MiniMax", "Random");
        whiteModelCombo.getSelectionModel().select("AlphaBeta");

        blackModelCombo = new ComboBox<>();
        blackModelCombo.getItems().setAll("AlphaBeta", "OrderedAlphaBeta", "MiniMax", "Random");
        blackModelCombo.getSelectionModel().select("OrderedAlphaBeta");

        modelColorGroup = new ToggleGroup();
        modelColorWhite = new RadioButton("White");
        modelColorBlack = new RadioButton("Black");
        modelColorWhite.setToggleGroup(modelColorGroup);
        modelColorBlack.setToggleGroup(modelColorGroup);
        modelColorGroup.selectToggle(modelColorBlack);
        modelColorGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updateGameModeSettings());

        depthLabel = new Label("Search Depth:");
        depthSpinner = new Spinner<>(1, 10, 4); // Min:1, Max:8, Default:3
        depthSpinner.setEditable(true);

        depthSpinner.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                depthSpinner.getEditor().setText(oldVal);
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
                depthLabel,
                depthSpinner,
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
        depthLabel.setVisible(showDepth);
        depthSpinner.setVisible(showDepth);
    }

    private void applySettings() {
        int maxDepth = depthSpinner.getValue();
        GameMode mode = gameModeCombo.getValue();
        String whiteModelType = whiteModelCombo.getValue();
        String blackModelType = blackModelCombo.getValue();
        boolean modelColor = modelColorGroup.getSelectedToggle() == modelColorWhite;

        EngineModel whiteModel = createEngineFromString(whiteModelType, maxDepth);
        EngineModel blackModel = createEngineFromString(blackModelType, maxDepth);

        controller.setGameMode(mode, whiteModel, blackModel, modelColor);
    }

    private EngineModel createEngineFromString(String engineType, int maxDepth) {
        SearchService searchService = new SearchService();

        return switch (engineType) {
            case "AlphaBeta" -> new AlphaBetaModel(searchService, maxDepth);
            case "OrderedAlphaBeta" -> new OrderedAlphaBetaModel(searchService, maxDepth);
            case "MiniMax" -> new MiniMaxModel(searchService, maxDepth);
            case "Random" -> new RandomMoveModel();
            default -> new AlphaBetaModel(searchService, maxDepth);
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
        int depth = depthSpinner.getValue();
        String modelType = blackModelCombo.getValue();
        return createEngineFromString(modelType, depth);
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