module tn.zeros.zchess {
    requires javafx.controls;
    requires javafx.fxml;

    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires com.almasb.fxgl.all;

    exports tn.zeros.zchess.app;
    opens tn.zeros.zchess.app to javafx.fxml;
}