package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.*;
import hkust.edu.visualneo.utils.frontend.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class VisualNeoController {
    /**
     * Buttons and Labels in the Label and Property Pane (9 in total)
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
    @FXML
    private CheckBox checkbox_directed;
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
    private Map<String, String> current_property_map;
    private ChangeListener<String> property_change_listener;
    private ChangeListener<Boolean> directed_change_listener;
    /**
     * Drawing Space
     */
    @FXML
    private TabPane tab_pane;
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
    private Canvas constructCanvas;
    @FXML
    private Canvas resultCanvas;
    @FXML
    private Canvas schemaCanvas;
    DbMetadata metadata;

    // The pattern recommendation panel
    @FXML
    private TabPane tabpane_pattern;
    @FXML
    private Tab tab_basic_pattern;
    @FXML
    private Tab tab_canned_pattern;
    @FXML
    private Tab tab_result_record;
    @FXML
    private VBox vbox_record;


    /**
     * The constructor.
     * The constructor is called before initialize() method.
     */
    public VisualNeoController() {}

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        constructCanvas.setType(Canvas.CanvasType.MODIFIABLE);
        constructCanvas.getHighlights().addListener((SetChangeListener<GraphElement>) c -> {
            GraphElement temp = constructCanvas.getSingleHighlight();
            if (temp != null)
                refreshAllPane(temp, false);
            else
                hideAllPane();
        });
        resultCanvas.setType(Canvas.CanvasType.NAVIGABLE);
        resultCanvas.getHighlights().addListener((SetChangeListener<GraphElement>) c -> {
            GraphElement temp = resultCanvas.getSingleHighlight();
            if (temp != null)
                refreshAllPane(temp, true);
            else
                hideAllPane();
        });
        schemaCanvas.setType(Canvas.CanvasType.NAVIGABLE);
        schemaCanvas.getHighlights().addListener((SetChangeListener<GraphElement>) c -> {
            GraphElement temp = schemaCanvas.getSingleHighlight();
            if (temp != null)
                refreshAllPane(temp, true);
            else
                hideAllPane();
        });

        Rectangle schemaContainer = new Rectangle(648, 321.5);
        schemaCanvas.setClip(schemaContainer);

        tab_pane.onKeyPressedProperty().bind(constructCanvas.onKeyPressedProperty());
        tabpane_pattern.getTabs().remove(tab_result_record);

        property_change_listener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> obs, String oldValue, String newValue) {
                String type = current_property_map.get(newValue);
                System.out.println("Current Selected Property Type: " + type);
                String propmt_text = "New Case! Please debug!";
                if(type.equals("String")){
                    propmt_text = "Please input a String";
                } else if (type.equals("Long") ) {
                    propmt_text = "Please input a Number";
                } else if (type.equals("Double") ) {
                    propmt_text = "Please input a Double";
                }
                textfield_property_value.setPromptText(propmt_text);
            }
        };

        directed_change_listener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                GraphElement current_highlight = constructCanvas.getSingleHighlight();
                Edge highlight_edge = (Edge) current_highlight;
                if(highlight_edge != null) highlight_edge.setDirected(newValue);
            }
        };

        tab_pane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number> (){
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int selectedIndex = newValue.intValue();
                switch (selectedIndex){
                    case 0 -> {
                        resultCanvas.clearHighlights();
                        schemaCanvas.clearHighlights();
                        tab_pane.onKeyPressedProperty().unbind();
                        tab_pane.onKeyPressedProperty().bind(constructCanvas.onKeyPressedProperty());
                        tabpane_pattern.getTabs().remove(tab_result_record);
                        tabpane_pattern.getTabs().addAll(tab_basic_pattern, tab_canned_pattern);
                    }
                    case 1 -> {
                        constructCanvas.clearHighlights();
                        schemaCanvas.clearHighlights();
                        tab_pane.onKeyPressedProperty().unbind();
                        tab_pane.onKeyPressedProperty().bind(resultCanvas.onKeyPressedProperty());
                        tabpane_pattern.getTabs().removeAll(tab_basic_pattern, tab_canned_pattern);
                        tabpane_pattern.getTabs().add(tab_result_record);
                    }
                    case 2 -> {
                        constructCanvas.clearHighlights();
                        resultCanvas.clearHighlights();
                        tab_pane.onKeyPressedProperty().unbind();
                        tab_pane.onKeyPressedProperty().bind(schemaCanvas.onKeyPressedProperty());
                        tabpane_pattern.getTabs().removeAll(tab_basic_pattern, tab_canned_pattern, tab_result_record);
                    }
                }
            }
        });
    }

    public void setApp(VisualNeoApp app) { this.app = app; }

    /**
     * Called when the user click on Load Database button
     */
    @FXML
    private void handleLoadDB() throws IOException {
        // Set the scene
        FXMLLoader fxmlLoader = new FXMLLoader(VisualNeoController.class.getResource("fxml/load-database.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 292, 280);
        LoadDatabaseController db_controller = fxmlLoader.<LoadDatabaseController>getController();
        db_controller.setVisualNeoController(this);
        // Set the stage
        Stage stage = new Stage();
        stage.setTitle("Load Database");
        stage.setScene(scene);
        stage.show();
    }

    public void submitDBInfo(String uri, String user, String password) {
        app.queryHandler.loadDatabase(uri, user, password);
    }

    public void updateUIWithMetaInfo() {
        metadata = app.queryHandler.getMeta();

        // Switch the info pane
        pane_with_database.setVisible(true);
        pane_no_database.setVisible(false);

        // Update node table
        UpdateNodeTable(metadata);
        UpdateRelationTable(metadata);

        // Display the schema graph
        schemaCanvas.clearElements();
        schemaCanvas.loadGraph(metadata.schemaGraph());
        Map<String, Map<String, String>> labelToNodeProperty = metadata.nodePropertiesByLabel();
        Map<String, Map<String, String>> labelToRelationProperty = metadata.relationPropertiesByLabel();
        for(Vertex v : schemaCanvas.getVertices()){
            Map<String, String> nodeProperties = labelToNodeProperty.get(v.getLabel());
            if(nodeProperties == null) continue;
            for(Map.Entry<String,String> nodeProperty : nodeProperties.entrySet())
                v.addProperty(nodeProperty.getKey(), Values.value(nodeProperty.getValue()));
        }
        for(Edge e : schemaCanvas.getEdges()){
            Map<String, String> relationProperties = labelToRelationProperty.get(e.getLabel());
            if(relationProperties == null) continue;
            for(Map.Entry<String,String> relationProperty : relationProperties.entrySet()){
                e.addProperty(relationProperty.getKey(), Values.value(relationProperty.getValue()));
            }
        }

        // Update the label choice box
        metadata.nodeLabels().forEach(label -> choicebox_node_label.getItems().add(label));
        metadata.relationLabels().forEach(label -> choicebox_relation_label.getItems().add(label));

        // Set initial state of all the buttons and choice box
        btn_add_node_label.setDisable(false);
        btn_add_relation_label.setDisable(false);
        btn_add_property.setDisable(false);
        choicebox_node_label.getSelectionModel().selectFirst();
        choicebox_relation_label.getSelectionModel().selectFirst();

        // If there is a single highlight, refresh all panes
        if(constructCanvas.getSingleHighlight() != null) refreshAllPane(constructCanvas.getSingleHighlight(), false);
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
        Collection<Vertex> listOfVertices = constructCanvas.getVertices();
        Collection<Edge> listOfEdges = constructCanvas.getEdges();
        QueryHandler.Results results = null;
        try {
            results = app.queryHandler.exactSearch(listOfVertices, listOfEdges);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exact Search Error");
            alert.setHeaderText("Cannot perform the exact search!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        if (results != null) {
            resultCanvas.clearElements();
            resultCanvas.loadGraph(results.graph());
            vbox_record.getChildren().removeAll();
            int matchIdx = 1;
            for (int i = 0; i < results.ids().size(); i++) {
                Pair<List<Long>> match = results.ids().get(i);
                String line1 = "Match " + matchIdx++ + ":";
                String line2 = "Node ID(s): ";
                for (Long id : match.head()) line2 += id + ", ";
                line2 = line2.substring(0, line2.length() - 2);
                String line3 = "Relation ID(s): ";
                for (Long id : match.tail()) line3 += id + ", ";
                line3 = line3.substring(0, line3.length() - 2);

                MatchRecord record = new MatchRecord(line1, line2, line3, match.head());

                record.setOnMouseEntered(e -> handleMouseEnterButton(e));
                record.setOnMouseExited(e -> handleMouseLeaveButton(e));
                record.setOnMouseClicked(e -> {
                    MatchRecord currentRecord = (MatchRecord) e.getSource();
//                    if(currentRecord != null) resultCanvas.;
                });

                vbox_record.getChildren().add(record);
                if (i < results.ids().size() - 1) vbox_record.getChildren().add(new Separator());
            }
        }
    }

    /**
     * Called when the user click on Similarity Search button
     */
    @FXML
    private void handleSimilaritySearch() {
    }

    @FXML
    void handleAddNodeLabel() {
        GraphElement current_highlight = constructCanvas.getSingleHighlight();
        current_highlight.setLabel(choicebox_node_label.getValue());
        refreshAllPane(current_highlight, false);
    }

    @FXML
    void handleAddRelationLabel() {
        GraphElement current_highlight = constructCanvas.getSingleHighlight();
        current_highlight.setLabel(choicebox_relation_label.getValue());
        refreshAllPane(current_highlight, false);
    }

    @FXML
    void handleAddProperty() {
        GraphElement current_highlight = constructCanvas.getSingleHighlight();
        // Add Node/Relation properties to the Node/Relation
        String prop_name = choicebox_property_name.getValue();
        String prop_type = current_property_map.get(prop_name);
        String prop_value_text = textfield_property_value.getText().trim();
        Value prop_value = parsePropValue(prop_type, prop_value_text);
        if (prop_value != null) {
            current_highlight.addProperty(prop_name, prop_value);
            textfield_property_value.clear();
            refreshAllPane(current_highlight, false);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Add Property Error");
            alert.setHeaderText("Cannot add the property.");
            alert.setContentText("Please input a " + prop_type);
            alert.showAndWait();
        }
    }

    private Value parsePropValue(String type, String input) {
        try{
            if(type.equals("String")){
                return Values.value(input);
            }
            else if(type.equals("Long")){
                long num = Long.parseLong(input);
                return Values.value(num);
            }
            else if(type.equals("Float")){
                double num = Double.parseDouble(input);
                return Values.value(num);
            }
        }catch(Exception e){
            return null;
        }
        return null;
    }

    /**
     * Called when the user's mouse enter a button
     */
    @FXML
    private void handleMouseEnterButton(MouseEvent m) {
        constructCanvas.getScene().setCursor(Cursor.HAND);
    }
    /**
     * Called when the user's mouse leaves a button
     */
    @FXML
    private void handleMouseLeaveButton(MouseEvent m) {
        constructCanvas.getScene().setCursor(Cursor.DEFAULT);
    }

    private void UpdateNodeTable(DbMetadata metadata) {
        node_name_col.setCellValueFactory(new MapValueFactory("Label"));
        node_count_col.setCellValueFactory(new MapValueFactory("Count"));
        ObservableList<Map<String, Object>> items =
                FXCollections.<Map<String, Object>>observableArrayList();
        Collection<String> nodeLabels = metadata.nodeLabels();
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
        Collection<String> relationLabels = metadata.relationLabels();
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
     * Given a GraphElement, REFRESH the InfoPane according to the information
     */
    private void refreshAllPane(GraphElement current_highlight, boolean info_only) {
        boolean isVertex = current_highlight instanceof Vertex;

        // Display the information on the information pane
        info_pane.setVisible(true);
        text_node_or_relation.setText(isVertex ? "Node Information" : "Relation Information");
        StringBuilder builder = new StringBuilder();
        text_label_info.setText(current_highlight.getLabel());
        Map<String, Value> properties = current_highlight.getProp();
        for (String propertyKey : properties.keySet())
            builder.append(propertyKey).append(" : ").append(properties.get(propertyKey)).append("\n");
        text_property_info.setText(builder.toString());

        if (info_only) return;

        // Display the information on the label and property pane
        pane_property.setVisible(true);
        pane_node_label.setVisible(isVertex);
        pane_relation_label.setVisible(!isVertex);
        choicebox_property_name.getSelectionModel()
                .selectedItemProperty().removeListener(property_change_listener);
        choicebox_property_name.getItems().clear();
        checkbox_directed.selectedProperty().removeListener(directed_change_listener);
        if (!isVertex) {
            // Display the checkbox according to current status and add listener
            Edge highlight_edge = (Edge) current_highlight;
            checkbox_directed.setSelected(highlight_edge.isDirected());
            checkbox_directed.selectedProperty().addListener(directed_change_listener);
        }

        // Update current property map
        if (metadata == null) return;
        if (isVertex)
            current_property_map = current_highlight.getLabel() == null ? metadata.nodeProperties() : metadata.nodePropertiesOf(current_highlight.getLabel());
        else
            current_property_map = current_highlight.getLabel() == null ? metadata.relationProperties() : metadata.relationPropertiesOf(current_highlight.getLabel());

        // Update the property choice box according to current property map
        if (current_property_map != null)
            current_property_map.keySet().forEach(property -> choicebox_property_name.getItems().add(property));
        else
            choicebox_property_name.getItems().removeAll();

        // Set the listener for choicebox_property_name
        choicebox_property_name.getSelectionModel().selectFirst();
        choicebox_property_name.getSelectionModel()
                .selectedItemProperty()
                .addListener(property_change_listener);
    }

    private void hideAllPane() {
        info_pane.setVisible(false);
        pane_property.setVisible(false);
        pane_node_label.setVisible(false);
        pane_relation_label.setVisible(false);
    }

    @FXML
    void handleZoomIn() { constructCanvas.camera.zoomIn();}

    @FXML
    void handleZoomOut() { constructCanvas.camera.zoomOut();}

    // Functions in the Menu Bar
    /**
     * Display the "About Us" information
     */
    @FXML
    void aboutUs() throws IOException {
        // Set the scene
        FXMLLoader fxmlLoader = new FXMLLoader(VisualNeoController.class.getResource("fxml/about-us.fxml"));
        Scene dialogScene = new Scene(fxmlLoader.load(), 500, 300);
        // Set the stage
        final Stage dialog = new Stage();
        dialog.setTitle("About Us");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(dialogScene);
        dialog.setResizable(false);
        dialog.show();
    }
    /**
     * Clear the drawing board
     */
    @FXML
    private void handleClear() {
        constructCanvas.clearElements();
    }

    /**
     * Save the drawing pattern
     */
    @FXML
    private void handleSave() {
        StringBuilder outputText = new StringBuilder();
        Collection<Vertex> vertices = constructCanvas.getVertices();
        Collection<Edge> edges =  constructCanvas.getEdges();
        for(Vertex v : vertices)
            outputText.append(v.toText()).append('\n');
        for(Edge e : edges)
            outputText.append(e.toText()).append('\n');
        System.out.println(outputText);
        FileChooser fileChooser = new FileChooser();
        // Set extension filter for txt files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        // Show save file dialog
        File file = fileChooser.showSaveDialog(app.stage);
        if (file != null) {
            // save the outputText to the file
            try {
                PrintWriter writer;
                writer = new PrintWriter(file);
                writer.println(outputText);
                writer.close();
            } catch (IOException e) {
                System.out.println("Error when saving the pattern");
                e.printStackTrace();
            }
        }
    }

    /**
     * Load the drawing pattern
     */
    @FXML
    private void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(app.stage);
        try {
            if (selectedFile == null) return;
            Graph pattern_graph = parsePattern(selectedFile);
            constructCanvas.clearElements();
            constructCanvas.loadGraph(pattern_graph);
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
        }
    }

    // Helper function to parse the pattern
    private Graph parsePattern(File pattern_file) throws Exception {
        Scanner sc = new Scanner(pattern_file);

        Collection<Node> nodes = new HashSet<>();
        Collection<Relation> relations = new HashSet<>();
        Map<Long, Node> nodeMap = new HashMap<>();
        long currentId = GraphElement.getCurrentId();
        boolean hasDatabase = (metadata != null);

        while (sc.hasNextLine()) {
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) {
                // Parse a single line
                String[] elements = s.split("\\s+");
                boolean isVertex = elements[0].equals("v");
                if (isVertex) {
                    // Vertex
                    if(!hasDatabase && (!elements[2].equals("null") || !elements[3].equals("null"))) throw new Exception("No Database");
                    String label = elements[2].equals("null") ? null : elements[2];
                    Map<String, Value> properties = new TreeMap<>();
                    for (int i = 3; i < elements.length; i++) {
                        String property = elements[i];
                        if (property.equals("null")) break;
                        String[] splitProperty = property.split(":");
                        Value propertyValue = parsePropValue(metadata.nodeProperties().get(splitProperty[0]), splitProperty[1]);
                        properties.put(splitProperty[0], propertyValue);
                    }
                    Node newNode = new Node(currentId++, label, properties);
                    nodeMap.put(Long.parseLong(elements[1]), newNode);
                    nodes.add(newNode);
                } else {
                    // Edge
                    if(!hasDatabase && (!elements[4].equals("null") || !elements[5].equals("null"))) throw new Exception("No Database");
                    boolean directed = Boolean.valueOf(elements[3]);
                    String label = elements[4].equals("null") ? null : elements[4];
                    Map<String, Value> properties = new TreeMap<>();
                    for (int i = 5; i < elements.length; i++) {
                        String property = elements[i];
                        if (property.equals("null")) break;
                        String[] splitProperty = property.split(":");
                        Value propertyValue = parsePropValue(metadata.relationProperties().get(splitProperty[0]), splitProperty[1]);
                        properties.put(splitProperty[0], propertyValue);
                    }
                    Node start = nodeMap.get(Long.parseLong(elements[1]));
                    Node end = nodeMap.get(Long.parseLong(elements[2]));
                    Relation newRelation = new Relation(currentId++, directed, start, end, label, properties);
                    relations.add(newRelation);
                }
            }
        }
        return new Graph(nodes, relations, false);
    }

}
