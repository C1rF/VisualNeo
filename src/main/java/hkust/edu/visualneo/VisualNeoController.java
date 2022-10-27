package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.DbMetadata;
import hkust.edu.visualneo.utils.frontend.Constants;
import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.GraphElement;
import hkust.edu.visualneo.utils.frontend.Vertex;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.neo4j.driver.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private Pane Drawboard;
    // Variables to move the pane
    private double pane_x;
    private double pane_y;
    private double offset_x;
    private double offset_y;
    private Camera camera;
    @FXML
    private SubScene subscene_drawboard;

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

    // The scene of the main app
    private Scene scene;

    // The current focused element
    private ObjectProperty<GraphElement> highlight_element = new SimpleObjectProperty<>();

    // A list that stores all the Vertex objects and Edges objects
    public ArrayList<Vertex> listOfVertices = new ArrayList<Vertex>();
    public ArrayList<Edge> listOfEdges = new ArrayList<Edge>();

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
    private void initialize() {
        // Set the behavior when highlight_element changes
        highlight_element.addListener((observableValue, oldHighlight, newHighlight) -> {
                    // First we need to remove the highlight from the previous GraphElement
                    if (oldHighlight != null) oldHighlight.removeHighlight();
                    // Then depends on the new Highlight, we determine the display
                    if (newHighlight == null) {
                        // newHighlight is NOT a GraphElement, hide all panes
                        info_pane.setVisible(false);
                        pane_node_label.setVisible(false);
                        pane_relation_label.setVisible(false);
                        pane_property.setVisible(false);
                    } else {
                        // newHighlight is a GraphElement, show the corresponding panes
                        newHighlight.becomeHighlight();
                        refreshAllPane(newHighlight);
                    }
                }
        );
        // Create the DrawBoard for constructing the query
        // Initialize the DrawBoard
        Drawboard = new Pane();
        Drawboard.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        Drawboard.setPrefWidth(Constants.BOARD_SIZE);
        Drawboard.setPrefHeight(Constants.BOARD_SIZE);
        Drawboard.setLayoutX(Constants.BOARD_INIT_LAYOUT);
        Drawboard.setLayoutY(Constants.BOARD_INIT_LAYOUT);
        pane_x = Constants.BOARD_INIT_LAYOUT;
        pane_y = Constants.BOARD_INIT_LAYOUT;
        // Initialize the Group
        Group DrawBoard_wrapper = new Group();
        DrawBoard_wrapper.getChildren().add(Drawboard);
        // Initialize the camera
        camera = new PerspectiveCamera();
        camera.setTranslateZ(0);
        // Set the SubScene
        subscene_drawboard.setRoot(DrawBoard_wrapper);
        subscene_drawboard.setCamera(camera);
        subscene_drawboard.setFill(Color.BLUE);

        // Scroll Effect
        Drawboard.setOnScroll((e) -> {
            double deltaY = e.getDeltaY();
            if (camera.getTranslateZ() + deltaY > Constants.TranslateZ_UPPER_BOUND)
                camera.setTranslateZ(Constants.TranslateZ_UPPER_BOUND);
            else if (camera.getTranslateZ() + deltaY < Constants.TranslateZ_LOWER_BOUND)
                camera.setTranslateZ(Constants.TranslateZ_LOWER_BOUND);
            else
                camera.setTranslateZ(camera.getTranslateZ() + deltaY);
            System.out.println(camera.getTranslateZ());
        });
        // MouseEvent
        MouseEventHandler mouseHandler = new MouseEventHandler();
        Drawboard.addEventHandler(MouseEvent.ANY, mouseHandler);
        // KeyEvent
        KeyEventHandler keyHandler = new KeyEventHandler();
        Drawboard.addEventHandler(KeyEvent.ANY, keyHandler);
    }

    public void setApp(VisualNeoApp app) {
        this.app = app;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        scene.focusOwnerProperty().addListener(
                (prop, oldNode, newNode) -> {
                    // Check whether the new node is a GraphElement(Vertex or Edge)
                    if (newNode instanceof GraphElement) {
                        System.out.println("A GraphElement got focused!");
                        // Assign the new focus to the current_highlight
                        highlight_element.set((GraphElement) newNode);
                    } else {
                        // If the focused element is not a GraphElement(Vertex or Edge)
                        System.out.println("A non-GraphElement got focused!");
                        // If the focused item becomes the DrawBoard or buttons on the toolbox, set the highlight to null
                        // Otherwise, do nothing
                        if (newNode == Drawboard) highlight_element.set(null);
                    }
                }
        );

    }

    /**
     * Clear the drawing board
     */
    @FXML
    private void handleClear() {
        for (Vertex v : listOfVertices) Drawboard.getChildren().remove(v);
        for (Edge e : listOfEdges) Drawboard.getChildren().remove(e);
        listOfVertices.clear();
        listOfEdges.clear();
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
     * Event handler to handle all the MouseEvents on the DrawBoard
     */
    public class MouseEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            // If the element cannot be selected, do nothing
            if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
                // check whether it is the left key
                MouseButton button = e.getButton();
                if (button != MouseButton.PRIMARY) return;
                if (!e.isShiftDown()) {
                    System.out.println("NO SHIFT + LEFT PRESSED!");
                    // If left key is pressed, record the position
                    offset_x = e.getX();
                    offset_y = e.getY();
                    // Then remove the highlight
                    Drawboard.requestFocus();
                } else {
                    System.out.println("SHIFT + LEFT PRESSED!");
                    // Create new Vertex on the pane
                    Vertex temp_vertex = new Vertex(VisualNeoController.this, e.getX(), e.getY());
                    Drawboard.getChildren().add(temp_vertex);
                    listOfVertices.add(temp_vertex);
                    temp_vertex.requestFocus();
                }
            }

            if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                MouseButton button = e.getButton();
                if (button != MouseButton.PRIMARY) return;
                if (e.isShiftDown()) return;
                System.out.println("DRAGGED!");
                System.out.println("DRAGGED");
                pane_x += e.getX() - offset_x;
                pane_y += e.getY() - offset_y;
                if (pane_x > Constants.PANE_X_LEFT_BOUND) pane_x = Constants.PANE_X_LEFT_BOUND;
                if (pane_x < Constants.PANE_X_RIGHT_BOUND) pane_x = Constants.PANE_X_RIGHT_BOUND;
                if (pane_y > Constants.PANE_Y_TOP_BOUND) pane_y = Constants.PANE_Y_TOP_BOUND;
                if (pane_y < Constants.PANE_Y_BOTTOM_BOUND) pane_y = Constants.PANE_Y_BOTTOM_BOUND;
                System.out.println(pane_x + " " + pane_y);
                Drawboard.setLayoutX(pane_x);
                Drawboard.setLayoutY(pane_y);
            }
        }
    }

    /**
     * Event handler to handle all the MouseEvents on the DrawBoard
     */
    public class KeyEventHandler implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent e) {
            KeyCode key = e.getCode();
            switch (key) {
                case DELETE:
                case BACK_SPACE:
                    // Delete highlighted GraphElement(if there is any)
                    System.out.println("DELETE Pressed!");
                    GraphElement current_highlight = highlight_element.get();
                    if (current_highlight != null)
                        current_highlight.erase();
                    highlight_element.set(null);
                    break;
            }
        }
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
        app.queryHandler.exactSearch(listOfVertices, listOfEdges);
    }

    /**
     * Called when the user click on Similarity Search button
     */
    @FXML
    private void handleSimilaritySearch() {
    }

    @FXML
    void handleAddNodeLabel() {
        GraphElement current_highlight = highlight_element.get();
        // Add Node labels to the Vertex
        if (current_highlight instanceof Vertex) {
            current_highlight.addLabel(choicebox_node_label.getValue());
            refreshAllPane(current_highlight);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Label Addition Error");
            alert.setHeaderText("Cannot add the label.");
            alert.setContentText("Please select the correct element!");
            alert.showAndWait();
        }
    }

    @FXML
    void handleAddRelationLabel() {
        GraphElement current_highlight = highlight_element.get();
        // Add Relation labels to the Edge
        if (current_highlight instanceof Edge) {
            current_highlight.addLabel(choicebox_relation_label.getValue());
            refreshAllPane(current_highlight);
        } else {
            throw new RuntimeException("Bug Found!");
        }
    }


    @FXML
    void handleAddProperty() {
        GraphElement current_highlight = highlight_element.get();
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
        Drawboard.getScene().setCursor(Cursor.HAND);
    }

    /**
     * Called when the user's mouse leaves a button
     */
    @FXML
    private void handleMouseLeaveButton(MouseEvent m) {
        Drawboard.getScene().setCursor(Cursor.DEFAULT);
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
        HashMap<String, Value> properties = current_highlight.getProp();
        for (String propertyKey : properties.keySet()) {
            builder.append(propertyKey).append(" : ").append(properties.get(propertyKey)).append("\n");
        }
        text_property_info.setText(builder.toString());
    }

    @FXML
    void handleZoomIn() {
        // Increase the TranslateZ value
        if (camera.getTranslateZ() + Constants.UNIT_Z_CHANGE > Constants.TranslateZ_UPPER_BOUND)
            camera.setTranslateZ(Constants.TranslateZ_UPPER_BOUND);
        else
            camera.setTranslateZ(camera.getTranslateZ() + Constants.UNIT_Z_CHANGE);
        System.out.println(camera.getTranslateZ());
    }

    @FXML
    void handleZoomOut() {
        // Decrease the TranslateZ value
        if (camera.getTranslateZ() - Constants.UNIT_Z_CHANGE < Constants.TranslateZ_LOWER_BOUND)
            camera.setTranslateZ(Constants.TranslateZ_LOWER_BOUND);
        else
            camera.setTranslateZ(camera.getTranslateZ() - Constants.UNIT_Z_CHANGE);
        System.out.println(camera.getTranslateZ());
    }

    public GraphElement getHighlight() {
        return highlight_element.get();
    }

    public void createEdgeBetween(Vertex start, Vertex end) {
        Edge temp_edge = new Edge(this, start, end, false);
        Drawboard.getChildren().add(temp_edge);
        temp_edge.toBack();
        listOfEdges.add(temp_edge);
        temp_edge.requestFocus();
    }
}
