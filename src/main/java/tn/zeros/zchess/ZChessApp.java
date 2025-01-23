package tn.zeros.zchess;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.ui.components.ChessBoardView;

public class ZChessApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        BoardState position = new BoardState();
        ChessBoardView boardView = new ChessBoardView(position);

        VBox root = new VBox(10);
        root.getChildren().add(boardView);

        Scene scene = new Scene(root, 800, 800);
        scene.getStylesheets().add(getClass().getResource("/styles/chess.css").toExternalForm());

        primaryStage.setTitle("Chess");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}