package tn.zeros.zchess.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.zeros.zchess.core.board.ChessBoard;
import tn.zeros.zchess.gui.views.ChessBoardView;

import java.io.IOException;

public class ZChessApp extends Application {
    public static final int BOARD_SIZE = 480;

    @Override
    public void start(Stage primaryStage) throws IOException {
        ChessBoard board = new ChessBoard();
        ChessBoardView boardView = new ChessBoardView(board);

        Scene scene = new Scene(boardView, BOARD_SIZE, BOARD_SIZE);
        primaryStage.setTitle("ZChess");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}