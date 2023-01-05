package hkust.edu.visualneo;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class LoadPatternController {

    @FXML
    private Button btn_load_from_txt;
    private VisualNeoController controller;
    private Stage stage;

    @FXML
    void loadPatternFromFile() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(stage);

        try {
            if (selectedFile == null) return;
            controller.parseRecommendedPatternFromFile(selectedFile, true);
            Stage stage = (Stage) btn_load_from_txt.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Pattern Loading Error");
            alert.setHeaderText("Cannot load your pattern file.");
            alert.setContentText("Cannot parse the information from pattern file.\nPlease check your file!");
            alert.showAndWait();
        }
    }

    @FXML
    void generatePattern() {

    }


    public void setVisualNeoController(VisualNeoController controller) {
        this.controller = controller;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
