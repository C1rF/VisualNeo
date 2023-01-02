package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.Graph;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LoadPatternController {

    private VisualNeoController controller;
    private Stage stage;

    @FXML
    void loadPatternFromFile(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(stage);

        try {
            if (selectedFile == null) return;
            Scanner sc = new Scanner(selectedFile);
            List<List<String>> all_patterns = new ArrayList<>();
            List<String> pattern = null;
            while (sc.hasNextLine()){
                String line = sc.nextLine().trim();
                if(line.isEmpty()) return;
                if(line.startsWith("Final")){
                    pattern = new ArrayList<>();
                    all_patterns.add(pattern);
                }
                else{
                    pattern.add(line);
                }
            }

            List<Graph> pattern_graphs = new ArrayList<>();
            for(List<String> single_pattern : all_patterns)
                pattern_graphs.add(controller.parsePatternFromText(single_pattern));
            controller.displayPatterns(pattern_graphs);

        } catch (FileNotFoundException fe) {
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Load Pattern Error");
            alert.setHeaderText("Cannot load the txt file.");
            if(e.getMessage().equals("No Database")){
                alert.setContentText("Your pattern contains labels/properties while no database is loaded.\nPlease load the database first!");
            }
            else{
                alert.setContentText("The txt file has incorrect format!");
            }
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    void generatePattern(){

    }



    public void setVisualNeoController(VisualNeoController controller) {
        this.controller = controller;
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
