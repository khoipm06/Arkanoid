module org.example.btl {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.desktop;

    opens org.example.btl to javafx.fxml;
    exports org.example.btl;

    exports com.arkanoid.ui.view;
    opens com.arkanoid.ui.view to javafx.fxml;
}