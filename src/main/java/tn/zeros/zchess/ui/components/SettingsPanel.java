package tn.zeros.zchess.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.zeros.zchess.engine.models.*;
import tn.zeros.zchess.ui.controller.ChessController;
import tn.zeros.zchess.ui.matchmaker.GameMode;

public class SettingsPanel extends VBox {
    private final ChessController controller;
    private ComboBox<GameMode> gameModeCombo;
    private ComboBox<String> whiteModelCombo;
    private ComboBox<String> blackModelCombo;
    private ToggleGroup modelColorGroup;
    private RadioButton modelColorWhite;
    private RadioButton modelColorBlack;

    public SettingsPanel(ChessController controller) {
        this.controller = controller;
        initialize();
    }

    private void initialize() {
        gameModeCombo = new ComboBox<>();
        gameModeCombo.getItems().addAll(GameMode.values());
        gameModeCombo.getSelectionModel().select(GameMode.HUMAN_VS_HUMAN);
        gameModeCombo.setOnAction(e -> updateGameModeSettings());

        whiteModelCombo = new ComboBox<>();
        whiteModelCombo.getItems().addAll("AlphaBeta", "OrderedAlphaBeta", "MiniMax", "Random");
        whiteModelCombo.getSelectionModel().select("AlphaBeta");

        blackModelCombo = new ComboBox<>();
        blackModelCombo.getItems().addAll("AlphaBeta", "OrderedAlphaBeta", "MiniMax", "Random");
        blackModelCombo.getSelectionModel().select("OrderedAlphaBeta");

        modelColorGroup = new ToggleGroup();
        modelColorWhite = new RadioButton("White");
        modelColorBlack = new RadioButton("Black");
        modelColorWhite.setToggleGroup(modelColorGroup);
        modelColorBlack.setToggleGroup(modelColorGroup);
        modelColorGroup.selectToggle(modelColorWhite);
        modelColorGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updateGameModeSettings());

        Button applyButton = new Button("Apply Settings");
        applyButton.setOnAction(e -> applySettings());

        getChildren().addAll(
                new Label("Game Mode:"),
                gameModeCombo,
                new Label("White Model:"),
                whiteModelCombo,
                new Label("Black Model:"),
                blackModelCombo,
                new Label("Model Plays:"),
                modelColorWhite,
                modelColorBlack,
                applyButton
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
    }

    private void applySettings() {
        GameMode mode = gameModeCombo.getValue();
        String whiteModelType = whiteModelCombo.getValue();
        String blackModelType = blackModelCombo.getValue();
        boolean modelColor = modelColorGroup.getSelectedToggle() == modelColorWhite;

        EngineModel whiteModel = createEngineFromString(whiteModelType);
        EngineModel blackModel = createEngineFromString(blackModelType);

        controller.setGameMode(mode, whiteModel, blackModel, modelColor);
    }

    private EngineModel createEngineFromString(String engineType) {
        return switch (engineType) {
            case "AlphaBeta" -> new AlphaBetaModel();
            case "OrderedAlphaBeta" -> new OrderedAlphaBetaModel();
            case "MiniMax" -> new MiniMaxModel();
            case "Random" -> new RandomMoveModel();
            default -> new AlphaBetaModel();
        };
    }
}