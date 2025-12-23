module io.github.orizynpx.nodepad {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // External libraries (Automatic Modules)
    requires org.xerial.sqlitejdbc;
    requires org.fxmisc.richtext;
    requires com.google.gson;
    requires okhttp3;

    // Open your packages so JavaFX FXML loader can access them via reflection
    opens io.github.orizynpx.nodepad.app to javafx.graphics, javafx.fxml;
    opens io.github.orizynpx.nodepad.controller to javafx.fxml;
    opens io.github.orizynpx.nodepad.view to javafx.fxml;

    // Allow Gson to access your entities for JSON parsing if needed
    opens io.github.orizynpx.nodepad.model.entity to com.google.gson;

    exports io.github.orizynpx.nodepad.app;
}