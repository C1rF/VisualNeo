package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.frontend.*;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class VisualNeoController {

    private VisualNeoApp app;

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

    @FXML
    private Pane Drawboard;

    @FXML
    private Button btn_load_db;

    @FXML
    private Button btn_generate_p;

    @FXML
    private Button btn_exact_search;

    @FXML
    private Button btn_similarity_search;

    // ALL Status
    public enum Status {EMPTY, VERTEX , EDGE_1, EDGE_2, ERASE, SELECT};
    // Current Status
    public static Status s;


    // A list that stores all the Vertex objects and Edges objects
    public ArrayList<Vertex> listOfVertices = new ArrayList<Vertex>();
    public ArrayList<Edge> listOfEdges = new ArrayList<Edge>();

    // A temperate startVertex
    Vertex startVertex;

    /**
     * The constructor.
     * The constructor is called before initialize() method.
     */
    public VisualNeoController() { }

    public void setApp(VisualNeoApp app) {
        this.app = app;
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
         s = Status.EMPTY;
    }

    /**
     * Called when the user clicks on the clear button.
     * Clear the drawing board
     */
    @FXML
    private void handleClear() {
        listOfVertices.clear();
        listOfEdges.clear();
        Drawboard.getChildren().clear();
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
        listOfVertices.clear();
        Drawboard.getChildren().clear();
        // Load the new data
    }

    /**
     * Called when the user clicks the drawing board
     */
    @FXML
    private void handleClickOnBoard(MouseEvent m) {
        // System.out.println("Clicked at " + m.getX() + ' ' +  m.getY());
        // If the status is VERTEX, meaning that we need to create the vertex
        if(s == Status.VERTEX) {
            Vertex temp_vertex = new Vertex(m.getX(), m.getY());
            listOfVertices.add(temp_vertex);
            Drawboard.getChildren().add(temp_vertex);
            temp_vertex.requestFocus();
        }

        // If the status is ERASE, meaning that we need to erase the focused Vertex
        if(s == Status.ERASE) {

            // Check whether a vertex is selected
            Vertex temp_vertex = getFocusedVertex();
            if(temp_vertex != null) {
                // Now we find the vertex that needs to be removed
                // First remove all its connected edges
                int numOfEdges = listOfEdges.size();
                List<Integer> index_list = new ArrayList<Integer>();
                for(int j=0; j<numOfEdges; j++){
                    Edge temp_edge = listOfEdges.get(j);
                    if( temp_edge.startVertex == temp_vertex || temp_edge.endVertex == temp_vertex){
                        // Remove the edge both from the board and the ArrayList
                        Drawboard.getChildren().remove(temp_edge);
                        index_list.add(j);
                    }
                }
                for(int t = index_list.size()-1; t >= 0; t--){
                    int j = index_list.get(t);
                    listOfEdges.remove(j);
                }

                // Then remove the vertex itself
                Drawboard.getChildren().remove(temp_vertex);
                listOfVertices.remove(temp_vertex);

                System.out.println("Successfully removed a vertex");
            }

            // Check whether an edge is selected
            Edge temp_edge = getFocusedEdge();
            if(temp_edge != null){
                // Only need to remove the edge
                Drawboard.getChildren().remove(temp_edge);
                listOfEdges.remove(temp_edge);
                System.out.println("Successfully removed an edge");
            }
        }

        // If the status is EDGE_1/EDGE_2, meaning that we are forming the EDGE
        if(s == Status.EDGE_1 || s == Status.EDGE_2) {
            int numOfVertices = listOfVertices.size();
            for(int i =0; i < numOfVertices; i++) {
                Vertex temp = listOfVertices.get(i);
                if(temp.isFocused()) {
                    // If the status is EDGE_2, meaning that we are choosing the second Vertex
                    if(s == Status.EDGE_2){
                        // Create a new Edge between the two if they are not the same vertex
                        if(startVertex != temp){
                            Edge temp_edge =  new Edge(startVertex, temp, false);
                            listOfEdges.add(temp_edge);
                            Drawboard.getChildren().add(temp_edge);
                            temp_edge.toBack();
                        }
                        // No matter whether the edge is created or not
                        // Return to EDGE_1 state and remove focus
                        s = Status.EDGE_1;
                        // Remove the focus
                        Drawboard.requestFocus();
                        break;
                    }
                    // If the status is EDGE_1, meaning that we are choosing the first Vertex
                    if(s == Status.EDGE_1) {
                        startVertex = temp;
                        s = Status.EDGE_2;
                        break;
                    }
                }
            }
        }
        // If the current state is SELECT, we move the focus to the drawing board.
        // Note that here we already exclude the case when user clicks a vertex or an edge
        // Because in that case, the mouse event will be consumed in the Vertex/Edge object
        // and will not be passed to drawing board
        if(s == Status.SELECT) {
            Drawboard.requestFocus();
        }
    }

    /**
     * Called when the user drag on the drawing board
     */
    @FXML
    private void handleDragOnBoard() {
        // If the status is SELECT, meaning that we possibly need to move a vertex and relevant edges
        if (s == Status.SELECT) {
            Vertex temp = getFocusedVertex();
            if(temp != null) {
                // temp is the focused vertex
                // System.out.println("Drag the vertex");
                int numOfEdges = listOfEdges.size();
                for(int j=0; j<numOfEdges; j++){
                    Edge temp_edge = listOfEdges.get(j);
                    if( temp_edge.startVertex == temp || temp_edge.endVertex == temp){
                        temp_edge.updatePos();
                    }
                }
            }
        }
    }

    /**
     * Called when the user click on Load Database button
     */
    @FXML
    private void handleLoadDB() {

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

    private void lockAllVertices(){
        int numOfVertices = listOfVertices.size();
        for(int i =0; i < numOfVertices; i++) {
            listOfVertices.get(i).canSelect = false;
        }
    }
    private void lockAllEdges(){
        int numOfEdges = listOfEdges.size();
            for(int i =0; i < numOfEdges; i++) {
            listOfEdges.get(i).canSelect = false;
        }
    }
    private void unlockAllVertices(){
        int numOfVertices = listOfVertices.size();
        for(int i =0; i < numOfVertices; i++) {
            listOfVertices.get(i).canSelect = true;
        }
    }
    private void unlockAllEdges(){
        int numOfEdges = listOfEdges.size();
        for(int i =0; i < numOfEdges; i++) {
            listOfEdges.get(i).canSelect = true;
        }
    }

    public static Status getStatus(){
        return s;
    }

    public Vertex getFocusedVertex(){
        int numOfVertices = listOfVertices.size();
        for(int i =0; i < numOfVertices; i++) {
            Vertex temp = listOfVertices.get(i);
            if(temp.isFocused()) {
                // We find the vertex
                return temp;
            }
        }
        return null;
    }
    public Edge getFocusedEdge(){
        int numOfEdges = listOfEdges.size();
        for(int i =0; i < numOfEdges; i++) {
            Edge temp = listOfEdges.get(i);
            if(temp.isFocused()) {
                // We find the edge
                return temp;
            }
        }
        return null;
    }

}
