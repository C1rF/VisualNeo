package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.DbMetadata;
import hkust.edu.visualneo.utils.frontend.Canvas;
import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.GraphElement;
import hkust.edu.visualneo.utils.frontend.Vertex;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.neo4j.driver.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VisualNeoController {
    /**
     * Functions in the MenuBar
     */
    @FXML
    private MenuItem menu_item_save;
    @FXML
    private MenuItem menu_item_load;
    @FXML
    private MenuItem menu_item_clear;
    /**
     * 4 Buttons controlling the core operations
     */
    @FXML
    private Button btn_load_db;
    @FXML
    private Button btn_generate_p;
    @FXML
    private Button btn_exact_search;
    @FXML
    private Button btn_similarity_search;
    /**
     * Buttons and Labels in the Label and Property Pane (8 in total)
     */
    @FXML
    private ChoiceBox<String> choicebox_node_label;
    @FXML
    private Button btn_add_node_label;
    @FXML
    private ChoiceBox<String> choicebox_relation_label;
    @FXML
    private Button btn_add_relation_label;
    @FXML
    private ChoiceBox<String> choicebox_property_name;
    @FXML
    private TextField textfield_property_value;
    @FXML
    private Button btn_add_property;
    /**
     * Buttons and Texts in the Info Pane (4 in total)
     */
    @FXML
    private ScrollPane info_pane;
    @FXML
    private Text text_node_or_relation;
    @FXML
    private Text text_label_info;
    @FXML
    private Text text_property_info;
    /**
     * Node Label Pane + Relation Label Pane + Property Pane
     */
    @FXML
    private AnchorPane pane_node_label;
    @FXML
    private AnchorPane pane_relation_label;
    @FXML
    private AnchorPane pane_property;
    /**
     * Drawing Space
     */
    @FXML
    private AnchorPane query_constructor_pane;
    /**
     * Zoom In and Zoom Out Buttons
     */
    @FXML
    private Button btn_zoom_in;
    @FXML
    private Button btn_zoom_out;

    /**
     * Database Info Pane
     */
    @FXML
    private AnchorPane pane_no_database;
    @FXML
    private AnchorPane pane_with_database;
    @FXML
    private TableView<Map<String, Object>> tableview_node;
    @FXML
    private TableColumn<Map<String, Object>, String> node_name_col;
    @FXML
    private TableColumn<Map<String, Object>, String> node_count_col;
    @FXML
    private TableView<Map<String, Object>> tableview_relation;
    @FXML
    private TableColumn<Map<String, Object>, String> relation_name_col;
    @FXML
    private TableColumn<Map<String, Object>, String> relation_count_col;

    // The system application
    private VisualNeoApp app;
    // The canvas
    @FXML
    private Canvas canvas;

    /**
     * The constructor.
     * The constructor is called before initialize() method.
     */
    public VisualNeoController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {}

    public void setApp(VisualNeoApp app) {
        this.app = app;
    }

    /**
     * Clear the drawing board
     */
    @FXML
    private void handleClear() {
        canvas.clearElements();
    }

    /**
     * Save the drawing pattern
     */
    @FXML
    private void handleSave() {
    }

    /**
     * Load the drawing pattern
     */
    @FXML
    private void handleLoad() {
        // Load the new data
    }

    /**
     * Called when the user click on Load Database button
     */
    @FXML
    private void handleLoadDB() throws IOException {
        // Pop up the load database window
        FXMLLoader fxmlLoader = new FXMLLoader(VisualNeoController.class.getResource("fxml/load-database.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 292, 280);
        LoadDatabaseController db_controller = fxmlLoader.<LoadDatabaseController>getController();
        db_controller.setVisualNeoController(this);
        Stage stage = new Stage();
        stage.setTitle("Load Database");
        stage.setScene(scene);
        stage.show();
    }

    public void submitDBInfo(String uri, String user, String password) {
        app.queryHandler.loadDatabase(uri, user, password);
    }

    public void updateUIWithMetaInfo() {
        DbMetadata metadata = app.queryHandler.getMeta();
        // First switch the display pane
        pane_with_database.setVisible(true);
        pane_no_database.setVisible(false);
        // Update node table
        UpdateNodeTable(metadata);
        UpdateRelationTable(metadata);
        // TODO: Show the schema

        // Then update the choice box with correct choices
        metadata.nodeLabels().forEach(label -> choicebox_node_label.getItems().add(label));
        metadata.relationLabels().forEach(label -> choicebox_relation_label.getItems().add(label));
        // TODO: Add the property choices according to the type (Node/Relation)
        // Set initial state of all the buttons and choice box
        btn_add_node_label.setDisable(false);
        btn_add_relation_label.setDisable(false);
        btn_add_property.setDisable(false);
        choicebox_node_label.getSelectionModel().selectFirst();
        choicebox_relation_label.getSelectionModel().selectFirst();
        choicebox_property_name.getSelectionModel().selectFirst();
    }

    /**
     * Called when the user click on Generate Patterns button
     */
    @FXML
    private void handleGeneratePatterns() {
    }

    /**
     * Called when the user click on Exact Search button
     */
    @FXML
    private void handleExactSearch() {
        List<Vertex> listOfVertices = canvas.getVertices();
        List<Edge> listOfEdges = canvas.getEdges();
        app.queryHandler.exactSearch(listOfVertices,listOfEdges);
    }

    /**
     * Called when the user click on Similarity Search button
     */
    @FXML
    private void handleSimilaritySearch() {
    }

    @FXML
    void handleAddNodeLabel() {
        GraphElement current_highlight = canvas.getSingleHighlight();
        // Add Node labels to the Vertex
        if (current_highlight instanceof Vertex) {
            current_highlight.setLabel(choicebox_node_label.getValue());
            refreshAllPane(current_highlight);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Label Addition Error");
            alert.setHeaderText("Cannot add the label.");
            alert.setContentText("Please select one node!");
            alert.showAndWait();
        }
    }

    @FXML
    void handleAddRelationLabel() {
        GraphElement current_highlight = canvas.getSingleHighlight();
        // Add Relation labels to the Edge
        if (current_highlight instanceof Edge) {
            current_highlight.setLabel(choicebox_relation_label.getValue());
            refreshAllPane(current_highlight);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Label Addition Error");
            alert.setHeaderText("Cannot add the label.");
            alert.setContentText("Please select one edge!");
            alert.showAndWait();
        }
    }


    @FXML
    void handleAddProperty() {
        GraphElement current_highlight = canvas.getSingleHighlight();
        // Add Node/Relation properties to the Node/Relation
        if (current_highlight instanceof GraphElement) {
            String prop_name = choicebox_property_name.getValue();
            String prop_value_text = textfield_property_value.getText();
            Value prop_value = parsePropValue(prop_value_text);
            if (prop_value != null) {
                current_highlight.addProperty(prop_name, prop_value);
                refreshAllPane(current_highlight);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Add Property Error");
                alert.setHeaderText("Cannot add the property.");
                alert.setContentText("Please input the value in correct form!");
                alert.showAndWait();
            }
        } else {
            throw new RuntimeException("Bug Found!");
        }
    }

    private Value parsePropValue(String input_text) {
        return null;
    }

    /**
     * Called when the user's mouse enter a button
     */
    @FXML
    private void handleMouseEnterButton(MouseEvent m) {
        canvas.getScene().setCursor(Cursor.HAND);
    }

    /**
     * Called when the user's mouse leaves a button
     */
    @FXML
    private void handleMouseLeaveButton(MouseEvent m) {
        canvas.getScene().setCursor(Cursor.DEFAULT);
    }

    private void UpdateNodeTable(DbMetadata metadata) {
        node_name_col.setCellValueFactory(new MapValueFactory("Label"));
        node_count_col.setCellValueFactory(new MapValueFactory("Count"));
        ObservableList<Map<String, Object>> items =
                FXCollections.<Map<String, Object>>observableArrayList();
        Set<String> nodeLabels = metadata.nodeLabels();
        for (String label : nodeLabels) {
            Map<String, Object> temp_item = new HashMap<>();
            temp_item.put("Label", label);
            temp_item.put("Count", metadata.nodeCountOf(label));
            items.add(temp_item);
        }
        Map<String, Object> final_item = new HashMap<>();
        final_item.put("Label", "#SUM");
        final_item.put("Count", metadata.nodeCount());
        items.add(final_item);
        tableview_node.getItems().addAll(items);
    }

    private void UpdateRelationTable(DbMetadata metadata) {
        relation_name_col.setCellValueFactory(new MapValueFactory("Label"));
        relation_count_col.setCellValueFactory(new MapValueFactory("Count"));
        ObservableList<Map<String, Object>> items =
                FXCollections.<Map<String, Object>>observableArrayList();
        Set<String> relationLabels = metadata.relationLabels();
        for (String label : relationLabels) {
            Map<String, Object> temp_item = new HashMap<>();
            temp_item.put("Label", label);
            temp_item.put("Count", metadata.relationCountOf(label));
            items.add(temp_item);
        }
        Map<String, Object> final_item = new HashMap<>();
        final_item.put("Label", "#SUM");
        final_item.put("Count", metadata.relationCount());
        items.add(final_item);
        tableview_relation.getItems().addAll(items);
    }

    /**
     * Helper Function
     * Given a GraphElement, refresh the InfoPane according to the information
     */
    private void refreshAllPane(GraphElement current_highlight) {
        //javafx.scene.Node graphical_node = current_highlight.getShape();
        info_pane.setVisible(true);
        pane_property.setVisible(true);
        // TODO: Change the choices of the choice box accordingly.
        if (current_highlight instanceof Vertex) {
            pane_node_label.setVisible(true);
            pane_relation_label.setVisible(false);
            text_node_or_relation.setText("Node Information");
        } else {
            pane_node_label.setVisible(false);
            pane_relation_label.setVisible(true);
            text_node_or_relation.setText("Relation Information");
        }

        // Display the information on the information pane
        StringBuilder builder = new StringBuilder();
        text_label_info.setText(current_highlight.getLabel());
        Map<String, Value> properties = current_highlight.getProp();
        for (String propertyKey : properties.keySet()) {
            builder.append(propertyKey).append(" : ").append(properties.get(propertyKey)).append("\n");
        }
        text_property_info.setText(builder.toString());
    }

    @FXML
    void handleZoomIn() {
        canvas.camera.zoomIn();
    }

    @FXML
    void handleZoomOut() {
        canvas.camera.zoomOut();
    }

}
