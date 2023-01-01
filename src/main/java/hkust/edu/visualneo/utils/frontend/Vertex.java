package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.utils.backend.Node;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.PI;

public class Vertex extends GraphElement {

    static final double VERTEX_RADIUS = 25.0;
    static final double HIGHLIGHT_RADIUS = VERTEX_RADIUS + 5.0;
    private static final double STROKE_WIDTH = 1.0;
    private static final double DEFAULT_ANGLE = -PI / 2;
    private static final Color CIRCLE_COLOR = Color.LIGHTGRAY;
    private static final Color STROKE_COLOR = Color.DARKGREY;

    private final DoubleProperty selfLoopAngle =
            new SimpleDoubleProperty(this, "selfLoopAngle", DEFAULT_ANGLE);

    private final MapProperty<Vertex, SetProperty<Edge>> neighborhood =
            new SimpleMapProperty<>(this, "neighborhood", FXCollections.observableHashMap());
//    private final MapProperty<Vertex, SetProperty<Edge>> unmodifiableNeighborhood =
//            FXCollections.unmodifiableMapProperty(neighborhood);

    private final ChangeListener<Point2D> positionListener =  // For re-usability
            (observable, oldValue, newValue) -> updateSelfLoopAngle();

    private final SetChangeListener<Edge> neighborEdgesListener = change -> {  // For re-usability
        if (change.getSet().isEmpty()) {
            getNeighborhood().remove(change.getElementRemoved().other(Vertex.this));
            return;
        }
        if (Vertex.this == change.getSet().iterator().next().primaryVertex) {
            int idx = 0;
            for (Edge edge : change.getSet()) {
                edge.setIdx(idx);
                ++idx;
            }
        }
    };

    private final MapChangeListener<Vertex, SetProperty<Edge>> neighborhoodListener = change -> {  // For clarity
        if (change.wasAdded()) {
            change.getKey().positionProperty().addListener(positionListener);
            change.getValueAdded().addListener(neighborEdgesListener);
        } else if (change.wasRemoved()) {
            change.getKey().positionProperty().removeListener(positionListener);
            change.getValueRemoved().removeListener(neighborEdgesListener);
        }
        updateSelfLoopAngle();
    };

    public Vertex(Canvas canvas) {
        super(canvas, currentId++);

        initializeGraphics();

        positionProperty().addListener(positionListener);
        neighborhoodProperty().addListener(neighborhoodListener);

        // For debugging
        System.out.println("A new Vertex is created.");
    }

    public Vertex(Canvas canvas, Node node) {
        super(canvas, node.getId());

        initializeGraphics();

        positionProperty().addListener(positionListener);
        neighborhoodProperty().addListener(neighborhoodListener);

        // Add the label and properties (if any)
        setLabel(node.getLabel());
        properties = node.getProperties();

        // For debugging
        System.out.println("A new Vertex is created.");
    }

    @Override
    protected void initializeGraphics() {
        shape = new Circle(VERTEX_RADIUS, CIRCLE_COLOR);
        shape.setStrokeWidth(STROKE_WIDTH);
        shape.setStroke(STROKE_COLOR);

        highlightShape = new Circle(HIGHLIGHT_RADIUS);

        getChildren().addAll(highlightShape, shape);

        highlightProperty().addListener((observable, oldValue, newValue) ->
                                                highlightShape.setFill(newValue ? HIGHLIGHT_COLOR : Color.TRANSPARENT));

        super.initializeGraphics();
    }

    @Override
    protected void entered(MouseEvent e) {
        super.entered(e);
        if (!isHighlighted())
            highlightShape.setFill(HOVER_COLOR);
    }
    @Override
    protected void exited(MouseEvent e) {
        super.exited(e);
        if (!isHighlighted())
            highlightShape.setFill(Color.TRANSPARENT);
    }
    @Override
    protected void dragged(MouseEvent e) {
        getScene().setCursor(Cursor.CLOSED_HAND);
    }
    @Override
    protected void released(MouseEvent e) {
        getScene().setCursor(Cursor.HAND);
    }

    public boolean hasSelfLoop() {
        return hasNeighbor(this);
    }

    private void updateSelfLoopAngle() {
        if (hasSelfLoop()) {
            List<Double> angles = computeAngles();
            if (!angles.isEmpty()) {
                angles.add(angles.get(0) + 2 * PI);
                double maxSpan = Double.NEGATIVE_INFINITY;
                int idx = 0;
                for (int i = 1; i < angles.size(); ++i) {
                    double span = angles.get(i) - angles.get(i - 1);
                    if (span > maxSpan) {
                        maxSpan = span;
                        idx = i;
                    }
                }
                setSelfLoopAngle((angles.get(idx) + angles.get(idx - 1)) / 2);
                return;
            }
        }
        setSelfLoopAngle(DEFAULT_ANGLE);
    }

    public List<Double> computeAngles() {
        return getNeighbors()
                .stream()
                .filter(other -> !other.equals(this))
                .map(other -> angle(this, other))
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void attach(Edge edge) {
        Vertex neighbor = edge.other(this);
        SetProperty<Edge> neighborEdgesProperty = edgesPropertyBetween(neighbor);
        if (neighborEdgesProperty == null) {
            neighborEdgesProperty =
                    new SimpleSetProperty<>(FXCollections.observableSet(new LinkedHashSet<>()));
            neighborhood.put(neighbor, neighborEdgesProperty);
        }
        neighborEdgesProperty.add(edge);
    }

    public void detach(Edge edge) {
        Set<Edge> edges = getEdgesBetween(edge.other(this));
        if(edges != null) edges.remove(edge);
    }

    public MapProperty<Vertex, SetProperty<Edge>> neighborhoodProperty() {
        return neighborhood;
    }
    public Map<Vertex, SetProperty<Edge>> getNeighborhood() {
        return neighborhoodProperty().get();
    }

    public boolean hasNeighbor(Vertex neighbor) {
        return getNeighborhood().containsKey(neighbor);
    }

    public Set<Vertex> getNeighbors() {
        return getNeighborhood().keySet();
    }

    public SetProperty<Edge> edgesPropertyBetween(Vertex neighbor) {
        return getNeighborhood().get(neighbor);
    }

    public Set<Edge> getEdgesBetween(Vertex neighbor) {
        SetProperty<Edge> neighborEdgesProperty = edgesPropertyBetween(neighbor);
        return neighborEdgesProperty == null ?
                null : neighborEdgesProperty.get();
    }

    public Set<Edge> getEdges() {
        return getNeighborhood()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public ReadOnlyIntegerProperty numEdgesPropertyBetween(Vertex neighbor) {
        SetProperty<Edge> neighborEdgesProperty = edgesPropertyBetween(neighbor);
        return neighborEdgesProperty == null ?
                null : neighborEdgesProperty.sizeProperty();
    }

    public int getNumEdgesBetween(Vertex neighbor) {
        SetProperty<Edge> neighborEdgesProperty = edgesPropertyBetween(neighbor);
        return neighborEdgesProperty == null ?
                0 : neighborEdgesProperty.getSize();
    }

    public DoubleProperty selfLoopAngleProperty() {
        return selfLoopAngle;
    }

    public double getSelfLoopAngle() {
        return selfLoopAngleProperty().get();
    }

    public void setSelfLoopAngle(double angle) {
        selfLoopAngleProperty().set(angle);
    }

    @Override
    public void erase() {
        canvas.erase(this);
        getEdges().forEach(Edge::erase);
    }

    /**
     * Convert the Vertex Object to a String that contains all the information
     */
    @Override
    public String toText() {
        String[] temp = new String[]{
                "v",
                String.valueOf(getId()),
                String.valueOf(getX()),
                String.valueOf(getY()),
                getLabel(),
                propertyToText()};
        return String.join(" ", Arrays.asList(temp));
    }
}
