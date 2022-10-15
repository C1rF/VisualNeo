package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.VisualNeoController;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.StringUtil;

import java.util.Arrays;

public class Edge extends GraphElement {

    private static final double GAP_ANGLE = Math.PI / 10;
    private static final double LINE_LENGTH = VERTEX_RADIUS + 40.0;

    public Vertex startVertex;
    public Vertex endVertex;
    public boolean directed;
    private final Path curve = new Path();
    private int edgeIdx;
    private int numEdges;

    private boolean selfLoop;

    public Edge(Vertex startVertex, Vertex endVertex, boolean directed) {
        super();
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        this.directed = directed;
        selfLoop = (startVertex == endVertex);
        // Notify two vertices to attach it
        attach();
        // Initialize the shape
        initializeShape();
        // Give them event handler
        mouseEventHandler handler = new mouseEventHandler();
        addEventHandler(MouseEvent.ANY, handler);
        // Add curve and label to the Edge Group (Display)
        this.getChildren().addAll(curve, label_displayed);
        label_displayed.toBack();
        curve.toBack();
        // For Testing
        System.out.println("An Edge from (" + startVertex.x + " , " + startVertex.y + ") to " +
                "(" + endVertex.x + " , " + endVertex.y + ")" + " is created!");
    }

    @Override
    protected void initializeShape() {
        curve.setStroke(new Color(0, 0, 0, 0.4));
//        edge.setStrokeType(StrokeType.CENTERED);
        curve.setFill(null);
    }

    @Override
    public void becomeHighlight() {
        curve.setStrokeWidth(6);
        curve.setStroke(new Color(0, 0, 0, 0.7));
    }

    @Override
    public void removeHighlight() {
        curve.setStrokeWidth(5);
        curve.setStroke(new Color(0, 0, 0, 0.4));
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

    public void update() {
        curve.getElements().clear();
        
        if (startVertex.equals(endVertex)) {
            double cX = startVertex.x;
            double cY = startVertex.y;
            // TODO: Modify this
            double baseAngle = Math.PI / 2;
            
            double offsetAngle = (2 * edgeIdx - numEdges + 0.5) * GAP_ANGLE;

            double l1A = baseAngle + offsetAngle;
            double l1Cos = Math.cos(l1A);
            double l1Sin = Math.sin(l1A);
            double l1SX = cX + VERTEX_RADIUS * l1Cos;
            double l1SY = cY + VERTEX_RADIUS * l1Sin;
            double l1EX = cX + LINE_LENGTH * l1Cos;
            double l1EY = cY + LINE_LENGTH * l1Sin;

            double l2A = baseAngle + offsetAngle + GAP_ANGLE;
            double l2Cos = Math.cos(l2A);
            double l2Sin = Math.sin(l2A);
            double l2SX = cX + LINE_LENGTH * l2Cos;
            double l2SY = cY + LINE_LENGTH * l2Sin;
            double l2EX = cX + VERTEX_RADIUS * l2Cos;
            double l2EY = cY + VERTEX_RADIUS * l2Sin;

            double r = LINE_LENGTH * Math.tan(GAP_ANGLE / 2);

            curve.getElements().addAll(
                    new MoveTo(l1SX, l1SY),
                    new LineTo(l1EX, l1EY),
                    new ArcTo(r, r, 0.0, l2SX, l2SY, true, true),
                    new LineTo(l2EX, l2EY));
        }
        else {
            double sX, sY, eX, eY;
            if (startVertex.x < endVertex.x || startVertex.x == endVertex.x && startVertex.y <= endVertex.y) {
                sX = startVertex.x;
                sY = startVertex.y;
                eX = endVertex.x;
                eY = endVertex.y;
            }
            else {
                sX = endVertex.x;
                sY = endVertex.y;
                eX = startVertex.x;
                eY = startVertex.y;
            }
            double baseAngle = sX == eX ? Math.PI / 2 : Math.atan2(eY - sY, eX - sX);

            double offsetAngle = (edgeIdx - (numEdges - 1) / 2.0) * GAP_ANGLE;

            double aSA = baseAngle + offsetAngle;
            double aSCos = Math.cos(aSA);
            double aSSin = Math.sin(aSA);
            double aSX = sX + VERTEX_RADIUS * aSCos;
            double aSY = sY + VERTEX_RADIUS * aSSin;

            double aEA = baseAngle - offsetAngle + Math.PI;
            double aECos = Math.cos(aEA);
            double aESin = Math.sin(aEA);
            double aEX = eX + VERTEX_RADIUS * aECos;
            double aEY = eY + VERTEX_RADIUS * aESin;

            double d = Math.sqrt(Math.pow(eY - sY, 2) + Math.pow(eX - sX, 2));
            double r = offsetAngle == 0.0 ?
                       Double.POSITIVE_INFINITY :
                       (d / 2 - VERTEX_RADIUS * Math.cos(offsetAngle)) / Math.sin(offsetAngle);

            curve.getElements().addAll(
                    new MoveTo(aSX, aSY),
                    ((Double) r).equals(Double.POSITIVE_INFINITY) ?
                    new LineTo(aEX, aEY) :
                    new ArcTo(r, r, 0.0, aEX, aEY, false, edgeIdx < numEdges / 2));
        }
    }

    /**
     * Given the num_curves and curve_index, determine the offset of that edge
     *
     * @param edgeIdx the index of this edge among all edges between startVertex and endVertex
     * @param numEdges number of edges between startVertex and endVertex (including itself)
     */
    public void updateIdx(int edgeIdx, int numEdges) {
        this.edgeIdx = edgeIdx;
        this.numEdges = numEdges;
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
        startVertex.updateEdgesBetween(endVertex);
    }

    @Override
    public void eraseFrom(VisualNeoController controller) {
        startVertex.detach(this);
        endVertex.detach(this);
        startVertex.updateEdgesBetween(endVertex);
        ((Pane) getParent()).getChildren().remove(this);
        controller.listOfEdges.remove(this);
    }
    
    public Vertex other(Vertex vertex) {
        if (vertex == startVertex)
            return endVertex;

        if (vertex == endVertex)
            return startVertex;

        return null;
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