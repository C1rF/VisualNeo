module hkust.edu.visualneo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens hkust.edu.visualneo to javafx.fxml;
    exports hkust.edu.visualneo;
}