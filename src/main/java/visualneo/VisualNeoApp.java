package visualneo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import visualneo.utils.frontend.*;
import java.util.ArrayList;

public class VisualNeoApp extends Application {

    FrontendUI gui;
    QueryHandler handler;

    @Override
    public void start(Stage stage) {
        handler = new QueryHandler(this);
        initUI(stage);
    }
    public static void main(String[] args) {
        launch();
    }
    private void initUI(Stage stage){
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        System.out.println("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

        gui = new FrontendUI(this);
        Scene scene = new Scene(gui, 1200, 700);
        stage.setScene(scene);
        stage.setTitle("Our FYP");
        stage.setOnCloseRequest(e->{
            Platform.exit(); System.exit(0); });
        stage.show();
    }
}
