package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.DbMetadata;
import hkust.edu.visualneo.utils.frontend.Canvas;
import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.GraphElement;
import hkust.edu.visualneo.utils.frontend.Vertex;
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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.value.FloatValue;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.internal.value.StringValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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

    DbMetadata metadata;

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
        canvas.getHighlights().addListener((SetChangeListener<GraphElement>) c -> {
            if (canvas.getHighlights().size() == 1)
                refreshAllPane(canvas.getSingleHighlight());
            else
                hideAllPane();
        });

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
                GraphElement current_highlight = canvas.getSingleHighlight();
                Edge highlight_edge = (Edge) current_highlight;
                if(highlight_edge != null) highlight_edge.setDirected(newValue);
            }
        };
        tab_pane.onKeyPressedProperty().bind(canvas.onKeyPressedProperty());
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

        // TODO: Show the schema

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
        if(canvas.getSingleHighlight() != null) refreshAllPane(canvas.getSingleHighlight());
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
        try{
            app.queryHandler.exactSearch(listOfVertices,listOfEdges);
        }catch (Exception e){
           String errorMsg = e.getMessage();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exact Search Error");
            alert.setHeaderText("Cannot perform the exact search!");
            alert.setContentText(errorMsg);
            alert.showAndWait();
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
        GraphElement current_highlight = canvas.getSingleHighlight();
        current_highlight.setLabel(choicebox_node_label.getValue());
        refreshAllPane(current_highlight);
    }

    @FXML
    void handleAddRelationLabel() {
        GraphElement current_highlight = canvas.getSingleHighlight();
        current_highlight.setLabel(choicebox_relation_label.getValue());
        refreshAllPane(current_highlight);
    }

    @FXML
    void handleAddProperty() {
        GraphElement current_highlight = canvas.getSingleHighlight();
        // Add Node/Relation properties to the Node/Relation
        String prop_name = choicebox_property_name.getValue();
        String prop_type = current_property_map.get(prop_name);
        String prop_value_text = textfield_property_value.getText().trim();
        Value prop_value = parsePropValue(prop_type, prop_value_text);
        if (prop_value != null) {
            current_highlight.addProperty(prop_name, prop_value);
            textfield_property_value.clear();
            refreshAllPane(current_highlight);
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
                return new StringValue(input);
            }
            else if(type.equals("Long")){
                long num = Long.parseLong(input);
                return new IntegerValue(num);
            }
            else if(type.equals("Float")){
                double num = Double.parseDouble(input);
                return new FloatValue(num);
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
     * Given a GraphElement, REFRESH the InfoPane according to the information
     */
    private void refreshAllPane(GraphElement current_highlight) {
        info_pane.setVisible(true);
        pane_property.setVisible(true);
        choicebox_property_name.getSelectionModel()
                .selectedItemProperty().removeListener(property_change_listener);
        choicebox_property_name.getItems().clear();
        checkbox_directed.selectedProperty().removeListener(directed_change_listener);
        if (current_highlight instanceof Vertex) {
            // Node Info Pane (bottom-right pane)
            pane_node_label.setVisible(true);
            pane_relation_label.setVisible(false);
            text_node_or_relation.setText("Node Information");
            // Update property choices
            if(metadata == null) return;
            if(current_highlight.getLabel() == null)
                current_property_map = metadata.nodeProperties();
            else
                current_property_map = metadata.nodePropertiesOf(current_highlight.getLabel());
            if(current_property_map != null)
                current_property_map.keySet().forEach(property -> choicebox_property_name.getItems().add(property));
            else
                choicebox_property_name.getItems().removeAll();
        } else {
            // Relation Info Pane (bottom-right pane)
            pane_node_label.setVisible(false);
            pane_relation_label.setVisible(true);
            text_node_or_relation.setText("Relation Information");
            // Display the checkbox according to the current status
            Edge highlight_edge = (Edge) current_highlight;
            checkbox_directed.setSelected(highlight_edge.isDirected());
            checkbox_directed.selectedProperty().addListener(directed_change_listener);
            // Update property choices
            if(metadata == null) return;
            if(current_highlight.getLabel() == null)
                current_property_map = metadata.relationProperties();
            else
                current_property_map = metadata.relationPropertiesOf(current_highlight.getLabel());
            if(current_property_map != null)
                current_property_map.keySet().forEach(property -> choicebox_property_name.getItems().add(property));
            else
                choicebox_property_name.getItems().removeAll();
        }

        // Set the listener for choicebox_property_name
        choicebox_property_name.getSelectionModel().selectFirst();
        choicebox_property_name.getSelectionModel()
                .selectedItemProperty()
                .addListener(property_change_listener);

        // Display the information on the information pane
        StringBuilder builder = new StringBuilder();
        text_label_info.setText(current_highlight.getLabel());
        Map<String, Value> properties = current_highlight.getProp();
        for (String propertyKey : properties.keySet()) {
            builder.append(propertyKey).append(" : ").append(properties.get(propertyKey)).append("\n");
        }
        text_property_info.setText(builder.toString());
    }

    private void hideAllPane() {
        info_pane.setVisible(false);
        pane_property.setVisible(false);
        pane_node_label.setVisible(false);
        pane_relation_label.setVisible(false);
    }

    @FXML
    void handleZoomIn() { canvas.camera.zoomIn();}

    @FXML
    void handleZoomOut() { canvas.camera.zoomOut();}

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
        canvas.clearElements();
    }

    /**
     * Save the drawing pattern
     */
    @FXML
    private void handleSave() {
        String outputText = "";
        List<Vertex> vertices = canvas.getVertices();
        List<Edge> edges =  canvas.getEdges();
        for(Vertex v : vertices)
            outputText += v.toText() + '\n';
        for(int i = edges.size()-1; i >= 0; i--){
            Edge e = edges.get(i);
            outputText += e.toText() + '\n';
        }
        System.out.println(outputText);
        FileChooser fileChooser = new FileChooser();
        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        //Show save file dialog
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
        File selectedFile = fileChooser.showOpenDialog(app.stage);
        try
        {
            Scanner sc = new Scanner(selectedFile);
            canvas.clearElements();
            while ( sc.hasNextLine() )
            {
                // For a single line
                String s = sc.nextLine().trim();
                if(!s.isEmpty()) {
                    parseOneLine(s);
                }
            }
        }
        catch(FileNotFoundException fe){
            System.out.println("Cannot find the file!");
        }
        catch( Exception ee ){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Load Data Error");
            alert.setHeaderText("Cannot load the text file.");
            alert.setContentText("Please check the format of the txt file!");
            alert.showAndWait();
        }
    }

    // Helper Function to parse one line
    private void parseOneLine(String line){
        if(line.isEmpty()) return;
        String[] elements = line.split("\\s+");
        if(elements[0].equals("v")){
            // current line describes a Vertex
            double x = Double.parseDouble(elements[2]);
            double y = Double.parseDouble(elements[3]);
            canvas.createVertex(x,y);
        }
        else if(elements[0].equals("e")){
            // current line describes an Edge
            List<Vertex> vertices = canvas.getVertices();
            int startVertexId = Integer.parseInt(elements[1]);
            int endVertexId = Integer.parseInt(elements[2]);
            boolean directed = Boolean.valueOf(elements[3]);
            canvas.createEdge(vertices.get(startVertexId), vertices.get(endVertexId), directed);
        }
        GraphElement newElement = canvas.getSingleHighlight();
        // parse Label
        String label = elements[4];
        if(!label.equals("null")) newElement.setLabel(label);
        // parse Properties
        for(int i = 5; i < elements.length; i++){
            String property = elements[i];
            if(property.equals("null")) break;
            String[] splitProperty = property.split(":");
            String propertyName = splitProperty[0];
            String propertyType;
            if(newElement instanceof Vertex){
                propertyType = metadata.nodeProperties().get(propertyName);
            }else {
                propertyType = metadata.relationProperties().get(propertyName);
            }
            String propertyValueText = splitProperty[1];
            Value propertyValue = parsePropValue(propertyType, propertyValueText);
            newElement.addProperty(propertyName, propertyValue);
        }
    }

}
