package hkust.edu.visualneo.utils.frontend;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.transform.Rotate;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.StringUtil;

import java.util.Arrays;

import static hkust.edu.visualneo.utils.frontend.Vertex.VERTEX_RADIUS;
import static java.lang.Math.PI;

public class Edge extends GraphElement {

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

    public final Vertex startVertex;
    public final Vertex endVertex;

    public final Vertex primaryVertex;
    public final Vertex secondaryVertex;

    private final BooleanProperty directed =
            new SimpleBooleanProperty(this, "directed", true);
    private final IntegerProperty idx =
            new SimpleIntegerProperty(this, "idx", -1);
    private final DoubleProperty angle;

    public Edge(Canvas canvas, Vertex startVertex, Vertex endVertex, boolean directed) {
        super(canvas);
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        setDirected(directed);
        
        if (isReverted()) {
            primaryVertex = endVertex;
            secondaryVertex = startVertex;
        }
        else {
            primaryVertex = startVertex;
            secondaryVertex = endVertex;
        }

        // To change the pivot of rotation
        Rotate rotate = new Rotate();
        angle = rotate.angleProperty();
        getTransforms().add(rotate);

        // Notify two vertices to attach it
        attach();
        initializeGraphics();

        // For Debugging
        System.out.println("An Edge from (" + startVertex.getX() + " , " + startVertex.getY() + ") to " +
                "(" + endVertex.getX() + " , " + endVertex.getY() + ")" + " is created!");
    }

    @Override
    protected void initializeGraphics() {
        super.initializeGraphics();

        shape = new Path();

        if (isSelfLoop()) {  // Self-loop case
            positionProperty().bind(startVertex.positionProperty());
            angleProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.toDegrees(
                            startVertex.getSelfLoopAngle() +
                            ((2 * getIdx() - startVertex.getNumEdgesBetween(endVertex) + 1) *
                             (LOOP_GAP_ANGLE + LOOP_SPAN_ANGLE)) / 2),
                    startVertex.selfLoopAngleProperty(),
                    startVertex.numEdgesPropertyBetween(endVertex),
                    idxProperty()));

            double r = LINE_LENGTH * Math.tan(Edge.LOOP_SPAN_ANGLE / 2);

            text.setLayoutX(LINE_LENGTH / Math.cos(LOOP_SPAN_ANGLE / 2) + r + TEXT_GAP);
            text.rotateProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.sin(Math.toRadians(getAngle())) < 0.0 ? 90.0 : -90.0,
                    angleProperty()));

            double sCos = Math.cos(LOOP_SPAN_ANGLE / 2);
            double sSin = Math.sin(LOOP_SPAN_ANGLE / 2);
            double lNX = VERTEX_RADIUS * sCos;
            double lNY = VERTEX_RADIUS * sSin;
            double lFX = LINE_LENGTH * sCos;
            double lFY = LINE_LENGTH * sSin;

            arc().getElements().addAll(
                    new MoveTo(lNX, lNY),
                    new LineTo(lFX, lFY),
                    new ArcTo(r, r, 0.0, lFX, -lFY, true, false),
                    new LineTo(lNX, -lNY));

            if (!isDirected()) {
                double h1Cos = Math.cos((-LOOP_SPAN_ANGLE + ARROWHEAD_ANGLE) / 2);
                double h1Sin = Math.sin((-LOOP_SPAN_ANGLE + ARROWHEAD_ANGLE) / 2);
                double h1X = lNX + ARROWHEAD_LENGTH * h1Cos;
                double h1Y = -lNY + ARROWHEAD_LENGTH * h1Sin;
                double h2Cos = Math.cos((-LOOP_SPAN_ANGLE - ARROWHEAD_ANGLE) / 2);
                double h2Sin = Math.sin((-LOOP_SPAN_ANGLE - ARROWHEAD_ANGLE) / 2);
                double h2X = lNX + ARROWHEAD_LENGTH * h2Cos;
                double h2Y = -lNY + ARROWHEAD_LENGTH * h2Sin;

                arc().getElements().addAll(
                        new LineTo(h1X, h1Y),
                        new MoveTo(lNX, -lNY),
                        new LineTo(h2X, h2Y));
            }
        }
        else {  // Crossing-arc case
            positionProperty().bind(Bindings.createObjectBinding(
                    () -> primaryVertex.getPosition().midpoint(secondaryVertex.getPosition()),
                    primaryVertex.positionProperty(), secondaryVertex.positionProperty()));
            angleProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.toDegrees(angle(startVertex, endVertex)),
                    startVertex.positionProperty(), endVertex.positionProperty()));

            final DoubleBinding baseAngle = Bindings.createDoubleBinding(
                    () -> Math.toRadians(getAngle()),
                    angleProperty());

            text.rotateProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.cos(baseAngle.get()) > 0.0 ? 0.0 : 180.0,
                    baseAngle));

            final DoubleBinding d = Bindings.createDoubleBinding(
                    () -> primaryVertex.getPosition().distance(secondaryVertex.getPosition()),
                    primaryVertex.positionProperty(), secondaryVertex.positionProperty());

            final DoubleBinding offsetAngle = Bindings.createDoubleBinding(
                    () -> 2 * getIdx() + 1 == primaryVertex.getNumEdgesBetween(secondaryVertex) ?
                          0.0 : (isReverted() ? -1 : 1) *
                                (getIdx() - (primaryVertex.getNumEdgesBetween(secondaryVertex) - 1) / 2.0) *
                                GAP_ANGLE,
                    idxProperty(), primaryVertex.numEdgesPropertyBetween(secondaryVertex));

            final ChangeListener<Object> updateListener =
                    (observable, oldValue, newValue) -> updateCrossingArc(d, offsetAngle);

            directedProperty().addListener(updateListener);
            d.addListener(updateListener);
            offsetAngle.addListener(updateListener);
            text.layoutBoundsProperty().addListener(updateListener);

            updateCrossingArc(d, offsetAngle);
        }

        getChildren().add(shape);
        shape.toBack();

        highlightProperty().addListener((observable, oldValue, newValue) -> {
            shape.setStrokeWidth(newValue ? HIGHLIGHT_STROKE_WIDTH : DEFAULT_STROKE_WIDTH);
            shape.setStroke(newValue ? HIGHLIGHT_COLOR : DEFAULT_COLOR);
        });
    }

    private void updateCrossingArc(DoubleBinding dBinding, DoubleBinding offsetAngleBinding) {
        arc().getElements().clear();

        double d = dBinding.get();
        double offsetAngle = offsetAngleBinding.get();

        boolean atMiddle = offsetAngle == 0.0;

        int idx = getIdx();
        int num = primaryVertex.getNumEdgesBetween(secondaryVertex);

        double aX, aY;

        if (atMiddle) {
            aX = d / 2 - VERTEX_RADIUS;
            aY = 0.0;

            if (text.getLayoutBounds().getWidth() < TEXT_EPSILON)
                arc().getElements().addAll(
                        new MoveTo(-aX, 0.0),
                        new LineTo(aX, 0.0));
            else {
                double tX = text.getLayoutBounds().getWidth() / 2 + TEXT_EPSILON;
                if (tX > d / 2)
                    tX = d / 2;

                arc().getElements().addAll(
                        new MoveTo(-aX, 0.0),
                        new LineTo(-tX, 0.0),
                        new MoveTo(tX, 0.0),
                        new LineTo(aX, 0.0));

                text.setLayoutY(0.0);
            }
        }
        else {
            boolean lowerHalf = (idx < num / 2) != isReverted();

            double cos = Math.cos(offsetAngle);
            double sin = Math.sin(offsetAngle);
            aX = d / 2 - VERTEX_RADIUS * cos;
            aY = VERTEX_RADIUS * sin;

            double r = aX / Math.abs(sin);
            double fY = r - Math.sqrt(Math.pow(r, 2) + Math.pow(VERTEX_RADIUS, 2) - Math.pow(d / 2, 2));

            if (text.getText() == null || text.getText().equals(""))
                arc().getElements().addAll(
                        new MoveTo(-aX, aY),
                        new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));
            else {
                double tX = text.getLayoutBounds().getWidth() / 2 + TEXT_EPSILON;
                if (tX > d / 2)
                    tX = d / 2;
                double offY = r - Math.sqrt(Math.pow(r, 2) - Math.pow(tX, 2));
                double tY = lowerHalf ? -fY + offY : fY - offY;

                arc().getElements().addAll(
                        new MoveTo(-aX, aY),
                        new ArcTo(r, r, 0.0, -tX, tY, cos < 0.0, lowerHalf),
                        new MoveTo(tX, tY),
                        new ArcTo(r, r, 0.0, aX, aY, cos < 0.0, lowerHalf));

                text.setLayoutY(tY);
            }
        }

        if (!isDirected()) {
            double h1Cos = Math.cos(offsetAngle + ARROWHEAD_ANGLE / 2);
            double h1Sin = Math.sin(offsetAngle + ARROWHEAD_ANGLE / 2);
            double h1X = aX - ARROWHEAD_LENGTH * h1Cos;
            double h1Y = aY + ARROWHEAD_LENGTH * h1Sin;
            double h2Cos = Math.cos(offsetAngle - ARROWHEAD_ANGLE / 2);
            double h2Sin = Math.sin(offsetAngle - ARROWHEAD_ANGLE / 2);
            double h2X = aX - ARROWHEAD_LENGTH * h2Cos;
            double h2Y = aY + ARROWHEAD_LENGTH * h2Sin;

            arc().getElements().addAll(
                    new LineTo(h1X, h1Y),
                    new MoveTo(aX, aY),
                    new LineTo(h2X, h2Y));
        }
    }

    public BooleanProperty directedProperty() {
        return directed;
    }
    public boolean isDirected() {
        return directedProperty().get();
    }
    public void setDirected(boolean directed) {
        directedProperty().set(directed);
    }

    public IntegerProperty idxProperty() {
        return idx;
    }
    public int getIdx() {
        return idxProperty().get();
    }
    public void setIdx(int idx) {
        idxProperty().set(idx);
    }

    public DoubleProperty angleProperty() {
        return angle;
    }
    public double getAngle() {
        return angleProperty().get();
    }

    public void attach() {
        startVertex.attach(this);
        endVertex.attach(this);
    }

    @Override
    public void erase() {
        canvas.getChildren().remove(this);
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
    
    public boolean isSelfLoop() {
        return startVertex.equals(endVertex);
    }
    
    public boolean isReverted() {
        return System.identityHashCode(startVertex) > System.identityHashCode(endVertex);
    }

    private Path arc() {
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