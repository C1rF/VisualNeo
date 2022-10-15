package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.VisualNeoController;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.QuadCurve;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.StringUtil;

import java.util.Arrays;

public class Edge extends GraphElement {

    public Vertex startVertex;
    public Vertex endVertex;
    public boolean directed;
    private QuadCurve edge;
    private CubicCurve self_edge;
    private double offsetIndex;

    private boolean selfLoop;

    public Edge(Vertex startVertex, Vertex endVertex, boolean directed) {
        super();
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        this.directed = directed;
        selfLoop = (startVertex == endVertex);
        offsetIndex = 0;
        // Notify two vertices to attach it
        attach();
        // Initialize the shape
        initializeShape();
        setCurve();
        // Give them event handler
        mouseEventHandler handler = new mouseEventHandler();
        addEventHandler(MouseEvent.ANY, handler);
        // Add curve and label to the Edge Group (Display)
        this.getChildren().addAll(edge, label_displayed);
        label_displayed.toBack();
        edge.toBack();
        // For Testing
        System.out.println("An Edge from (" + startVertex.x + " , " + startVertex.y + ") to " +
                "(" + endVertex.x + " , " + endVertex.y + ")" + " is created!");
    }

    @Override
    protected void initializeShape() {
        edge = new QuadCurve();
        edge.setStroke(new Color(0, 0, 0, 0.4));
        //edge.setStrokeType(StrokeType.CENTERED);
        edge.setFill(null);
    }

    @Override
    public void becomeHighlight() {
        edge.setStrokeWidth(6);
        edge.setStroke(new Color(0, 0, 0, 0.7));
    }

    @Override
    public void removeHighlight() {
        edge.setStrokeWidth(5);
        edge.setStroke(new Color(0, 0, 0, 0.4));
    }

    /**
     * Request focus when pressed
     */
    @Override
    protected void pressed(MouseEvent m) {
        this.requestFocus();
        if (VisualNeoController.getStatus() == VisualNeoController.Status.SELECT) m.consume();
    }

    @Override
    protected void mouseEntered(MouseEvent m) {
        if (VisualNeoController.getStatus() == VisualNeoController.Status.SELECT)
            getScene().setCursor(Cursor.HAND);
        else if (VisualNeoController.getStatus() == VisualNeoController.Status.ERASE)
            getScene().setCursor(Cursor.DISAPPEAR);
    }

    /**
     * Set the position of the line
     */
    public void setCurve() {
        // If this edge is self-connected
        if (selfLoop) {
            double x = startVertex.x;
            double y = startVertex.y;
            edge.setStartX(x - 0.75 * VERTEX_RADIUS);
            edge.setStartY(y);
            edge.setControlX(x);
            edge.setControlY(y - VERTEX_RADIUS * (4 + offsetIndex));
            edge.setEndX(x + 0.75 * VERTEX_RADIUS);
            edge.setEndY(y);
            // Set the label at the same time
            label_displayed.setLayoutX(x);
            label_displayed.setLayoutY(y + VERTEX_RADIUS * offsetIndex);
            return;
        }

        double start_x = startVertex.x;
        double start_y = startVertex.y;
        double end_x = endVertex.x;
        double end_y = endVertex.y;
        // Now we get the four values
        // Compute the control point's coordinate
        double L = Math.sqrt(Math.pow(end_y - start_y, 2) + Math.pow(end_x - start_x, 2));
        double slope = -(end_x - start_x) / (end_y - start_y);
        double sin_val = slope / Math.sqrt(1 + slope * slope);
        double cos_val = 1 / Math.sqrt(1 + slope * slope);
        double control_pt_x = (start_x + end_x) / 2 - L * offsetIndex * cos_val;
        double control_pt_y = (start_y + end_y) / 2 - L * offsetIndex * sin_val;
        // Set all the attributes for the edge
        edge.setStartX(start_x);
        edge.setStartY(start_y);
        edge.setControlX(control_pt_x);
        edge.setControlY(control_pt_y);
        edge.setEndX(end_x);
        edge.setEndY(end_y);

        // Set the label at the same time
        label_displayed.setLayoutX(control_pt_x - 0.2 * VERTEX_RADIUS);
        label_displayed.setLayoutY(control_pt_y);
    }

    /**
     * Given the num_curves and curve_index, determine the offset of that edge
     *
     * @param num_curves  number of edges between startVertex and endVertex (including itself)
     * @param curve_index the index of this edge among all edges between startVertex and endVertex
     */
    public void updateOffset(int num_curves, int curve_index) {
        if (!selfLoop)
            offsetIndex = 0.14 * curve_index - 0.07 * (num_curves - 1);
        else
            offsetIndex = 2 * curve_index;
    }

    /**
     * Event handler to handle all the MouseEvents
     */
    public class mouseEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            // If the element cannot be selected, do nothing
            if (!canSelect) return;
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
                pressed(event);
            else if (event.getEventType() == MouseEvent.MOUSE_ENTERED)
                mouseEntered(event);
            else if (event.getEventType() == MouseEvent.MOUSE_EXITED)
                mouseExited(event);
        }
    }

    private void attach() {
        startVertex.attach(this);
        endVertex.attach(this);
    }

    @Override
    public void eraseFrom(VisualNeoController controller) {
        startVertex.detach(this);
        endVertex.detach(this);
        ((Pane) getParent()).getChildren().remove(this);
        controller.listOfEdges.remove(this);
    }

    /**
     * Convert the Edge Object to a String that contains all the information
     */
    @Override
    public String toText() {
        String[] temp = new String[]{String.valueOf(1),
                String.valueOf(2),
                label_displayed.getText()};
        return (String) StringUtil.join(" ", Arrays.asList(temp));
    }

}