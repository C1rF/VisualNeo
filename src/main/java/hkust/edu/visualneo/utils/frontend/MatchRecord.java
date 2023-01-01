package hkust.edu.visualneo.utils.frontend;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class MatchRecord extends VBox {

    private final List<Long> vertexIds;
    public MatchRecord(String idLine, String nodeLine, String relationLine, List<Long> vertexIds){
        Label idInfoLabel = new Label(idLine);
        Label nodeInfoLabel = new Label(nodeLine);
        Label relationInfoLabel = new Label(relationLine);
        this.vertexIds = vertexIds;

        idInfoLabel.getStyleClass().add("match_record_first_line");
        getStyleClass().add("match_record");

        getChildren().addAll(idInfoLabel, nodeInfoLabel, relationInfoLabel);
    }

    public List<Long> getVertexIds() {
        return vertexIds;
    }
}
