package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.frontend.LoadAnimation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.neo4j.driver.exceptions.AuthenticationException;

public class LoadDatabaseController {

    private VisualNeoController controller;
    @FXML
    private TextField textfield_uri;

    @FXML
    private TextField textfield_user;

    @FXML
    private TextField textfield_password;

    @FXML
    private Button btn_cancel;

    @FXML
    private Button btn_load;

    // Boolean variable to indicate whether connection is successful
    Boolean connect_success;

    @FXML
    void handleCancel() {
        Stage stage = (Stage) btn_cancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleLoad() {

        // Create the animation
        Stage stage = (Stage) btn_load.getScene().getWindow();
        LoadAnimation loadAnimation = new LoadAnimation(stage);

        // Get the user's input
        String uri = textfield_uri.getText();
        String user = textfield_user.getText();
        String password = textfield_password.getText();

        //worker thread for connecting the database
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    // Try to connect to the database
                    //controller.submitDBInfo(uri, user, password);
                    controller.submitDBInfo("bolt://52.90.7.253:7687", "neo4j", "attempts-windows-items");
                    connect_success = true;
                } catch (AuthenticationException e) {
                    connect_success = false;
                    Platform.runLater(() -> {
                        System.out.println(e.getMessage());
                        loadAnimation.cancelLoadAnimation();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Database Connection Error");
                        alert.setHeaderText("Cannot connect the database. ");
                        alert.setContentText("The provided information is not correct!");
                        alert.showAndWait();
                    });
                }
            }
        });
        worker.start();

        // Load Animation Starts
        loadAnimation.activateLoadAnimation();

        //observer thread for notifications
        new Thread(new Runnable() {
            public void run() {
                try {
                    // Try to connect to the database
                    worker.join();
                } catch (Exception e) {
                    ;
                } finally {
                    Platform.runLater(() -> {
                        System.out.println("The load database thread ends");
                        if (connect_success) {
                            loadAnimation.cancelLoadAnimation();
                            // If the database is successfully connected, close the window
                            handleCancel();
                            // update the UI with the database meta info
                            controller.updateUIWithMetaInfo();
                        }
                    });
                }
            }
        }).start();

    }

    public void setVisualNeoController(VisualNeoController controller) {
        this.controller = controller;
    }

}

