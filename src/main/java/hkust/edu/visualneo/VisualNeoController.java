package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.*;
import hkust.edu.visualneo.utils.frontend.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
import java.util.stream.Stream;

public class VisualNeoController {

    public final QueryHandler queryHandler = new QueryHandler();

    @FXML
    private Button btn_exact_search;
    @FXML
    private Button btn_generate_patterns;
    /**
     * Buttons and Labels in Info Pane
     */
    @FXML
    private VBox info_box;
    private Map<String, String> current_property_map;
    TextField prop_value_textfield;
    ChoiceBox<String> property_choices;
    /**
     * Node Label Pane + Relation Label Pane + Property Pane
     */
    private ChangeListener<String> label_change_listener;
    private ChangeListener<String> property_change_listener;
    private ChangeListener<Boolean> directed_change_listener;
    private EventHandler<MouseEvent> click_match_handler;
    /**
     * Drawing Space
     */
    @FXML
    private TabPane tab_pane;
    @FXML
    private Tab tab_query_constructor;
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
    @FXML
    private VBox vbox_basic_patterns;
    @FXML
    private VBox vbox_canned_patterns;
    @FXML
    private TextArea textarea_query;

    List<Canvas> basicPatternCanvases = new ArrayList<>();

    List<Canvas> cannedPatternCanvases = new ArrayList<>();
    private static double PATTERN_CANVAS_HEIGHT = 150.0;

    private final String BASIC_PATTERN_PATH = "src/main/resources/hkust/edu/visualneo/data/basic/basicPattern.txt";


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
    private void initialize() throws Exception {
        constructCanvas.setType(Canvas.CanvasType.MODIFIABLE);
        constructCanvas.addListener(observable -> queryHandler.getTranslator().update(new Graph(constructCanvas)));
        constructCanvas.getHighlights().addListener((SetChangeListener<GraphElement>) c -> {
            GraphElement temp = constructCanvas.getSingleHighlight();
            if (temp != null)
                refreshInfoPane(temp, true);
            else
                hideInfoPane();
        });
        constructCanvas.setOnDragOver(e -> {
            e.acceptTransferModes(TransferMode.COPY);
        });
        constructCanvas.setOnDragDropped(e -> {
            Dragboard dragboard = e.getDragboard();
            if (dragboard.hasString()) {
                String[] info = dragboard.getString().split("\\s+");
                int listIdx = Integer.parseInt(info[0]);
                int idxInList = Integer.parseInt(info[1]);
                Canvas patternCanvas = listIdx == 0 ? basicPatternCanvases.get(idxInList) : cannedPatternCanvases.get(idxInList);
                pastePatternCanvasToConstructCanvas(patternCanvas, new Point2D(e.getX(), e.getY()));
                e.setDropCompleted(true);
            } else {
                e.setDropCompleted(false);
            }
        });


        resultCanvas.setType(Canvas.CanvasType.NAVIGABLE);
        resultCanvas.getHighlights().addListener((SetChangeListener<GraphElement>) c -> {
            GraphElement temp = resultCanvas.getSingleHighlight();
            if (temp != null)
                refreshInfoPane(temp, false);
            else
                hideInfoPane();
        });
        schemaCanvas.setType(Canvas.CanvasType.NAVIGABLE);
        schemaCanvas.getHighlights().addListener((SetChangeListener<GraphElement>) c -> {
            GraphElement temp = schemaCanvas.getSingleHighlight();
            if (temp != null)
                refreshInfoPane(temp, false);
            else
                hideInfoPane();
        });

        Rectangle schemaContainer = new Rectangle(648, 321.5);
        schemaCanvas.setClip(schemaContainer);
        tabpane_pattern.getTabs().remove(tab_result_record);

        label_change_listener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                GraphElement current_highlight = constructCanvas.getSingleHighlight();
                if(current_highlight == null) return;
                boolean nullLabel = newValue.isEmpty();

                // Update the current label
                if(nullLabel) current_highlight.setLabel(null);
                else current_highlight.setLabel(newValue);

                // Update current property map
                if (current_highlight instanceof Vertex)
                    current_property_map = nullLabel ? metadata.nodeProperties() : metadata.nodePropertiesOf(newValue);
                else
                    current_property_map = nullLabel ? metadata.relationProperties() : metadata.relationPropertiesOf(newValue);

                // Remove the redundant properties
                Set<String> properties_to_be_removed = new HashSet<>();
                Set<String> all_property_names = current_highlight.getElementProperties().keySet();
                for (String name: all_property_names) {
                    if(!current_property_map.keySet().contains(name))
                        properties_to_be_removed.add(name);
                }
                for(String name : properties_to_be_removed)
                    current_highlight.removeProperty(name);

                refreshInfoPane(current_highlight, true);
            }
        };
        property_change_listener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> obs, String oldValue, String newValue) {
                if(newValue == null) return;
                String type = current_property_map.get(newValue);
                prop_value_textfield.setPromptText("Please input a " + type);
            }
        };

        directed_change_listener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                GraphElement current_highlight = constructCanvas.getSingleHighlight();
                Edge highlight_edge = (Edge) current_highlight;
                if (highlight_edge != null) highlight_edge.setDirected(newValue);
            }
        };

        click_match_handler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                MatchRecord currentRecord = (MatchRecord) e.getSource();
                if (currentRecord != null) {
                    Collection<GraphElement> elements = Stream
                            .concat(currentRecord.getMatch().head().stream().map(id -> resultCanvas.getVertex(id)),
                                    currentRecord.getMatch().tail().stream().map(id -> resultCanvas.getEdge(id)))
                            .toList();
                    resultCanvas.navigateTo(elements, true, false);
                }
            }
        };

        tab_pane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int selectedIndex = newValue.intValue();
                switch (selectedIndex) {
                    case 0 -> {
                        resultCanvas.clearHighlights();
                        schemaCanvas.clearHighlights();
                        tabpane_pattern.getTabs().remove(tab_result_record);
                        tabpane_pattern.getTabs().addAll(tab_basic_pattern, tab_canned_pattern);
                    }
                    case 1 -> {
                        constructCanvas.clearHighlights();
                        schemaCanvas.clearHighlights();
                        tabpane_pattern.getTabs().removeAll(tab_basic_pattern, tab_canned_pattern);
                        tabpane_pattern.getTabs().add(tab_result_record);
                    }
                    case 2 -> {
                        constructCanvas.clearHighlights();
                        resultCanvas.clearHighlights();
                        tabpane_pattern.getTabs().removeAll(tab_basic_pattern, tab_canned_pattern, tab_result_record);
                    }
                }
            }
        });

        // Bind the query code display textarea
        textarea_query.setWrapText(true);
        textarea_query.textProperty().bind(queryHandler.getTranslator().translationProperty());

        // Load the basic patterns
        File basicPattern = new File(BASIC_PATTERN_PATH);
        parseRecommendedPatternFromFile(basicPattern, false);
    }

    public void setApp(VisualNeoApp app) {
        this.app = app;
    }

    /**
     * Called when the user click on Load Database button
     */
    @FXML
    private void handleLoadDB() throws IOException {
        // Set the scene
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/load-database.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 292, 280);
        var db_controller = fxmlLoader.<LoadDatabaseController>getController();
        db_controller.setVisualNeoController(this);
        // Set the stage
        Stage stage = new Stage();
        stage.setTitle("Load Database");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public void submitDBInfo(String uri, String user, String password) {
        queryHandler.loadDatabase(uri, user, password);
    }

    public void updateUIWithMetaInfo() {
        metadata = queryHandler.getMeta();

        // Compute label colors
        Canvas.computeColors(metadata);

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
        for (Vertex v : schemaCanvas.getVertices()) {
            Map<String, String> nodeProperties = labelToNodeProperty.get(v.getLabel());
            if (nodeProperties == null) continue;
            for (Map.Entry<String, String> nodeProperty : nodeProperties.entrySet())
                v.addProperty(nodeProperty.getKey(), Values.value(nodeProperty.getValue()));
        }
        for (Edge e : schemaCanvas.getEdges()) {
            Map<String, String> relationProperties = labelToRelationProperty.get(e.getLabel());
            if (relationProperties == null) continue;
            for (Map.Entry<String, String> relationProperty : relationProperties.entrySet()) {
                e.addProperty(relationProperty.getKey(), Values.value(relationProperty.getValue()));
            }
        }
        // If there is a single highlight, refresh all panes
        if (constructCanvas.getSingleHighlight() != null)
            refreshInfoPane(constructCanvas.getSingleHighlight(), true);

        // Enable exact search function
        btn_exact_search.setDisable(false);
        btn_generate_patterns.setDisable(false);
    }

    /**
     * Called when the user click on Generate Patterns button
     */
    @FXML
    private void handleGeneratePatterns() throws IOException {
        // Set the scene
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/load-pattern.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 292, 280);
        var patternController = fxmlLoader.<LoadPatternController>getController();
        patternController.setVisualNeoController(this);
        // Set the stage
        Stage stage = new Stage();
        stage.setTitle("Load Patterns");
        stage.setResizable(false);
        patternController.setStage(stage);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Called when the user click on Exact Search button
     */
    @FXML
    private void handleExactSearch() {
        QueryHandler.Results results = null;
        try {
            results = queryHandler.exactSearch(constructCanvas);
        } catch (Graph.BadTopologyException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exact Search Error");
            alert.setHeaderText("Cannot perform the exact search!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        if (results != null) {
            resultCanvas.clearElements();
            resultCanvas.loadGraph(results.graph());
            vbox_record.getChildren().clear();
            int matchIdx = 1;
            for (int i = 0; i < results.ids().size(); i++) {
                Pair<List<Long>> match = results.ids().get(i);
                MatchRecord record = new MatchRecord(match, matchIdx++);

                record.setOnMouseEntered(e -> handleMouseEnterButton(e));
                record.setOnMouseExited(e -> handleMouseLeaveButton(e));
                record.setOnMouseClicked(click_match_handler);

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

    void addProperty() {
        GraphElement current_highlight = constructCanvas.getSingleHighlight();
        String name = property_choices.getValue();
        if(name == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Add Property Error");
            alert.setHeaderText("Cannot add the property.");
            alert.setContentText("Please select a property first");
            alert.showAndWait();
            return;
        }
        String type = current_property_map.get(name);
        Value prop_value = parsePropValue(type, prop_value_textfield.getText());

        if (prop_value != null) {
            current_highlight.addProperty(name, prop_value);
            refreshInfoPane(current_highlight, true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Add Property Error");
            alert.setHeaderText("Cannot add the property.");
            alert.setContentText("Please input a " + type);
            alert.showAndWait();
        }
    }

    private Value parsePropValue(String type, String input) {
        try {
            if (type.equals("String")) {
                return Values.value(input);
            } else if (type.equals("Long")) {
                long num = Long.parseLong(input);
                return Values.value(num);
            } else if (type.equals("Float")) {
                double num = Double.parseDouble(input);
                return Values.value(num);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @FXML
    private void handleMouseEnterButton(MouseEvent m) {
        constructCanvas.getScene().setCursor(Cursor.HAND);
    }
    @FXML
    private void handleMouseLeaveButton(MouseEvent m) {
        constructCanvas.getScene().setCursor(Cursor.DEFAULT);
    }

    private void UpdateNodeTable(DbMetadata metadata) {
        tableview_node.getItems().clear();
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
        tableview_relation.getItems().clear();
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
    private void refreshInfoPane(GraphElement current_highlight, boolean modifiable) {
        if(current_highlight == null) return;
        info_box.getChildren().clear();
        boolean hasDatabase = (metadata != null);
        boolean isVertex = current_highlight instanceof Vertex;
        boolean emptyLabel = !current_highlight.hasLabel();

        // Display the information on the information pane
        info_box.setVisible(true);
        Text info_text = new Text(isVertex ? "Node Information" : "Relation Information");
        info_text.getStyleClass().add("info_text");
        info_box.getChildren().add(info_text);

        // Display the direction checkbox if this is a relation
        if(!isVertex){
            // Display the checkbox according to current status and add listener
            CheckBox checkbox_directed = new CheckBox();
            checkbox_directed.setSelected(((Edge) current_highlight).isDirected());
            checkbox_directed.selectedProperty().addListener(directed_change_listener);
            Text directed_text = new Text("Directed:");
            directed_text.getStyleClass().add("headline");
            HBox dir_hbox = new HBox(directed_text, checkbox_directed);
            dir_hbox.setSpacing(20);
            info_box.getChildren().add(dir_hbox);
        }

        // Label Choice Box Creation
        ChoiceBox<String> label_choices = new ChoiceBox<>();
        label_choices.setPrefWidth(150);
        label_choices.getItems().add("");
        if(hasDatabase){
            if(isVertex)
                metadata.nodeLabels().forEach(label -> label_choices.getItems().add(label));
            else
                metadata.relationLabels().forEach(label -> label_choices.getItems().add(label));
            if(emptyLabel)
                label_choices.getSelectionModel().selectFirst();
            else
                label_choices.getSelectionModel().select(current_highlight.getLabel());
        }

        // Label Selection Module
        Text label_headline = new Text("Label Info:");
        label_headline.getStyleClass().add("headline");
        Text label_text = new Text("Current Label:");
        label_text.getStyleClass().add("normal_text");
        VBox label_vbox = new VBox(label_text, label_choices);
        label_vbox.setSpacing(5);
        info_box.getChildren().addAll(label_headline, label_vbox);
        if(!hasDatabase || !modifiable) label_choices.setDisable(true);
        label_choices.getSelectionModel().selectedItemProperty().addListener(label_change_listener);


        // Line separator
        info_box.getChildren().add(new Separator());

        // Current Properties
        Text property_headline = new Text("Property Info:");
        property_headline.getStyleClass().add("headline");
        Text property_text = new Text("Current Properties:");
        property_text.getStyleClass().add("normal_text");
        info_box.getChildren().addAll(property_headline, property_text);
        VBox property_box = new VBox();
        property_box.setSpacing(10);
        PropertyRecord.setParent(property_box);
        PropertyRecord.setHost(current_highlight);
        Map<String, Value> properties = current_highlight.getElementProperties();
        for (String propertyKey : properties.keySet()){
            PropertyRecord newRecord = new PropertyRecord(modifiable);
            newRecord.setDisplay(propertyKey, String.valueOf(properties.get(propertyKey)));
            property_box.getChildren().add(newRecord);
        }
        info_box.getChildren().add(property_box);

        // Line separator
        info_box.getChildren().add(new Separator());

        // Add/Update Property Module (if modifiable)
        if(modifiable){

            Text add_property_text = new Text("Add New Properties:");
            add_property_text.getStyleClass().add("normal_text");

            // Update current property map (based on label of graph element(node/relationships))
            if(hasDatabase){
                if (isVertex)
                    current_property_map = current_highlight.getLabel() == null ? metadata.nodeProperties() : metadata.nodePropertiesOf(current_highlight.getLabel());
                else
                    current_property_map = current_highlight.getLabel() == null ? metadata.relationProperties() : metadata.relationPropertiesOf(current_highlight.getLabel());
            }

            // Create property choice box according to current property map
            property_choices = new ChoiceBox<>();
            property_choices.setPrefWidth(150);
            if (current_property_map != null)
                current_property_map.keySet().forEach(property -> property_choices.getItems().add(property));

            property_choices.getSelectionModel().selectFirst();
            property_choices.getSelectionModel()
                    .selectedItemProperty()
                    .addListener(property_change_listener);

            // Text Field
            prop_value_textfield = new TextField();
            prop_value_textfield.setPrefWidth(150);
            if(hasDatabase && current_property_map != null)
                prop_value_textfield.setPromptText("Please input a " + current_property_map.get(property_choices.getValue()));

            // Button
            Button add_prop_btn = new Button("Add/Update");
            add_prop_btn.setPrefWidth(100);
            add_prop_btn.setOnAction(e -> addProperty());

            if(!hasDatabase){
                property_choices.setDisable(true);
                prop_value_textfield.setDisable(true);
                add_prop_btn.setDisable(true);
            }

            // VBox
            VBox add_prop_vbox = new VBox(new Text("Property Type:"), property_choices, new Text("Property Value:"), prop_value_textfield , add_prop_btn);
            add_prop_vbox.setSpacing(5);

            info_box.getChildren().addAll(add_property_text, add_prop_vbox);
        }
    }

    private void hideInfoPane() {
        info_box.setVisible(false);
    }

    @FXML
    void handleZoomIn() {
        if (tab_query_constructor.isSelected())
            constructCanvas.camera.zoomIn();
        else
            resultCanvas.camera.zoomIn();
    }

    @FXML
    void handleZoomOut() {
        if (tab_query_constructor.isSelected())
            constructCanvas.camera.zoomOut();
        else
            resultCanvas.camera.zoomOut();
    }

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
        Collection<Edge> edges = constructCanvas.getEdges();
        for (Vertex v : vertices)
            outputText.append(v.toText()).append('\n');
        for (Edge e : edges)
            outputText.append(e.toText()).append('\n');
        // System.out.println(outputText);
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
    private void handleUserLoad() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(app.stage);
        try {
            if (selectedFile == null) return;
            Graph pattern_graph = parseUserPatternFromFile(selectedFile);
            constructCanvas.clearElements();
            constructCanvas.loadGraph(pattern_graph);
        } catch (FileNotFoundException fe) {
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Load Pattern Error");
            alert.setHeaderText("Cannot load the txt file.");
            if (e.getMessage().equals("NoDB"))
                alert.setContentText("Your pattern contains labels/properties while no database is loaded.\nPlease load the database first!");
            else
                alert.setContentText("Cannot parse the information from pattern file.\nPlease check your file!");
            alert.showAndWait();
        }
    }

    // Helper function to parse the pattern
    private Graph parseUserPatternFromFile(File pattern_file) throws Exception {

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
                    if (!hasDatabase && (!elements[2].equals("null") || !elements[3].equals("null")))
                        throw new Exception("NoDB");
                    String label = elements[2].equals("null") ? null : elements[2];
                    if (hasDatabase && label != null && !metadata.nodeLabels().contains(label))
                        throw new Exception("Wrong Label");
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
                    if (!hasDatabase && (!elements[4].equals("null") || !elements[5].equals("null")))
                        throw new Exception("NoDB");
                    boolean directed = Boolean.valueOf(elements[3]);
                    String label = elements[4].equals("null") ? null : elements[4];
                    if (hasDatabase && label != null && !metadata.relationLabels().contains(label))
                        throw new Exception("Wrong Label");
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
        return new Graph(nodes, relations);
    }

    public void displayRecommendedPatterns(List<Graph> patterns, boolean isCannedPattern) {
        VBox vbox_to_add_pattern = isCannedPattern ? vbox_canned_patterns : vbox_basic_patterns;
        vbox_to_add_pattern.getChildren().clear();
        if(isCannedPattern) cannedPatternCanvases.clear();
        int canvasIdx = 0;
        Iterator<Graph> it = patterns.iterator();
        while (it.hasNext()) {
            Graph pattern = it.next();

            Canvas patternCanvas = new Canvas();
            patternCanvas.setType(Canvas.CanvasType.STATIC);
            patternCanvas.loadGraph(pattern);

            patternCanvas.sizeProperty().addListener((observable, oldValue, newValue) -> {
                patternCanvas.rotateSearch(() -> {
                    Bounds bounds = patternCanvas.computeBounds();
                    return Math.max(bounds.getWidth() * patternCanvas.getHeight(),
                            bounds.getHeight() * patternCanvas.getWidth());
                });
                patternCanvas.frameAllElements(false, true);
            });

            if(!isCannedPattern)
                basicPatternCanvases.add(patternCanvas);
            else
                cannedPatternCanvases.add(patternCanvas);
            int idxCopy = canvasIdx++;
            String isCannedBinary = isCannedPattern ? "1" : "0";
            patternCanvas.setOnDragDetected(e -> {
                Dragboard dragboard = patternCanvas.startDragAndDrop(TransferMode.COPY);
                dragboard.setDragView(patternCanvas.snapshot(null, null),
                        0,
                        0);
                ClipboardContent content = new ClipboardContent();
                content.putString(isCannedBinary+ " " + idxCopy);
                dragboard.setContent(content);
            });

            AnchorPane.setTopAnchor(patternCanvas, 0.0);
            AnchorPane.setBottomAnchor(patternCanvas, 0.0);
            AnchorPane.setLeftAnchor(patternCanvas, 0.0);
            AnchorPane.setRightAnchor(patternCanvas, 0.0);

            AnchorPane patternAnchorPane = new AnchorPane();
            patternAnchorPane.setMinHeight(PATTERN_CANVAS_HEIGHT);
            patternAnchorPane.setMaxHeight(PATTERN_CANVAS_HEIGHT);

            patternAnchorPane.getChildren().add(patternCanvas);
            vbox_to_add_pattern.getChildren().add(patternAnchorPane);

            if (it.hasNext())
                vbox_to_add_pattern.getChildren().add(new Separator());
        }
    }

    public void parseRecommendedPatternFromFile(File file, boolean isCannedPattern) throws Exception {
        Scanner sc = new Scanner(file);
        List<List<String>> all_patterns = new ArrayList<>();
        List<String> pattern = null;
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("Final")) {
                pattern = new ArrayList<>();
                all_patterns.add(pattern);
            } else {
                pattern.add(line);
            }
        }
        List<Graph> pattern_graphs = new ArrayList<>();
        for (List<String> single_pattern : all_patterns)
            pattern_graphs.add(parseRecommendedPatternFromString(single_pattern));
        displayRecommendedPatterns(pattern_graphs, isCannedPattern);
    }

    public Graph parseRecommendedPatternFromString(List<String> text) throws Exception {
        Collection<Node> nodes = new HashSet<>();
        Collection<Relation> relations = new HashSet<>();
        Map<Long, Node> nodeMap = new HashMap<>();
        long currentId = GraphElement.getCurrentId();

        for (String s : text) {
            s = s.trim();
            if (!s.isEmpty()) {
                // Parse a single line
                String[] elements = s.split("\\s+");
                boolean isVertex = elements[0].equals("v");
                if (isVertex) {
                    // Vertex
                    String label = elements[2].equals("null") ? null : elements[2];
                    if (metadata != null && label != null && !metadata.nodeLabels().contains(label))
                        throw new Exception("Wrong Label");
                    Node newNode = new Node(currentId++, label, new TreeMap<>());
                    nodeMap.put(Long.parseLong(elements[1]), newNode);
                    nodes.add(newNode);
                } else {
                    // Edge
                    String label = elements[3].equals("null") ? null : elements[3];
                    if (metadata != null && label != null && !metadata.relationLabels().contains(label))
                        throw new Exception("Wrong Label");
                    Node start = nodeMap.get(Long.parseLong(elements[1]));
                    Node end = nodeMap.get(Long.parseLong(elements[2]));
                    String startLabel = start.getLabel();
                    String endLabel = end.getLabel();
                    if (metadata != null && label != null && startLabel != null && endLabel != null) {
                        Collection<String> sourceOfThisRelation = metadata.sourcesOf(label);
                        boolean startLabelCanBeSource = sourceOfThisRelation.contains(startLabel);
                        boolean endLabelCanBeSource = sourceOfThisRelation.contains(endLabel);
                        if (endLabelCanBeSource && !startLabelCanBeSource) {
                            // This relation must go from end to start, so we switch them
                            Node temp = start;
                            start = end;
                            end = temp;
                        }
                    }
                    Relation newRelation = new Relation(currentId++, true, start, end, label, new TreeMap<>());
                    relations.add(newRelation);
                }
            }
        }
        return new Graph(nodes, relations);
    }

    private void pastePatternCanvasToConstructCanvas(Canvas patternCanvas, Point2D mousePosition){
        Point2D patternViewCenter = patternCanvas.camera.getPosition();
        Point2D constructCanvasMouseInWorldPosition = constructCanvas.camera.screenToWorld(mousePosition);
        constructCanvas.loadCanvas(patternCanvas, constructCanvasMouseInWorldPosition.subtract(patternViewCenter));
    }

}
