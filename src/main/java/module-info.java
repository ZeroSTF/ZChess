module tn.zeros.zchess {
    requires javafx.controls;
    requires javafx.fxml;

    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires com.almasb.fxgl.all;
    requires java.desktop;

    exports tn.zeros.zchess;
    opens tn.zeros.zchess to javafx.fxml;
}