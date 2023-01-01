package hkust.edu.visualneo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class VisualNeoApp extends Application {

    final QueryHandler queryHandler;
    Stage stage;

    public VisualNeoApp() {
        super();
        queryHandler = new QueryHandler(this);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/visualneo-home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("css/visualneo-home.css").toExternalForm());
        VisualNeoController controller = fxmlLoader.<VisualNeoController>getController();
        controller.setApp(this);
        this.stage = stage;
        stage.getIcons().add(new Image(getClass().getResource("icon/icon1.png").toExternalForm()));
        stage.setTitle("VisualNeo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}