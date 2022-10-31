package hkust.edu.visualneo.utils.frontend;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.StringUtil;

import java.util.Arrays;

import static hkust.edu.visualneo.utils.frontend.Vertex.VERTEX_RADIUS;
import static java.lang.Math.PI;

public class Edge extends GraphElement {

    private static final Point2D X_AXIS = new Point2D(1.0, 0.0);

    private static final double GAP_ANGLE = PI / 12;
    private static final double LOOP_SPAN_ANGLE = PI / 6;
    private static final double LOOP_GAP_ANGLE = PI / 18;
    private static final double ARROWHEAD_ANGLE = PI / 3;
    private static final double LINE_LENGTH = VERTEX_RADIUS + 25.0;
    private static final double ARROWHEAD_LENGTH = 8.0;
    private static final double TEXT_GAP = 10.0;
    private static final double TEXT_EPSILON = 4.0;
    private static final double DEFAULT_STROKE_WIDTH = 2.0;
    private static final double HIGHLIGHT_STROKE_WIDTH = 3.0;
    private static final Color DEFAULT_COLOR = new Color(0.0, 0.0, 0.0, 0.4);
    private static final Color HIGHLIGHT_COLOR = new Color(0.0, 0.0, 0.0, 0.7);

    enum TextDisplayMode {TOP, MIDDLE, BOTTOM}

    private TextDisplayMode textDisplayMode = TextDisplayMode.MIDDLE;

    public final Vertex startVertex;
    public final Vertex endVertex;
    public final boolean directed;
    private final IntegerProperty idx =
            new SimpleIntegerProperty(this, "idx", -1);
    private final IntegerProperty num =
            new SimpleIntegerProperty(this, "num", 0);

    public Edge(Canvas canvas, Vertex startVertex, Vertex endVertex, boolean directed) {
        super(canvas);
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        this.directed = directed;

        initializeGraphics();
        // Notify two vertices to attach it
        attach();
        
        // Give them event handler
//        mouseEventHandler handler = new mouseEventHandler();
//        addEventHandler(MouseEvent.ANY, handler);
        // For Testing
        System.out.println("An Edge from (" + startVertex.getX() + " , " + startVertex.getY() + ") to " +
                "(" + endVertex.getX() + " , " + endVertex.getY() + ")" + " is created!");
    }

    protected void initializeShapeAndText_() {
//        initializeShape();
//        initializeText();

//        getTransforms().add(new Rotate(0.0, 0.0, 0.0));

        if (isSelfLoop()) {
            double sCos = Math.cos(LOOP_SPAN_ANGLE / 2);
            double sSin = Math.sin(LOOP_SPAN_ANGLE / 2);
            double lNX = VERTEX_RADIUS * sCos;
            double lNY = VERTEX_RADIUS * sSin;
            double lFX = LINE_LENGTH * sCos;
            double lFY = LINE_LENGTH * sSin;

            double r = LINE_LENGTH * Math.tan(Edge.LOOP_SPAN_ANGLE / 2);

            curve().getElements().addAll(
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

                curve().getElements().addAll(
                        new LineTo(h1X, h1Y),
                        new MoveTo(lNX, -lNY),
                        new LineTo(h2X, h2Y));
            }

            text.setLayoutX(LINE_LENGTH / Math.cos(LOOP_SPAN_ANGLE / 2) + r + TEXT_GAP);
            text.setRotate(90.0);

//            xProperty.bind(startVertex.xProperty);
//            yProperty.bind(startVertex.yProperty);
        }
    }

    @Override
    protected void initializeGraphics() {
        super.initializeGraphics();

        if (isSelfLoop()) {
            positionProperty().bind(startVertex.positionProperty());

            double r = LINE_LENGTH * Math.tan(Edge.LOOP_SPAN_ANGLE / 2);

            text.setLayoutX(LINE_LENGTH / Math.cos(LOOP_SPAN_ANGLE / 2) + r + TEXT_GAP);
            text.setRotate(90.0);

            shape = new Path();
        }
        else {

        }

        getChildren().add(shape);

        highlightProperty().addListener((observable, oldValue, newValue) -> {
            shape.setStrokeWidth(newValue ? HIGHLIGHT_STROKE_WIDTH : DEFAULT_STROKE_WIDTH);
            shape.setStroke(newValue ? HIGHLIGHT_COLOR : DEFAULT_COLOR);
        });

        if (isSelfLoop()) {
//            double sCos = Math.cos(LOOP_SPAN_ANGLE / 2);
//            double sSin = Math.sin(LOOP_SPAN_ANGLE / 2);
//            double lNX = VERTEX_RADIUS * sCos;
//            double lNY = VERTEX_RADIUS * sSin;
//            double lFX = LINE_LENGTH * sCos;
//            double lFY = LINE_LENGTH * sSin;
//
//            curve().getElements().addAll(
//                    new MoveTo(lNX, lNY),
//                    new LineTo(lFX, lFY),
//                    new ArcTo(r, r, 0.0, lFX, -lFY, true, false),
//                    new LineTo(lNX, -lNY));
//
//            // TODO: Revert this
//            if (!directed) {
//                double h1Cos = Math.cos((-LOOP_SPAN_ANGLE + ARROWHEAD_ANGLE) / 2);
//                double h1Sin = Math.sin((-LOOP_SPAN_ANGLE + ARROWHEAD_ANGLE) / 2);
//                double h1X = lNX + ARROWHEAD_LENGTH * h1Cos;
//                double h1Y = -lNY + ARROWHEAD_LENGTH * h1Sin;
//                double h2Cos = Math.cos((-LOOP_SPAN_ANGLE - ARROWHEAD_ANGLE) / 2);
//                double h2Sin = Math.sin((-LOOP_SPAN_ANGLE - ARROWHEAD_ANGLE) / 2);
//                double h2X = lNX + ARROWHEAD_LENGTH * h2Cos;
//                double h2Y = -lNY + ARROWHEAD_LENGTH * h2Sin;
//
//                curve().getElements().addAll(
//                        new LineTo(h1X, h1Y),
//                        new MoveTo(lNX, -lNY),
//                        new LineTo(h2X, h2Y));
//            }
        }
        else {
//            positionProperty().bind(Bindings.createObjectBinding(
//                    () -> startVertex.getPosition().midpoint(endVertex.getPosition()),
//                    startVertex.positionProperty(), endVertex.positionProperty()));
//
//            rotateProperty().bind(Bindings.createDoubleBinding(
//                    () -> X_AXIS.angle(endVertex.getPosition().subtract(startVertex.getPosition())),
//                    startVertex.positionProperty(), endVertex.positionProperty()));
//
//            double d = startVertex.getPosition().distance(endVertex.getPosition());
//
//            double offsetAngle;
//            double aX, aY;
//
//            if (2 * idx + 1 == num) {
//                offsetAngle = 0.0;
//
//                aX = d / 2 - VERTEX_RADIUS;
//                aY = 0.0;
//
//                switch (textDisplayMode) {
//                    case TOP -> {
//                        curve().getElements().addAll(
//                                new MoveTo(-aX, 0.0),
//                                new LineTo(aX, 0.0));
//
//                        text.setLayoutY(Math.cos(baseAngle) < 0 ? TEXT_GAP : -TEXT_GAP);
//                    }
//                    case MIDDLE -> {
//                        if (text.getText() == null || text.getText().equals(""))
//                            curve().getElements().addAll(
//                                    new MoveTo(-aX, 0.0),
//                                    new LineTo(aX, 0.0));
//                        else {
//                            double tX = text.getLayoutBounds().getWidth() / 2 + TEXT_EPSILON;
//                            if (tX > d / 2)
//                                tX = d / 2;
//
//                            curve().getElements().addAll(
//                                    new MoveTo(-aX, 0.0),
//                                    new LineTo(-tX, 0.0),
//                                    new MoveTo(tX, 0.0),
//                                    new LineTo(aX, 0.0));
//
//                            text.setLayoutY(0.0);
//                        }
//                    }
//                    case BOTTOM -> {
//                        curve().getElements().addAll(
//                                new MoveTo(-aX, 0.0),
//                                new LineTo(aX, 0.0));
//
//                        text.setLayoutY(Math.cos(baseAngle) < 0 ? -TEXT_GAP : TEXT_GAP);
//                    }
//                }
//            } else {
//                boolean lowerHalf = idx < num / 2;
//                offsetAngle = (idx - (num - 1) / 2.0) * GAP_ANGLE;
//
//                double cos = Math.cos(offsetAngle);
//                double sin = Math.sin(offsetAngle);
//                aX = d / 2 - VERTEX_RADIUS * cos;
//                aY = VERTEX_RADIUS * sin;
//
//                double r = aX / Math.abs(sin);
//                double fY = r - Math.sqrt(Math.pow(r, 2) + Math.pow(VERTEX_RADIUS, 2) - Math.pow(d / 2, 2));
//
//                switch (textDisplayMode) {
//                    case TOP -> {
//                        curve().getElements().addAll(
//                                new MoveTo(-aX, aY),
//                                new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));
//
//                        text.setLayoutY((lowerHalf ? -fY : fY) +
//                                        (Math.cos(baseAngle) < 0 ? TEXT_GAP : -TEXT_GAP));
//                    }
//                    case MIDDLE -> {
//                        if (text.getText() == null || text.getText().equals(""))
//                            curve().getElements().addAll(
//                                    new MoveTo(-aX, aY),
//                                    new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));
//                        else {
//                            double tX = text.getLayoutBounds().getWidth() / 2 + TEXT_EPSILON;
//                            if (tX > d / 2)
//                                tX = d / 2;
//                            double offY = r - Math.sqrt(Math.pow(r, 2) - Math.pow(tX, 2));
//                            double tY = lowerHalf ? -fY + offY : fY - offY;
//
//                            curve().getElements().addAll(
//                                    new MoveTo(-aX, aY),
//                                    new ArcTo(r, r, 0.0, -tX, tY, cos < 0.0, lowerHalf),
//                                    new MoveTo(tX, tY),
//                                    new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));
//
//                            text.setLayoutY(tY);
//                        }
//                    }
//                    case BOTTOM -> {
//                        curve().getElements().addAll(
//                                new MoveTo(-aX, aY),
//                                new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));
//
//                        text.setLayoutY((lowerHalf ? -fY : fY) +
//                                        (Math.cos(baseAngle) < 0 ? -TEXT_GAP : TEXT_GAP));
//                    }
//                }
//            }
//
//            // TODO: Revert this
//            if (!directed) {
//                double h1Cos = Math.cos(offsetAngle + ARROWHEAD_ANGLE / 2);
//                double h1Sin = Math.sin(offsetAngle + ARROWHEAD_ANGLE / 2);
//                double h1X = aX - ARROWHEAD_LENGTH * h1Cos;
//                double h1Y = aY + ARROWHEAD_LENGTH * h1Sin;
//                double h2Cos = Math.cos(offsetAngle - ARROWHEAD_ANGLE / 2);
//                double h2Sin = Math.sin(offsetAngle - ARROWHEAD_ANGLE / 2);
//                double h2X = aX - ARROWHEAD_LENGTH * h2Cos;
//                double h2Y = aY + ARROWHEAD_LENGTH * h2Sin;
//
//                if (reverted)
//                    curve().getElements().addAll(
//                            new MoveTo(-aX, aY),
//                            new LineTo(-h1X, h1Y),
//                            new MoveTo(-aX, aY),
//                            new LineTo(-h2X, h2Y));
//                else
//                    curve().getElements().addAll(
//                            new LineTo(h1X, h1Y),
//                            new MoveTo(aX, aY),
//                            new LineTo(h2X, h2Y));
//            }
//
//            text.setRotate(Math.cos(baseAngle) > 0.0 ? 0.0 : 180.0);

        }
    }

    private class SelfArc extends Path {
        private SelfArc() {
            positionProperty().bind(startVertex.positionProperty());

            double sCos = Math.cos(LOOP_SPAN_ANGLE / 2);
            double sSin = Math.sin(LOOP_SPAN_ANGLE / 2);
            double lNX = VERTEX_RADIUS * sCos;
            double lNY = VERTEX_RADIUS * sSin;
            double lFX = LINE_LENGTH * sCos;
            double lFY = LINE_LENGTH * sSin;

            double r = LINE_LENGTH * Math.tan(Edge.LOOP_SPAN_ANGLE / 2);

            getElements().addAll(
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

                getElements().addAll(
                        new LineTo(h1X, h1Y),
                        new MoveTo(lNX, -lNY),
                        new LineTo(h2X, h2Y));
            }

            text.setLayoutX(LINE_LENGTH / Math.cos(LOOP_SPAN_ANGLE / 2) + r + TEXT_GAP);
            text.setRotate(90.0);
        }
    }

    private class CrossingArc extends Path {

    }

//    /**
//     * Request focus when pressed
//     */
//    @Override
//    protected void pressed(MouseEvent m) {
//        if (m.isShiftDown()) return;
//        System.out.println("Edge Pressed");
//        requestFocus();
//    }

    public void update() {
//        if (isLoop()) {
//            double cX = startVertex.getX();
//            double cY = startVertex.getY();
//
//            List<Double> angles = startVertex.computeAngles();
//            double baseAngle = -PI / 2;
//            if (!angles.isEmpty()) {
//                angles.add(angles.get(0) + 2 * PI);
//                double maxSpan = Double.NEGATIVE_INFINITY;
//                for (int i = 1; i < angles.size(); ++i) {
//                    double span = angles.get(i) - angles.get(i - 1);
//                    if (span > maxSpan) {
//                        maxSpan = span;
//                        baseAngle = (angles.get(i) + angles.get(i - 1)) / 2;
//                    }
//                }
//            }
//
//            double offsetAngle = ((2 * idx - num + 1) * (LOOP_GAP_ANGLE + LOOP_SPAN_ANGLE)) / 2;
////            ((Rotate) getTransforms().get(0)).setAngle(Math.toDegrees(baseAngle + offsetAngle));
//            setRotate(Math.toDegrees(baseAngle + offsetAngle));
//
//            text.setRotate(Math.sin(baseAngle + offsetAngle) < 0.0 ? 90.0 : -90.0);
//        } else {
//            curve().getElements().clear();
//
//            double sX, sY, eX, eY;
//            boolean reverted = System.identityHashCode(startVertex) > System.identityHashCode(endVertex);
//            if (reverted) {
//                sX = endVertex.getX();
//                sY = endVertex.getY();
//                eX = startVertex.getX();
//                eY = startVertex.getY();
//            }
//            else {
//                sX = startVertex.getX();
//                sY = startVertex.getY();
//                eX = endVertex.getX();
//                eY = endVertex.getY();
//            }
//
//            setPositionInView((sX + eX) / 2, (sY + eY) / 2);
//
//            double baseAngle = sX == eX ? sY < eY ? PI / 2 : -PI / 2 : Math.atan2(eY - sY, eX - sX);
////            ((Rotate) getTransforms().get(0)).setAngle(Math.toDegrees(baseAngle));
//            setRotate(Math.toDegrees(baseAngle));
//
//            double d = Math.sqrt(Math.pow(eY - sY, 2) + Math.pow(eX - sX, 2));
//
//            double offsetAngle;
//            double aX, aY;
//
//            if (2 * idx + 1 == num) {
//                offsetAngle = 0.0;
//
//                aX = d / 2 - VERTEX_RADIUS;
//                aY = 0.0;
//
//                switch (textDisplayMode) {
//                    case TOP -> {
//                        curve().getElements().addAll(
//                                new MoveTo(-aX, 0.0),
//                                new LineTo(aX, 0.0));
//
//                        text.setLayoutY(Math.cos(baseAngle) < 0 ? TEXT_GAP : -TEXT_GAP);
//                    }
//                    case MIDDLE -> {
//                        if (text.getText() == null || text.getText().equals(""))
//                            curve().getElements().addAll(
//                                    new MoveTo(-aX, 0.0),
//                                    new LineTo(aX, 0.0));
//                        else {
//                            double tX = text.getLayoutBounds().getWidth() / 2 + TEXT_EPSILON;
//                            if (tX > d / 2)
//                                tX = d / 2;
//
//                            curve().getElements().addAll(
//                                    new MoveTo(-aX, 0.0),
//                                    new LineTo(-tX, 0.0),
//                                    new MoveTo(tX, 0.0),
//                                    new LineTo(aX, 0.0));
//
//                            text.setLayoutY(0.0);
//                        }
//                    }
//                    case BOTTOM -> {
//                        curve().getElements().addAll(
//                                new MoveTo(-aX, 0.0),
//                                new LineTo(aX, 0.0));
//
//                        text.setLayoutY(Math.cos(baseAngle) < 0 ? -TEXT_GAP : TEXT_GAP);
//                    }
//                }
//            } else {
//                boolean lowerHalf = idx < num / 2;
//                offsetAngle = (idx - (num - 1) / 2.0) * GAP_ANGLE;
//
//                double cos = Math.cos(offsetAngle);
//                double sin = Math.sin(offsetAngle);
//                aX = d / 2 - VERTEX_RADIUS * cos;
//                aY = VERTEX_RADIUS * sin;
//
//                double r = aX / Math.abs(sin);
//                double fY = r - Math.sqrt(Math.pow(r, 2) + Math.pow(VERTEX_RADIUS, 2) - Math.pow(d / 2, 2));
//
//                switch (textDisplayMode) {
//                    case TOP -> {
//                        curve().getElements().addAll(
//                                new MoveTo(-aX, aY),
//                                new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));
//
//                        text.setLayoutY((lowerHalf ? -fY : fY) +
//                                        (Math.cos(baseAngle) < 0 ? TEXT_GAP : -TEXT_GAP));
//                    }
//                    case MIDDLE -> {
//                        if (text.getText() == null || text.getText().equals(""))
//                            curve().getElements().addAll(
//                                    new MoveTo(-aX, aY),
//                                    new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));
//                        else {
//                            double tX = text.getLayoutBounds().getWidth() / 2 + TEXT_EPSILON;
//                            if (tX > d / 2)
//                                tX = d / 2;
//                            double offY = r - Math.sqrt(Math.pow(r, 2) - Math.pow(tX, 2));
//                            double tY = lowerHalf ? -fY + offY : fY - offY;
//
//                            curve().getElements().addAll(
//                                    new MoveTo(-aX, aY),
//                                    new ArcTo(r, r, 0.0, -tX, tY, cos < 0.0, lowerHalf),
//                                    new MoveTo(tX, tY),
//                                    new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));
//
//                            text.setLayoutY(tY);
//                        }
//                    }
//                    case BOTTOM -> {
//                        curve().getElements().addAll(
//                                new MoveTo(-aX, aY),
//                                new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));
//
//                        text.setLayoutY((lowerHalf ? -fY : fY) +
//                                        (Math.cos(baseAngle) < 0 ? -TEXT_GAP : TEXT_GAP));
//                    }
//                }
//            }
//
//            // TODO: Revert this
//            if (!directed) {
//                double h1Cos = Math.cos(offsetAngle + ARROWHEAD_ANGLE / 2);
//                double h1Sin = Math.sin(offsetAngle + ARROWHEAD_ANGLE / 2);
//                double h1X = aX - ARROWHEAD_LENGTH * h1Cos;
//                double h1Y = aY + ARROWHEAD_LENGTH * h1Sin;
//                double h2Cos = Math.cos(offsetAngle - ARROWHEAD_ANGLE / 2);
//                double h2Sin = Math.sin(offsetAngle - ARROWHEAD_ANGLE / 2);
//                double h2X = aX - ARROWHEAD_LENGTH * h2Cos;
//                double h2Y = aY + ARROWHEAD_LENGTH * h2Sin;
//
//                if (reverted)
//                    curve().getElements().addAll(
//                            new MoveTo(-aX, aY),
//                            new LineTo(-h1X, h1Y),
//                            new MoveTo(-aX, aY),
//                            new LineTo(-h2X, h2Y));
//                else
//                    curve().getElements().addAll(
//                            new LineTo(h1X, h1Y),
//                            new MoveTo(aX, aY),
//                            new LineTo(h2X, h2Y));
//            }
//
//            text.setRotate(Math.cos(baseAngle) > 0.0 ? 0.0 : 180.0);
//        }
    }

    @Override
    public void setLabel(String label) {
        super.setLabel(label);
        update();
    }

    public int getIdx() {
        return idx.get();
    }
    public int getNum() {
        return num.get();
    }
    /**
     * Given the num_shapes and shape_index, determine the offset of that edge
     *
     * @param edgeIdx  the index of this edge among all edges between startVertex and endVertex
     * @param numEdges number of edges between startVertex and endVertex (including itself)
     */
    public void updateIdx(int edgeIdx, int numEdges) {
        this.idx.set(edgeIdx);
        this.num.set(numEdges);
    }

//    /**
//     * Event handler to handle all the MouseEvents
//     */
//    public class mouseEventHandler implements EventHandler<MouseEvent> {
//        @Override
//        public void handle(MouseEvent event) {
//            event.consume();
//            if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
//                pressed(event);
//            else if (event.getEventType() == MouseEvent.MOUSE_ENTERED)
//                getScene().setCursor(Cursor.HAND);
//            else if (event.getEventType() == MouseEvent.MOUSE_EXITED)
//                getScene().setCursor(Cursor.DEFAULT);
//        }
//    }

    private void attach() {
        startVertex.attach(this);
        endVertex.attach(this);
    }

    @Override
    public void erase() {
        startVertex.detach(this);
        endVertex.detach(this);
    }

    public Vertex other(Vertex vertex) {
        if (vertex == startVertex)
            return endVertex;

        if (vertex == endVertex)
            return startVertex;

        return null;
    }
    
    private boolean isSelfLoop() {
        return startVertex.equals(endVertex);
    }
    
    private boolean isReverted() {
        return System.identityHashCode(startVertex) > System.identityHashCode(endVertex);
    }
    
    private Vertex primaryVertex() {
        return isReverted() ? endVertex : startVertex;
    }
    
    private Vertex secondaryVertex() {
        return isReverted() ? startVertex : endVertex;
    }

    private Path curve() {
        return (Path) shape;
    }

    /**
     * Convert the Edge Object to a String that contains all the information
     */
    @Override
    public String toText() {
        String[] temp = new String[]{String.valueOf(1),
                                     String.valueOf(2),
                                     text.getText()};
        return (String) StringUtil.join(" ", Arrays.asList(temp));
    }

}