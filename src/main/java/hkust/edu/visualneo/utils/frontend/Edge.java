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

import static hkust.edu.visualneo.utils.frontend.Vertex.VERTEX_RADIUS;
import static java.lang.Math.PI;

public class Edge extends GraphElement {

    private static final double GAP_ANGLE = PI / 12;
    private static final double LOOP_SPAN_ANGLE = PI / 6;
    private static final double LOOP_GAP_ANGLE = PI / 18;
    private static final double ARROWHEAD_ANGLE = PI / 3;
    private static final double LINE_LENGTH = VERTEX_RADIUS + 25.0;
    private static final double ARROWHEAD_LENGTH = 7.5;
    private static final double TEXT_GAP = 10.0;
    private static final double TEXT_EPSILON = 4.0;

    enum TextDisplayMode {TOP, MIDDLE, BOTTOM}

    private TextDisplayMode textDisplayMode = TextDisplayMode.MIDDLE;

    public Vertex startVertex;
    public Vertex endVertex;
    public boolean directed;
    private final Path curve = new Path();
    private int edgeIdx;
    private int numEdges;

    public Edge(VisualNeoController controller, Vertex startVertex, Vertex endVertex, boolean directed) {
        super(controller);
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

        if (startVertex.equals(endVertex)) {
            double sCos = Math.cos(LOOP_SPAN_ANGLE / 2);
            double sSin = Math.sin(LOOP_SPAN_ANGLE / 2);
            double lNX = VERTEX_RADIUS * sCos;
            double lNY = VERTEX_RADIUS * sSin;
            double lFX = LINE_LENGTH * sCos;
            double lFY = LINE_LENGTH * sSin;

            double r = LINE_LENGTH * Math.tan(Edge.LOOP_SPAN_ANGLE / 2);

            curve.getElements().addAll(
                    new MoveTo(lNX, lNY),
                    new LineTo(lFX, lFY),
                    new ArcTo(r, r, 0.0, lFX, -lFY, true, false),
                    new LineTo(lNX, -lNY));

            // TODO: Revert this
            if (!directed) {
                double h1Cos = Math.cos((-LOOP_SPAN_ANGLE + ARROWHEAD_ANGLE) / 2);
                double h1Sin = Math.sin((-LOOP_SPAN_ANGLE + ARROWHEAD_ANGLE) / 2);
                double h1X = lNX + ARROWHEAD_LENGTH * h1Cos;
                double h1Y = -lNY + ARROWHEAD_LENGTH * h1Sin;
                double h2Cos = Math.cos((-LOOP_SPAN_ANGLE - ARROWHEAD_ANGLE) / 2);
                double h2Sin = Math.sin((-LOOP_SPAN_ANGLE - ARROWHEAD_ANGLE) / 2);
                double h2X = lNX + ARROWHEAD_LENGTH * h2Cos;
                double h2Y = -lNY + ARROWHEAD_LENGTH * h2Sin;

                curve.getElements().addAll(
                        new LineTo(h1X, h1Y),
                        new MoveTo(lNX, -lNY),
                        new LineTo(h2X, h2Y));
            }

            label_displayed.setLayoutX(LINE_LENGTH / Math.cos(LOOP_SPAN_ANGLE / 2) + r + TEXT_GAP);
            label_displayed.setRotate(90.0);
        }
    }

    @Override
    public void becomeHighlight() {
        curve.setStrokeWidth(3.0);
        curve.setStroke(new Color(0, 0, 0, 0.7));
    }

    @Override
    public void removeHighlight() {
        curve.setStrokeWidth(2.0);
        curve.setStroke(new Color(0, 0, 0, 0.4));
    }

    /**
     * Request focus when pressed
     */
    @Override
    protected void pressed(MouseEvent m) {
        if (m.isShiftDown()) return;
        System.out.println("Edge Pressed");
        requestFocus();
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
        } else {
            curve.getElements().clear();

            double sX, sY, eX, eY;
            boolean reverted = System.identityHashCode(startVertex) > System.identityHashCode(endVertex);
            if (reverted) {
                sX = endVertex.x;
                sY = endVertex.y;
                eX = startVertex.x;
                eY = startVertex.y;
            }
            else {
                sX = startVertex.x;
                sY = startVertex.y;
                eX = endVertex.x;
                eY = endVertex.y;
            }

            setLayoutX((sX + eX) / 2);
            setLayoutY((sY + eY) / 2);

            double baseAngle = sX == eX ? sY < eY ? PI / 2 : -PI / 2 : Math.atan2(eY - sY, eX - sX);
            ((Rotate) getTransforms().get(0)).setAngle(Math.toDegrees(baseAngle));

            double d = Math.sqrt(Math.pow(eY - sY, 2) + Math.pow(eX - sX, 2));

            double offsetAngle;
            double aX, aY;

            if (2 * edgeIdx + 1 == numEdges) {
                offsetAngle = 0.0;

                aX = d / 2 - VERTEX_RADIUS;
                aY = 0.0;

                switch (textDisplayMode) {
                    case TOP -> {
                        curve.getElements().addAll(
                                new MoveTo(-aX, 0.0),
                                new LineTo(aX, 0.0));

                        label_displayed.setLayoutY(Math.cos(baseAngle) < 0 ? TEXT_GAP : -TEXT_GAP);
                    }
                    case MIDDLE -> {
                        if (label_displayed.getText() == null || label_displayed.getText().equals(""))
                            curve.getElements().addAll(
                                    new MoveTo(-aX, 0.0),
                                    new LineTo(aX, 0.0));
                        else {
                            double tX = label_displayed.getLayoutBounds().getWidth() / 2 + TEXT_EPSILON;
                            if (tX > d / 2)
                                tX = d / 2;

                            curve.getElements().addAll(
                                    new MoveTo(-aX, 0.0),
                                    new LineTo(-tX, 0.0),
                                    new MoveTo(tX, 0.0),
                                    new LineTo(aX, 0.0));

                            label_displayed.setLayoutY(0.0);
                        }
                    }
                    case BOTTOM -> {
                        curve.getElements().addAll(
                                new MoveTo(-aX, 0.0),
                                new LineTo(aX, 0.0));

                        label_displayed.setLayoutY(Math.cos(baseAngle) < 0 ? -TEXT_GAP : TEXT_GAP);
                    }
                }
            } else {
                boolean lowerHalf = edgeIdx < numEdges / 2;
                offsetAngle = (edgeIdx - (numEdges - 1) / 2.0) * GAP_ANGLE;

                double cos = Math.cos(offsetAngle);
                double sin = Math.sin(offsetAngle);
                aX = d / 2 - VERTEX_RADIUS * cos;
                aY = VERTEX_RADIUS * sin;

                double r = aX / Math.abs(sin);
                double fY = r - Math.sqrt(Math.pow(r, 2) + Math.pow(VERTEX_RADIUS, 2) - Math.pow(d / 2, 2));

                switch (textDisplayMode) {
                    case TOP -> {
                        curve.getElements().addAll(
                                new MoveTo(-aX, aY),
                                new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));

                        label_displayed.setLayoutY((lowerHalf ? -fY : fY) +
                                (Math.cos(baseAngle) < 0 ? TEXT_GAP : -TEXT_GAP));
                    }
                    case MIDDLE -> {
                        if (label_displayed.getText() == null || label_displayed.getText().equals(""))
                            curve.getElements().addAll(
                                    new MoveTo(-aX, aY),
                                    new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));
                        else {
                            double tX = label_displayed.getLayoutBounds().getWidth() / 2 + TEXT_EPSILON;
                            if (tX > d / 2)
                                tX = d / 2;
                            double offY = r - Math.sqrt(Math.pow(r, 2) - Math.pow(tX, 2));
                            double tY = lowerHalf ? -fY + offY : fY - offY;

                            curve.getElements().addAll(
                                    new MoveTo(-aX, aY),
                                    new ArcTo(r, r, 0.0, -tX, tY, cos < 0.0, lowerHalf),
                                    new MoveTo(tX, tY),
                                    new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));

                            label_displayed.setLayoutY(tY);
                        }
                    }
                    case BOTTOM -> {
                        curve.getElements().addAll(
                                new MoveTo(-aX, aY),
                                new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));

                        label_displayed.setLayoutY((lowerHalf ? -fY : fY) +
                                (Math.cos(baseAngle) < 0 ? -TEXT_GAP : TEXT_GAP));
                    }
                }
            }

            // TODO: Revert this
            if (!directed) {
                double h1Cos = Math.cos(offsetAngle + ARROWHEAD_ANGLE / 2);
                double h1Sin = Math.sin(offsetAngle + ARROWHEAD_ANGLE / 2);
                double h1X = aX - ARROWHEAD_LENGTH * h1Cos;
                double h1Y = aY + ARROWHEAD_LENGTH * h1Sin;
                double h2Cos = Math.cos(offsetAngle - ARROWHEAD_ANGLE / 2);
                double h2Sin = Math.sin(offsetAngle - ARROWHEAD_ANGLE / 2);
                double h2X = aX - ARROWHEAD_LENGTH * h2Cos;
                double h2Y = aY + ARROWHEAD_LENGTH * h2Sin;

                if (reverted)
                    curve.getElements().addAll(
                            new MoveTo(-aX, aY),
                            new LineTo(-h1X, h1Y),
                            new MoveTo(-aX, aY),
                            new LineTo(-h2X, h2Y));
                else
                    curve.getElements().addAll(
                            new LineTo(h1X, h1Y),
                            new MoveTo(aX, aY),
                            new LineTo(h2X, h2Y));
            }

            label_displayed.setRotate(Math.cos(baseAngle) > 0.0 ? 0.0 : 180.0);
        }
    }

    @Override
    public void addLabel(String new_label) {
        super.addLabel(new_label);
        update();
    }

    /**
     * Given the num_curves and curve_index, determine the offset of that edge
     *
     * @param edgeIdx  the index of this edge among all edges between startVertex and endVertex
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
            event.consume();
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
                pressed(event);
            else if (event.getEventType() == MouseEvent.MOUSE_ENTERED)
                getScene().setCursor(Cursor.HAND);
            else if (event.getEventType() == MouseEvent.MOUSE_EXITED)
                getScene().setCursor(Cursor.DEFAULT);
        }
    }

    private void attach() {
        startVertex.attach(this);
        endVertex.attach(this);
    }

    @Override
    public void erase() {
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