package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.DbMetadata;
import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.GraphElement;
import hkust.edu.visualneo.utils.frontend.Vertex;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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
     * 7 Buttons in the toolbox and 1 Label indicating the current tool
     */
    @FXML
    private Button btn_clear;
    @FXML
    private Button btn_select;
    @FXML
    private Button btn_vertex;
    @FXML
    private Button btn_edge;
    @FXML
    private Button btn_erase;
    @FXML
    private Button btn_save;
    @FXML
    private Button btn_load;
    @FXML
    private Label label_current_state;
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
    private Pane Drawboard;
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

    // ALL Status
    public enum Status {EMPTY, VERTEX, EDGE_1, EDGE_2, ERASE, SELECT}

    ;
    // Current Status
    public static Status s;

    // A list that stores all the Vertex objects and Edges objects
    public ArrayList<Vertex> listOfVertices = new ArrayList<Vertex>();
    public ArrayList<Edge> listOfEdges = new ArrayList<Edge>();

    // A temperate startVertex
    Vertex startVertex;

    // Camera for zoom in/out and move DrawBoard
    PerspectiveCamera camera = new PerspectiveCamera();

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
        s = Status.EMPTY;
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
                        Button[] draw_btns = {btn_clear, btn_select, btn_vertex, btn_edge, btn_erase, btn_save, btn_load};
                        boolean is_btn = false;
                        for (Button btn : draw_btns) {
                            if (btn == newNode) {
                                is_btn = true;
                                break;
                            }
                        }
                        if (newNode == Drawboard || is_btn) highlight_element.set(null);
                    }
                }
        );

        subscene_drawboard.setRoot(Drawboard);
        subscene_drawboard.setCamera(camera);
        subscene_drawboard.setFill(Color.BLUE);
        camera.translateZProperty().set(-100);
        app.stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case W:
                    camera.setTranslateZ(camera.getTranslateZ() + 150);
                    break;
                case S:
                    camera.setTranslateZ(camera.getTranslateZ() - 150);
                    break;
            }
        });
        Drawboard.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            handleClickOnBoard(e);
        });

    }

    /**
     * Called when the user clicks on the clear button.
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
     * Called when the user clicks the select button.
     * Select a vertex
     */
    @FXML
    private void handleSelect() {
        s = Status.SELECT;
        label_current_state.setText("Current Tool: Select");
        unlockAllVertices();
        unlockAllEdges();
    }

    /**
     * Called when the user clicks the vertex button.
     * Create a vertex
     */
    @FXML
    private void handleVertex() {
        s = Status.VERTEX;
        label_current_state.setText("Current Tool: Vertex");
        lockAllVertices();
        lockAllEdges();
    }

    /**
     * Called when the user clicks the Edge button.
     * Create an edge
     */
    @FXML
    private void handleEdge() {
        s = Status.EDGE_1;
        label_current_state.setText("Current Tool: Edge");
        unlockAllVertices();
        lockAllEdges();
    }

    /**
     * Called when the user clicks the Erase button.
     * Erase a vertex and all connected edges
     */
    @FXML
    private void handleErase() {
        s = Status.ERASE;
        label_current_state.setText("Current Tool: Erase");
        unlockAllVertices();
        unlockAllEdges();
    }

    /**
     * Called when the user clicks the Save button.
     * Save the drawing pattern
     */
    @FXML
    private void handleSave() {
    }

    /**
     * Called when the user clicks the Load button.
     * Load the drawing pattern
     */
    @FXML
    private void handleLoad() {
        // Load the new data
    }

    /**
     * Called when the user clicks the drawing board
     */
    @FXML
    private void handleClickOnBoard(MouseEvent m) {
        // If the status is VERTEX, we need to create the vertex
        if (s == Status.VERTEX) {
            Vertex temp_vertex = new Vertex(m.getX(), m.getY());
            Drawboard.getChildren().add(temp_vertex);
            listOfVertices.add(temp_vertex);
            temp_vertex.requestFocus();
        }

        // If the status is ERASE, we need to erase the focused Vertex/Edge
        if (s == Status.ERASE) {
            GraphElement current_highlight = highlight_element.get();
            // Check whether it is a Vertex
            if (current_highlight instanceof Vertex focused_vertex) {
                // Remove the vertex
                highlight_element.set(null);
                focused_vertex.eraseFrom(this);
                // For testing
                System.out.println("Successfully removed a vertex");
            }
            // Check whether it is an Edge
            if (current_highlight instanceof Edge focused_edge) {
                // Remove the edge
                highlight_element.set(null);
                focused_edge.eraseFrom(this);
                // For testing
                System.out.println("Successfully removed an edge");
            }
        }

        // If the status is EDGE_1/EDGE_2, meaning that we are forming the EDGE
        if (s == Status.EDGE_1 || s == Status.EDGE_2) {

            GraphElement current_highlight = highlight_element.get();
            if (!(current_highlight instanceof Vertex)) return;

            Vertex focused_vertex = (Vertex) current_highlight;
            // If the status is EDGE_2, meaning that we are choosing the second Vertex
            if (s == Status.EDGE_2) {
                /** Case 1: User clicks the white space, we do nothing but remove the highlight */
                if (startVertex == focused_vertex && Drawboard.getScene().getCursor() != Cursor.HAND) {
                    Drawboard.requestFocus();
                }
                /** Case 2: User does want to create an edge between node(s) */
                else {
                    Edge temp_edge = new Edge(startVertex, focused_vertex, false);
                    Drawboard.getChildren().add(temp_edge);
                    temp_edge.toBack();
                    listOfEdges.add(temp_edge);
                    startVertex = null;
                    temp_edge.requestFocus();
                }
                // No matter whether the edge is created or not, return to EDGE_1 state
                s = Status.EDGE_1;
            }
            // If the status is EDGE_1, meaning that we are choosing the first Vertex
            else if (s == Status.EDGE_1) {
                startVertex = focused_vertex;
                s = Status.EDGE_2;
            }
        }
        // If the current state is SELECT, we move the focus to the drawing board.
        // Note that here we already exclude the case when user clicks a vertex or an edge
        // Because in that case, the mouse event will be consumed in the Vertex/Edge object
        // and will not be passed to drawing board
        if (s == Status.SELECT) {
            Drawboard.requestFocus();
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

    private void lockAllVertices() {
        for (Vertex v : listOfVertices)
            v.canSelect = false;
    }

    private void lockAllEdges() {
        for (Edge e : listOfEdges)
            e.canSelect = false;
    }

    private void unlockAllVertices() {
        for (Vertex v : listOfVertices)
            v.canSelect = true;
    }

    private void unlockAllEdges() {
        for (Edge e : listOfEdges)
            e.canSelect = true;
    }

    public static Status getStatus() {
        return s;
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
    }

    @FXML
    void handleZoomOut() {

    }

}
