package tn.zeros.zchess;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.zeros.zchess.ui.components.ControlPanel;
import tn.zeros.zchess.ui.components.SettingsPanel;
import tn.zeros.zchess.ui.controller.ChessController;
import tn.zeros.zchess.ui.util.SoundManager;
import tn.zeros.zchess.ui.view.ChessBoardView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ZChessApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Preload sounds
        SoundManager.initialize();

        // Initialize core components
        ChessController controller = new ChessController();

        // Create UI components
        ChessBoardView boardView = new ChessBoardView(controller);
        SettingsPanel settingsPanel = new SettingsPanel(controller);
        ControlPanel controlPanel = new ControlPanel(controller, settingsPanel);
        // Start game
        controller.startGame();

        // Set up main layout
        BorderPane root = new BorderPane();
        HBox centeredBoard = new HBox();
        centeredBoard.setAlignment(Pos.CENTER);
        centeredBoard.getChildren().add(boardView);
        root.setCenter(centeredBoard);
        root.setBottom(controlPanel);
        root.setRight(settingsPanel);
        BorderPane.setMargin(controlPanel, new Insets(10));
        BorderPane.setMargin(centeredBoard, new Insets(20));
        settingsPanel.setVisible(false);

        // Configure window
        Scene scene = new Scene(root, 1200, 900);
        List<Image> icons = new ArrayList<>();
        addIconIfPresent(icons, "/icons/app-icon-16x16.png");
        addIconIfPresent(icons, "/icons/app-icon-32x32.png");
        addIconIfPresent(icons, "/icons/app-icon-64x64.png");
        addIconIfPresent(icons, "/icons/app-icon-128x128.png");

        if (!icons.isEmpty()) {
            primaryStage.getIcons().addAll(icons);
        }
        primaryStage.setTitle("ZChess");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(800);
        primaryStage.show();
    }

    private void addIconIfPresent(List<Image> icons, String path) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream != null) {
                icons.add(new Image(stream));
            }
        } catch (IOException e) {
            System.err.println("Error loading icon: " + path);
        }
    }
}