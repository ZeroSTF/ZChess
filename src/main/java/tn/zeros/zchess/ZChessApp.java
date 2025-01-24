package tn.zeros.zchess;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.ui.components.ControlPanel;
import tn.zeros.zchess.ui.controllers.ChessController;
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
        root.setCenter(boardView);
        root.setBottom(controlPanel);

        // Configure window
        Scene scene = new Scene(root, 640, 700); // Extra height for controls
        primaryStage.setTitle("ZChess");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}