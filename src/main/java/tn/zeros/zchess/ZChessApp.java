package tn.zeros.zchess;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.ui.components.ControlPanel;
import tn.zeros.zchess.ui.controller.ChessController;
import tn.zeros.zchess.ui.util.SoundManager;
import tn.zeros.zchess.ui.view.ChessBoardView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ZChessApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Preload sounds
        SoundManager.class.getName();

        // Initialize core components
        BoardState boardState = new BoardState();
        ChessController controller = new ChessController(boardState);

        // Create UI components
        ChessBoardView boardView = new ChessBoardView(controller);
        ControlPanel controlPanel = new ControlPanel(controller);

        // Set up main layout
        BorderPane root = new BorderPane();
        HBox centeredBoard = new HBox();
        centeredBoard.setAlignment(Pos.CENTER);
        centeredBoard.getChildren().add(boardView);
        root.setCenter(centeredBoard);
        root.setBottom(controlPanel);
        BorderPane.setMargin(controlPanel, new Insets(10));
        BorderPane.setMargin(centeredBoard, new Insets(20));

        // Configure window
        Scene scene = new Scene(root, 800, 900);
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

    public static void main(String[] args) {
        launch(args);
    }
}