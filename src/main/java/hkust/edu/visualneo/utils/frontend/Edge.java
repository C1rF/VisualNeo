package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.VisualNeoController;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.lang.Math;

public class Edge extends GraphElement {

    public Vertex startVertex;
    public Vertex endVertex;
    String relationship;

    public boolean directed;

    public Line edge;

    public Edge(Vertex startVertex, Vertex endVertex, boolean directed){
        super();
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        label_displayed = new Text("");
        // Set the position (of the StackPane)
        setPos();
        // Set the line
        edge = new Line();
        setLine();
        edge.setStroke(Color.BLACK);
        edge.setStrokeWidth(4);
        // Display the line on the StackPane
        getChildren().addAll(edge, label_displayed);

        // Set focus effect
        setFocusEffect();

        // Give them event handler
        edge.addEventHandler( MouseEvent.MOUSE_PRESSED, e->{if(canSelect) { pressed(e);}  } );
        edge.addEventHandler( MouseEvent.MOUSE_ENTERED, e->{if(canSelect) { mouseEntered(e);}  } );
        edge.addEventHandler( MouseEvent.MOUSE_EXITED, e->{if(canSelect) { mouseExited(e);}  } );

        // For Testing
        // setBackground(new Background(new BackgroundFill(Color.YELLOWGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        System.out.println("An Edge from (" + startVertex.x + " , " + startVertex.y + ") to " +
                "(" + endVertex.x + " , " + endVertex.y + ")" + " is created!");
    }

    /**
     *  Request focus when pressed and status is ERASE
     */
    public void setFocusEffect() {
        setFocusTraversable(true);
        focusedProperty().addListener(
                (obs, oldVal, newVal) ->
                {
                    if(newVal) {
                        edge.setStrokeWidth(5);
                        isFocused = true;
                    }
                    if(oldVal) {
                        edge.setStrokeWidth(4);
                        isFocused = false;
                    }
                }
        );
    }


    /**
     *  Request focus when pressed
     */
    private void pressed(MouseEvent m) {
        if(!isFocused) requestFocus();
        if(VisualNeoController.getStatus() == VisualNeoController.Status.SELECT){
            m.consume();
        }
    }

    /**
     * Set the position of the line
     */
    public void setLine(){
        Vertex leftOne = smallXVertex();
        Vertex rightOne = getTheOtherSide(leftOne);
        double start_y;
        double end_y;
        if(leftOne.y < rightOne.y){
            start_y = 0;
            end_y = diff(startVertex.y, endVertex.y);
        }
        else{
            start_y = diff(startVertex.y, endVertex.y);
            end_y = 0;
        }
        edge.setStartX(0);
        edge.setStartY(start_y);
        edge.setEndX(diff(startVertex.x, endVertex.x));
        edge.setEndY(end_y);
    }

    /**
     * Set the position of the StackPane
     */
    public void setPos(){
        setLayoutX(minValue(startVertex.x,endVertex.x) + VERTEX_RADIUS);
        setLayoutY(minValue(startVertex.y,endVertex.y) + VERTEX_RADIUS);
    }

    /**
     * Update the position of the StackPane and the line inside
     */
    public void updatePos(){
        setPos();
        setLine();
    }

    private double minValue(double x1, double x2){
        return ((x1 > x2)? x2: x1);
    }
    private double diff(double x1, double x2){
        return Math.abs(x1-x2);
    }
    private Vertex smallXVertex(){
        return ((startVertex.x < endVertex.x)?startVertex: endVertex);
    }
    private Vertex getTheOtherSide(Vertex oneSide){
        return ((oneSide == startVertex)?endVertex: startVertex);
    }

}