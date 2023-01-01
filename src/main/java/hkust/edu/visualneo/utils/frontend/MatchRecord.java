package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.utils.backend.Pair;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class MatchRecord extends VBox {

    private final Pair<List<Long>> match;

    public MatchRecord(Pair<List<Long>> match, int recordId) {
        this.match = match;

        String idLine = "Match " + recordId + ":";
        String nodeLine = "Node ID(s): ";
        for (Long id : match.head()) nodeLine += id + ", ";
        nodeLine = nodeLine.substring(0, nodeLine.length() - 2);
        String relationLine = "Relation ID(s): ";
        for (Long id : match.tail()) relationLine += id + ", ";
        relationLine = relationLine.substring(0, relationLine.length() - 2);

        Label idInfoLabel = new Label(idLine);
        Label nodeInfoLabel = new Label(nodeLine);
        Label relationInfoLabel = new Label(relationLine);

        idInfoLabel.getStyleClass().add("match_record_first_line");
        getStyleClass().add("match_record");

        getChildren().addAll(idInfoLabel, nodeInfoLabel, relationInfoLabel);
    }

    public Pair<List<Long>> getMatch() {
        return match;
    }
}
