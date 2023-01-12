package hkust.edu.visualneo;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;

public class LoadPatternController {

    @FXML
    private TextField textfield_num_patterns;
    @FXML
    private TextField textfield_min_nodes;
    @FXML
    private TextField textfield_max_nodes;
    @FXML
    private Button btn_generate_patterns;
    private VisualNeoController controller;
    private Stage stage;

    private final String FILE_PATH = "src/main/resources/hkust/edu/visualneo/data/worldcup/visualneo_patterns_file.txt";

    @FXML
    void generatePatterns() {
//        FileChooser fileChooser = new FileChooser();
//        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
//        fileChooser.getExtensionFilters().add(extFilter);
//        File selectedFile = fileChooser.showOpenDialog(stage);

        try {
            File selectedFile = new File(FILE_PATH);
            if (selectedFile == null) return;
            controller.parseRecommendedPatternFromFile(selectedFile, true);
            Stage stage = (Stage) btn_generate_patterns.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Pattern Loading Error");
            alert.setHeaderText("Cannot load your pattern file.");
            alert.setContentText("Cannot parse the information from pattern file.\nPlease check your file!");
            alert.showAndWait();
        }
    }


    public void setVisualNeoController(VisualNeoController controller) {
        this.controller = controller;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
