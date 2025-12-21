module io.github.orizynpx.nodepad {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens io.github.orizynpx.nodepad.view to javafx.fxml;

    exports io.github.orizynpx.nodepad;
    exports io.github.orizynpx.nodepad.view;
    opens io.github.orizynpx.nodepad to javafx.fxml;
}