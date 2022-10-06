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
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.StringUtil;

import java.lang.Math;
import java.util.Arrays;

public class Edge extends GraphElement {

    public Vertex startVertex;
    public Vertex endVertex;
    public boolean directed;
    public Line edge;

    public Edge(Vertex startVertex, Vertex endVertex, boolean directed){

        super();
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        this.directed = directed;
        // Set the position (of the StackPane)
        setPos();

        // Set the line
        edge = new Line();
        edge.setStroke(Color.BLACK);
        edge.setStrokeWidth(5);
        edge.setStrokeType(StrokeType.CENTERED);
        setLine();

        // Display the line and label on the StackPane
        getChildren().addAll(edge, label_displayed);

        // Give them event handler
        edge.addEventHandler( MouseEvent.MOUSE_PRESSED, e->{if(canSelect) { pressed(e);}  } );
        edge.addEventHandler( MouseEvent.MOUSE_ENTERED, e->{if(canSelect) { mouseEntered(e);}  } );
        edge.addEventHandler( MouseEvent.MOUSE_EXITED, e->{if(canSelect) { mouseExited(e);}  } );

        // For Testing
        System.out.println("An Edge from (" + startVertex.x + " , " + startVertex.y + ") to " +
                "(" + endVertex.x + " , " + endVertex.y + ")" + " is created!");
    }

    @Override
    public void becomeFocused(){
        edge.setStrokeWidth(6);
        edge.setStroke(new Color(0,0,0,0.7));
    }

    @Override
    public void removeFocused(){
        edge.setStrokeWidth(5);
        edge.setStroke(new Color(0,0,0,0.4));
    }

    /**
     *  Request focus when pressed
     */
    @Override
    protected void pressed(MouseEvent m) {
        requestFocus();
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

    @Override
    public String toText(){
        String[] temp = new String[] {String.valueOf(startVertex.getId()),
                                      String.valueOf(endVertex.getId()),
                                      label_displayed.getText()};
        return (String) StringUtil.join(" ", Arrays.asList(temp));
    }

}