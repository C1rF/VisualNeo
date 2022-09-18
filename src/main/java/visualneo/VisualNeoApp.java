package visualneo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import visualneo.utils.frontend.*;

import java.util.ArrayList;

public class VisualNeoApp extends Application {
    // Current Status
    public enum Status {EMPTY, VERTEX , EDGE_1, EDGE_2, ERASE, SELECT};
    public Status s = Status.EMPTY;
    Text current_status;

    // Current Mouse Status
    boolean mouseIsPressed = false;

    // A list that stores all the Vertex objects and Edges objects
    ArrayList<Vertex> listOfVertices = new ArrayList<Vertex>();
    ArrayList<Edge> listOfEdges = new ArrayList<Edge>();

    // A temperate startVertex
    Vertex startVertex;


    @Override
    public void start(Stage stage) {
        initUI(stage);
    }
    public static void main(String[] args) {
        launch();
    }
    public void initUI(Stage stage){
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        System.out.println("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

        BorderPane mainBoard = new BorderPane();
        implementMenuBar(mainBoard);
        implementLeft(mainBoard);
        implementRight(mainBoard);
        implementCenter(mainBoard);

        Scene scene = new Scene(mainBoard, 1200, 700);

        stage.setScene(scene);
        stage.setTitle("Our FYP");
        stage.setOnCloseRequest(e->{
            Platform.exit(); System.exit(0); });
        stage.show();
    }
    public void implementMenuBar(BorderPane mainBoard){
        MenuBar menuBar = new MenuBar();
        VBox menuVBox = new VBox(menuBar);
        //menuVBox.setPrefHeight(40);
        menuBar.setPrefHeight(40);
        menuBar.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        // Menu-1: Application
        Menu menuApp = new Menu("Application");
        MenuItem about_us = new MenuItem("About Us");
        MenuItem exit = new MenuItem("Exit");
        menuApp.getItems().add(about_us);
        menuApp.getItems().add(exit);
        menuApp.setStyle("-fx-font-size: 14px;");
        menuBar.getMenus().add(menuApp);

        // Menu-2: File
        Menu menuFile = new Menu("File");
        MenuItem a1 = new MenuItem("1");
        MenuItem a2 = new MenuItem("2");
        menuFile.getItems().add(a1);
        menuFile.getItems().add(a2);
        menuFile.setStyle("-fx-font-size: 14px;");
        menuBar.getMenus().add(menuFile);

        // Menu-3: Help
        Menu menuHelp = new Menu("Help");
        MenuItem b1 = new MenuItem("1");
        MenuItem b2 = new MenuItem("2");
        menuHelp.getItems().add(b1);
        menuHelp.getItems().add(b2);
        menuHelp.setStyle("-fx-font-size: 14px;");
        menuBar.getMenus().add(menuHelp);
        // Set the mainBoard
        mainBoard.setTop(menuVBox);
    }
    public void implementLeft(BorderPane mainBoard){
        VBox leftVBox = new VBox();
        // Implement upper left function buttons
        implementFunctionButton(leftVBox);
        // Implement bottom left label panels
        implementLabelPanel(leftVBox);
        mainBoard.setLeft(leftVBox);
    }
    public void implementFunctionButton(VBox leftVBox){
        // Implement upper left function buttons
        VBox functionButtons = new VBox();
        functionButtons.setPrefHeight(300);
        functionButtons.setPrefWidth(200);
        functionButtons.setBackground(new Background(new BackgroundFill(Color.YELLOW, CornerRadii.EMPTY, Insets.EMPTY)));

        Button btn_load = new Button("Load Database");
        Button btn_generate = new Button("Generate labels");

        btn_load.setPrefWidth(200);
        btn_load.setPrefHeight(60);
        btn_load.setStyle("-fx-font-size: 20px;");

        btn_generate.setPrefWidth(200);
        btn_generate.setPrefHeight(60);
        btn_generate.setStyle("-fx-font-size: 20px;");

        functionButtons.getChildren().add(btn_load);
        functionButtons.getChildren().add(btn_generate);

        leftVBox.getChildren().add(functionButtons);
    }
    public void implementLabelPanel(VBox leftVBox){
        // Implement bottom left label panels
        VBox labelPanels = new VBox();
        labelPanels.setPrefHeight(400);
        labelPanels.setPrefWidth(200);
        labelPanels.setBackground(new Background(new BackgroundFill(Color.GAINSBORO, CornerRadii.EMPTY, Insets.EMPTY)));

        leftVBox.getChildren().add(labelPanels);
    }
    public void implementRight(BorderPane mainBoard){
        VBox rightVBox = new VBox();
        rightVBox.setPrefHeight(700);
        rightVBox.setPrefWidth(200);
        rightVBox.setBackground(new Background(new BackgroundFill(Color.GREENYELLOW, CornerRadii.EMPTY, Insets.EMPTY)));

        mainBoard.setRight(rightVBox);
    }
    public void implementCenter(BorderPane mainBoard){
        VBox center = new VBox();
        center.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        // Tool Bar for Drawing Board
        HBox toolBar = new HBox();
        toolBar.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        final int TOOL_NUM = 8;
        String[] names = new String[]{"Clear","Select","Vertex","Edge","Erase","Undo","Save","Load"};
        Button[] tools = new Button[TOOL_NUM];
        for(int i=0; i<TOOL_NUM; i++){
            tools[i] = new Button(names[i]);
            tools[i].setPrefWidth(60);
            tools[i].setPrefHeight(40);
            toolBar.getChildren().add(tools[i]);
        }
        current_status = new Text("Current Tool:");
        toolBar.getChildren().add(current_status);


        // Drawing Board
        // TODO: Implement the Drawing Board
        Pane drawingBoard = new Pane();
        drawingBoard.setBackground(new Background(new BackgroundFill(Color.color(0.992,0.9608,0.902), CornerRadii.EMPTY, Insets.EMPTY)));
        drawingBoard.setMinWidth(800);
        drawingBoard.setMinHeight(620);
        drawingBoard.setFocusTraversable(true);
        center.getChildren().add(toolBar);
        center.getChildren().add(drawingBoard);
        mainBoard.setCenter(center);

        // mouse pressed makes the box
        drawingBoard.addEventHandler
                (  MouseEvent.MOUSE_PRESSED,
                        (MouseEvent m) ->
                        {
                            mouseIsPressed = true;
                            //System.out.println("Clicked at " + m.getX() + ' ' +  m.getY());
                            // If the status is VERTEX, meaning that we need to create the vertex
                            if(s == Status.VERTEX) {
                                Vertex temp_vertex = new Vertex(m.getX(), m.getY());
                                listOfVertices.add(temp_vertex);
                                drawingBoard.getChildren().add(temp_vertex);
                                temp_vertex.requestFocus();
                            }
                            // If the status is ERASE, meaning that we need to erase the focused Vertex
                            if(s == Status.ERASE) {
                                int numOfVertices = listOfVertices.size();
                                for(int i =0; i < numOfVertices; i++) {
                                    if(listOfVertices.get(i).isFocused()) {
                                        drawingBoard.getChildren().remove(listOfVertices.get(i));
                                        listOfVertices.remove(i);
                                        break;
                                    }
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
                                            // Create a new Edge between the two
                                            Edge temp_edge =  new Edge(startVertex, temp);
//                                            temp_edge.setStartX(startVertex.x);
//                                            temp_edge.setStartY(startVertex.y);
//                                            temp_edge.setEndX(temp.x);
//                                            temp_edge.setEndY(temp.y);
                                            listOfEdges.add(temp_edge);
                                            drawingBoard.getChildren().add(temp_edge);
                                            temp_edge.toBack();
                                            s = Status.EDGE_1;
                                            // Remove the focus
                                            drawingBoard.requestFocus();
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
                            // Note that here we already exclude the case when user clicks a vertex
                            // Because in that case, the mouse event will be consumed in the Vertex object
                            // and will not be passed to drawing board
                            if(s == Status.SELECT) {
                                drawingBoard.requestFocus();
                            }
                        }
                );

        drawingBoard.addEventHandler
                (  MouseEvent.MOUSE_DRAGGED,
                        (MouseEvent m) ->
                        {
                            // If the status is SELECT, meaning that we possibly need to move a vertex and relevant edges
                            if (s == Status.SELECT) {
                                int numOfVertices = listOfVertices.size();
                                for(int i =0; i < numOfVertices; i++) {
                                    Vertex temp = listOfVertices.get(i);
                                    if(temp.isFocused()) {
                                        // Now we know that temp is focused
                                        System.out.println("Drag the vertex");

                                    }
                                }
                            }
                            // If the status is ERASE, meaning that we need to erase the focused Vertex
                        }
                );

        // mouse released does nothing (except note that mouse is up)
        drawingBoard.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent m) -> {mouseIsPressed = false;});

        // Set the functions of the buttons
        // Button-0: Clear
        tools[0].setOnAction((e) -> {
                    // the status will not change
                    listOfVertices.clear();
                    drawingBoard.getChildren().clear();
                }
        );

        // Button-1: Select
        tools[1].setOnAction((e) -> {
                    s = Status.SELECT;
                    current_status.setText("Current Tool: Select");
                    unlockAllVertices();
                }
        );

        // Button-2: Vertex
        tools[2].setOnAction((e) -> {
                    s = Status.VERTEX;
                    current_status.setText("Current Tool: Vertex");
                    lockAllVertices();
                }
        );
        // Button-3: Edge
        tools[3].setOnAction((e) -> {
                    s = Status.EDGE_1;
                    current_status.setText("Current Tool: Edge");
                    unlockAllVertices();
                }
        );
        // Button-4: Erase
        tools[4].setOnAction((e) -> {
                    s = Status.ERASE;
                    current_status.setText("Current Tool: Erase");
                    unlockAllVertices();
                }
        );
        // Button-5: Undo
        tools[5].setOnAction((e) -> {

                }
        );
        // Button-5: Save
        tools[6].setOnAction((e) -> {

                }
        );
        // Button-6: Load
        tools[7].setOnAction((e) -> {
                    listOfVertices.clear();
                    drawingBoard.getChildren().clear();
                    // Load the new data
                }
        );

    }

    // Two helper functions
    private void lockAllVertices(){
        int numOfVertices = listOfVertices.size();
        for(int i =0; i < numOfVertices; i++) {
            listOfVertices.get(i).canSelect = false;
        }
    }
    private void unlockAllVertices(){
        int numOfVertices = listOfVertices.size();
        for(int i =0; i < numOfVertices; i++) {
            listOfVertices.get(i).canSelect = true;
        }
    }

    public Status getStatus(){
        return s;
    }
}
