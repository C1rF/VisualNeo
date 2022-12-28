package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.utils.backend.Node;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.PI;

public class Vertex extends GraphElement {

    // Radius of the Vertex
    static final double VERTEX_RADIUS = 25.0;
    private static final double DEFAULT_STROKE_WIDTH = 1.0;
    private static final double HIGHLIGHT_STROKE_WIDTH = 2.0;
    private static final double DEFAULT_ANGLE = -PI / 2;
    private static final Color DEFAULT_COLOR = Color.DARKGREY;
    private static final Color HIGHLIGHT_COLOR = Color.BLACK;
    private static final Color CIRCLE_COLOR = Color.LIGHTGRAY;

    private final DoubleProperty selfLoopAngle =
            new SimpleDoubleProperty(this, "selfLoopAngle", DEFAULT_ANGLE);

    private final MapProperty<Vertex, SetProperty<Edge>> neighborhood =
            new SimpleMapProperty<>(this, "neighborhood", FXCollections.observableHashMap());

    private final ChangeListener<Point2D> positionListener =  // For re-usability
            (observable, oldValue, newValue) -> updateSelfLoopAngle();

    private final SetChangeListener<Edge> neighborEdgesListener = c -> {  // For re-usability
        if (c.getSet().size() == 0) {
            getNeighborhood().remove(c.getElementRemoved().other(Vertex.this));
            return;
        }
        if (Vertex.this == c.getSet().iterator().next().primaryVertex) {
            int idx = 0;
            for (Edge edge : c.getSet()) {
                edge.setIdx(idx);
                ++idx;
            }
        }
    };

    private final MapChangeListener<Vertex, SetProperty<Edge>> neighborhoodListener = c -> {  // For clarity
        if (c.wasAdded()) {
            c.getKey().positionProperty().addListener(positionListener);
            c.getValueAdded().addListener(neighborEdgesListener);
        } else if (c.wasRemoved()) {
            c.getKey().positionProperty().removeListener(positionListener);
            c.getValueRemoved().removeListener(neighborEdgesListener);
        }
        updateSelfLoopAngle();
    };

    // Constructor
    public Vertex(Canvas canvas, double x, double y) {
        super(canvas, currentId++);

        setPosition(x, y);
        initializeGraphics();

        positionProperty().addListener(positionListener);
        neighborhoodProperty().addListener(neighborhoodListener);

        // For debugging
        System.out.println("A new Vertex is created.");
    }

    public Vertex(Canvas canvas, Node node, Point2D position) {
        super(canvas, node.getId());
        // TODO: Add information

        setPositionInView(position);
        initializeGraphics();

        positionProperty().addListener(positionListener);
        neighborhoodProperty().addListener(neighborhoodListener);

        // For debugging
        System.out.println("A new Vertex is created.");
    }

    @Override
    protected void initializeGraphics() {
        super.initializeGraphics();

        shape = new Circle(VERTEX_RADIUS, CIRCLE_COLOR);
        getChildren().add(shape);
        shape.toBack();

        highlightProperty().addListener((observable, oldValue, newValue) -> {
            shape.setStrokeWidth(newValue ? HIGHLIGHT_STROKE_WIDTH : DEFAULT_STROKE_WIDTH);
            shape.setStroke(newValue ? HIGHLIGHT_COLOR : DEFAULT_COLOR);
        });
    }

    @Override
    protected void initializeHandlers() {
        super.initializeHandlers();
        setOnMouseDragged(e -> getScene().setCursor(Cursor.CLOSED_HAND));
        setOnMouseReleased(e -> getScene().setCursor(Cursor.HAND));
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
        return neighborhood.get();
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
        getEdges().forEach(Edge::erase);
    }

    /**
     * Convert the Vertex Object to a String that contains all the information
     */
    @Override
    public String toText() {
        List<Vertex> vertices = canvas.getVertices();
        int vertexId = vertices.indexOf(this);
        String[] temp = new String[]{
                "v",
                String.valueOf(vertexId),
                String.valueOf(getX()),
                String.valueOf(getY()),
                getLabel(),
                propertyToText()};
        return String.join(" ", Arrays.asList(temp));
    }
}
