package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.VisualNeoController;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.StringUtil;

import java.lang.Math;
import java.util.Arrays;

public class Edge extends GraphElement {

    public Vertex startVertex;
    public Vertex endVertex;
    public boolean directed;
    private QuadCurve edge;
    private CubicCurve self_edge;
    private double offsetIndex;

    private boolean selfLoop;

    public Edge(Pane DrawBoard, Vertex startVertex, Vertex endVertex, boolean directed){

        super();
        this.DrawBoard = DrawBoard;
        // Set the position
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        this.directed = directed;
        selfLoop = (startVertex == endVertex);
        offsetIndex = 0;


        // Set the line
        edge = new QuadCurve();
        edge.setStroke(Color.BLACK);
        edge.setStroke(new Color(0,0,0,0.4));
        edge.setStrokeType(StrokeType.CENTERED);
        edge.setFill(null);

        // Set focus traversable
        edge.setFocusTraversable(true);

        // Set the curve position
        setCurve();

        // Display the line and label on the StackPane
        DrawBoard.getChildren().addAll(edge, label_displayed);
        label_displayed.toBack();
        edge.toBack();

        // Give them event handler
        edge.addEventHandler( MouseEvent.MOUSE_PRESSED, e->{if(canSelect) { pressed(e);}  } );
        edge.addEventHandler( MouseEvent.MOUSE_ENTERED, e->{if(canSelect) { mouseEntered(e);}  } );
        edge.addEventHandler( MouseEvent.MOUSE_EXITED, e->{if(canSelect) { mouseExited(e);}  } );

        // For Testing
        System.out.println("An Edge from (" + startVertex.x + " , " + startVertex.y + ") to " +
                "(" + endVertex.x + " , " + endVertex.y + ")" + " is created!");
    }

    @Override
    public void requestFocus(){
        edge.requestFocus();
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

    @Override
    public void eraseShapes(){
        DrawBoard.getChildren().remove(edge);
        DrawBoard.getChildren().remove(label_displayed);
    }
    @Override
    public javafx.scene.Node getShape(){
        if(selfLoop)
            return edge;
        else
            return self_edge;
    }

    /**
     *  Request focus when pressed
     */
    @Override
    protected void pressed(MouseEvent m) {
        edge.requestFocus();
        if(VisualNeoController.getStatus() == VisualNeoController.Status.SELECT){
            m.consume();
        }
    }

    /**
     * Set the position of the line
     */
    public void setCurve(){
        // If this edge is self-connected
        if(selfLoop){
            double x = startVertex.x;
            double y = startVertex.y;
            edge.setStartX(x-0.75*VERTEX_RADIUS);
            edge.setStartY(y);
            edge.setControlX(x);
            edge.setControlY(y-VERTEX_RADIUS*(4+offsetIndex));
            edge.setEndX(x+0.75*VERTEX_RADIUS);
            edge.setEndY(y);
            // Set the label at the same time
            label_displayed.setLayoutX(x);
            label_displayed.setLayoutY(y+VERTEX_RADIUS*offsetIndex);
            return;
        }

        double start_x = startVertex.x;
        double start_y = startVertex.y;
        double end_x = endVertex.x;
        double end_y = endVertex.y;
        // Now we get the four values
        // Compute the control point's coordinate
        double L = Math.sqrt( Math.pow(end_y-start_y,2)+ Math.pow(end_x-start_x,2) );
        double slope = -(end_x-start_x)/(end_y-start_y);
        double sin_val = slope/Math.sqrt(1+slope*slope);
        double cos_val = 1/Math.sqrt(1+slope*slope);
        double control_pt_x = (start_x+end_x)/2-L*offsetIndex*cos_val;
        double control_pt_y = (start_y+end_y)/2-L*offsetIndex*sin_val;
        // Set all the attributes for the edge
        edge.setStartX(start_x);
        edge.setStartY(start_y);
        edge.setControlX(control_pt_x);
        edge.setControlY(control_pt_y);
        edge.setEndX(end_x);
        edge.setEndY(end_y);

        // Set the label at the same time
        label_displayed.setLayoutX(control_pt_x-0.2*VERTEX_RADIUS);
        label_displayed.setLayoutY(control_pt_y);
    }

    /**
     * Given the num_curves and curve_index, determine the offset of that edge
     * @param num_curves number of edges between startVertex and endVertex (including itself)
     * @param curve_index the index of this edge among all edges between startVertex and endVertex
     */
    public void updateOffset(int num_curves, int curve_index){
        if(!selfLoop)
            offsetIndex = 0.14*curve_index-0.07*(num_curves-1);
        else
            offsetIndex = 2*curve_index;
    }

    public boolean containEdge(QuadCurve edge_to_compare){
        return (edge_to_compare == edge);
    }

    @Override
    public String toText(){
        String[] temp = new String[] {String.valueOf(1),
                                      String.valueOf(2),
                                      label_displayed.getText()};
        return (String) StringUtil.join(" ", Arrays.asList(temp));
    }

}