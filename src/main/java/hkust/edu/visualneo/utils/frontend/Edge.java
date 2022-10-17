package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.VisualNeoController;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.transform.Rotate;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.StringUtil;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.PI;

public class Edge extends GraphElement {

    private static final double GAP_ANGLE = PI / 12;
    private static final double LOOP_SPAN_ANGLE = PI / 6;
    private static final double LOOP_GAP_ANGLE = PI / 18;
    private static final double LINE_LENGTH = VERTEX_RADIUS + 40.0;
    private static final double TEXT_GAP = 10.0;

    public Vertex startVertex;
    public Vertex endVertex;
    public boolean directed;
    private final Path curve = new Path();
    private int edgeIdx;
    private int numEdges;

    public Edge(Vertex startVertex, Vertex endVertex, boolean directed) {
        super();
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        this.directed = directed;
        // Give them event handler
        mouseEventHandler handler = new mouseEventHandler();
        addEventHandler(MouseEvent.ANY, handler);
        // Initialize the shape
        initializeShape();
        // Notify two vertices to attach it
        attach();
        // For Testing
        System.out.println("An Edge from (" + startVertex.x + " , " + startVertex.y + ") to " +
                "(" + endVertex.x + " , " + endVertex.y + ")" + " is created!");
    }

    @Override
    protected void initializeShape() {
        super.initializeShape();
        // Add curve and label to the Edge Group (Display)
        getChildren().add(curve);
        label_displayed.toBack();
        curve.toBack();

        getTransforms().add(new Rotate(0.0, 0.0, 0.0));
        label_displayed.getTransforms().add(new Rotate(0.0, 0.0, 0.0));

        curve.setStroke(new Color(0, 0, 0, 0.4));
        curve.setFill(null);

        if (startVertex.equals(endVertex)) {
            double cos = Math.cos(LOOP_SPAN_ANGLE / 2);
            double sin = Math.sin(LOOP_SPAN_ANGLE / 2);
            double l1SX = VERTEX_RADIUS * cos;
            double l1SY = -VERTEX_RADIUS * sin;
            double l1EX = LINE_LENGTH * cos;
            double l1EY = -LINE_LENGTH * sin;
            double l2SX = LINE_LENGTH * cos;
            double l2SY = LINE_LENGTH * sin;
            double l2EX = VERTEX_RADIUS * cos;
            double l2EY = VERTEX_RADIUS * sin;

            double r = LINE_LENGTH * Math.tan(Edge.LOOP_SPAN_ANGLE / 2);

            curve.getElements().addAll(
                    new MoveTo(l1SX, l1SY),
                    new LineTo(l1EX, l1EY),
                    new ArcTo(r, r, 0.0, l2SX, l2SY, true, true),
                    new LineTo(l2EX, l2EY));

            label_displayed.setLayoutX(LINE_LENGTH / Math.cos(LOOP_SPAN_ANGLE / 2) + VERTEX_RADIUS + TEXT_GAP);
            label_displayed.setRotate(90.0);
        }
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
        if (startVertex.equals(endVertex)) {
            double cX = startVertex.x;
            double cY = startVertex.y;

            setLayoutX(cX);
            setLayoutY(cY);

            List<Double> angles = startVertex.computeAngles();
            double baseAngle = -PI / 2;
            if (!angles.isEmpty()) {
                angles.add(angles.get(0) + 2 * PI);
                double maxSpan = Double.NEGATIVE_INFINITY;
                for (int i = 1; i < angles.size(); ++i) {
                    double span = angles.get(i) - angles.get(i - 1);
                    if (span > maxSpan) {
                        maxSpan = span;
                        baseAngle = (angles.get(i) + angles.get(i - 1)) / 2;
                    }
                }
            }

            double offsetAngle = ((2 * edgeIdx - numEdges + 1) * (LOOP_GAP_ANGLE + LOOP_SPAN_ANGLE)) / 2;
            ((Rotate) getTransforms().get(0)).setAngle(Math.toDegrees(baseAngle + offsetAngle));

            label_displayed.setRotate(Math.sin(baseAngle + offsetAngle) < 0.0 ? 90.0 : -90.0);
        }
        else {
            curve.getElements().clear();

            double sX, sY, eX, eY;
            if (System.identityHashCode(startVertex) < System.identityHashCode(endVertex)) {
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

            setLayoutX(sX);
            setLayoutY(sY);

            double baseAngle = sX == eX ? sY < eY ? PI / 2 : -PI / 2 : Math.atan2(eY - sY, eX - sX);

            ((Rotate) getTransforms().get(0)).setAngle(Math.toDegrees(baseAngle));

            double d = Math.sqrt(Math.pow(eY - sY, 2) + Math.pow(eX - sX, 2));

            if (2 * edgeIdx + 1 == numEdges) {
                curve.getElements().addAll(
                        new MoveTo(VERTEX_RADIUS, 0.0),
                        new LineTo(d - VERTEX_RADIUS, 0.0));

                label_displayed.setLayoutY(Math.cos(baseAngle) < 0 ? TEXT_GAP : -TEXT_GAP);
            }
            else {
                double offsetAngle = (edgeIdx - (numEdges - 1) / 2.0) * GAP_ANGLE;

                double cos = Math.cos(offsetAngle);
                double sin = Math.sin(offsetAngle);

                double aSX = VERTEX_RADIUS * cos;
                double aSY = VERTEX_RADIUS * sin;

                double aEX = d - VERTEX_RADIUS * cos;
                double aEY = VERTEX_RADIUS * sin;

                double r = (d / 2 - VERTEX_RADIUS * cos) / Math.abs(sin);
                double f = r - Math.sqrt(Math.pow(r, 2) + Math.pow(VERTEX_RADIUS, 2) - Math.pow(d / 2, 2));

                curve.getElements().addAll(
                        new MoveTo(aSX, aSY),
                        new ArcTo(r, r, 0.0, aEX, aEY, cos < 0.0, edgeIdx < numEdges / 2));

                label_displayed.setLayoutY((edgeIdx < numEdges / 2 ? -f : f) +
                                           (Math.cos(baseAngle) < 0 ? TEXT_GAP : -TEXT_GAP));
            }

            label_displayed.setLayoutX(d / 2);
            label_displayed.setRotate(Math.cos(baseAngle) > 0.0 ? 0.0 : 180.0);
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
    }

    @Override
    public void eraseFrom(VisualNeoController controller) {
        startVertex.detach(this);
        endVertex.detach(this);
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