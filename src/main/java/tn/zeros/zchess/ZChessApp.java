package tn.zeros.zchess;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.ui.components.ControlPanel;
import tn.zeros.zchess.ui.controller.ChessController;
import tn.zeros.zchess.ui.view.ChessBoardView;

public class ZChessApp extends Application {

    @Override
    public void start(Stage primaryStage) {
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
        primaryStage.setTitle("ZChess");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(800);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}