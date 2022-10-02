package hkust.edu.visualneo;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    @FXML
    void handleCancel() {
        Stage stage = (Stage) btn_cancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleLoad() {
        String uri = textfield_uri.getText();
        String user = textfield_user.getText();
        String password = textfield_password.getText();
        try{
            controller.submitDBInfo(uri, user, password);
            // If the database is successfully connected, close the window
            handleCancel();
            // update the UI with the database meta info
            controller.updateUIWithMetaInfo();
        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Connection Error");
            alert.setHeaderText("Cannot connect the database. ");
            alert.setContentText("The provided information is not correct!");
            alert.showAndWait();
        }
    }

    public void setVisualNeoController(VisualNeoController controller){
        this.controller = controller;
    }

}

