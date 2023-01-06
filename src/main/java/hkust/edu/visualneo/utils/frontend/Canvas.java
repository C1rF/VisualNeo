package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.utils.backend.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.EventTarget;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Canvas extends Pane implements Observable {

    public enum CanvasType {
        NONE,
        STATIC,
        NAVIGABLE,
        MODIFIABLE
    }

    private static final double UNIT_SCROLL = 32.0;

    private static final int SEARCH_SAMPLES = 20;

    private static Map<String, Color> colorTable;

    private CanvasType type = CanvasType.NONE;

    public final OrthogonalCamera camera = new OrthogonalCamera(this);

    private boolean valid = true;
    private Collection<InvalidationListener> listeners = new ArrayList<>();

    private final ObservableSet<GraphElement> highlights =
            new SimpleSetProperty<>(this, "highlights", FXCollections.observableSet());
    private final ObservableSet<GraphElement> unmodifiableHighlights =
            FXCollections.unmodifiableObservableSet(highlights);

    private Point2D cursor;
    private boolean dragged;

    private final ObjectProperty<Point2D> size =
            new PositionProperty(this, "size");

    private final Map<Long, Vertex> vertices = new TreeMap<>();
    private final Map<Long, Edge> edges = new TreeMap<>();

    private final InvalidationListener elementListener = observable -> markInvalid();

    public static void computeColors(DbMetadata metadata) {
        int numColors = metadata.nodeLabels().size();
        double step = 360.0 / numColors;
        List<Double> colors = IntStream.range(0, numColors)
                                       .boxed()
                                       .map(i -> Vertex.ORIGIN_HUE + i * step)
                                       .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(colors);
        Iterator<Double> colorIt = colors.iterator();
        colorTable = metadata.nodeLabels()
                         .stream()
                         .collect(Collectors.toMap(Function.identity(),
                                                   label -> Color.hsb(colorIt.next(),
                                                                      Vertex.ORIGIN_SATURATION,
                                                                      Vertex.ORIGIN_BRIGHTNESS)));
    }

    public Color getColor(String label) {
        return colorTable == null ?
               Vertex.DEFAULT_COLOR :
               colorTable.getOrDefault(label, Vertex.DEFAULT_COLOR);
    }

    public void setType(CanvasType type) {
        if (this.type != CanvasType.NONE || type == CanvasType.NONE)
            return;
        this.type = type;

        if (type == CanvasType.NAVIGABLE || type == CanvasType.MODIFIABLE) {
            setOnMouseDragged(e -> {
                if (cursor == null)
                    return;

                EventTarget target = e.getTarget();

                if (target == this) {
                    camera.translateInScreen(-(e.getX() - cursor.getX()), -(e.getY() - cursor.getY()));
                }
                else {
                    GraphElement currentElement = (GraphElement) ((javafx.scene.Node) target).getParent();
                    if (currentElement.isHighlighted()) {
                        Point2D delta = camera.screenToWorldScale(e.getX() - cursor.getX(),
                                                                  e.getY() -
                                                                  cursor.getY());  // To avoid redundant calculations
                        for (GraphElement element : getHighlights()) {
                            if (element instanceof Vertex)
                                element.translate(delta);
                        }
                    }
                    else if (currentElement instanceof Vertex)
                        currentElement.translateInScreen(new Point2D(e.getX() - cursor.getX(),
                                                                     e.getY() - cursor.getY()));
                }

                cursor = new Point2D(e.getX(), e.getY());
                dragged = true;
            });

            setOnMouseReleased(e -> {
                EventTarget target = e.getTarget();
                if (target != this && cursor != null && !dragged) {
                    clearHighlights();
                    addHighlight((GraphElement) ((javafx.scene.Node) target).getParent());
                }
                cursor = null;
                dragged = false;
            });

            setOnScroll(e -> {
                if (e.isShortcutDown())
                    camera.zoomWithPivot(e.getDeltaY() / UNIT_SCROLL, e.getX(), e.getY());
                else
                    camera.translateInScreen(-e.getDeltaX(), -e.getDeltaY());
            });

            getHighlights().addListener((SetChangeListener<GraphElement>) change -> {
                if (change.wasAdded()) {
                    change.getElementAdded().setHighlight(true);
                    if (change.getElementAdded() instanceof Vertex vertex)
                        vertex.toFront();
                }
                else
                    change.getElementRemoved().setHighlight(false);
            });

            if (type == CanvasType.NAVIGABLE) {  // Navigable only
                setOnKeyPressed(e -> {
                    if (e.isShortcutDown()) {
                        if (e.getCode() == KeyCode.A)
                            highlightAll();
                    }
                });

                setOnMousePressed(e -> {
                    EventTarget target = e.getTarget();

                    if (target == this) {  // Clicked on Canvas
                        clearHighlights();
                        cursor = new Point2D(e.getX(), e.getY());
                    }
                    else {  // Clicked on a GraphElement
                        GraphElement currentElement = (GraphElement) ((javafx.scene.Node) target).getParent();
                        if (e.isShortcutDown()) {
                            if (currentElement.isHighlighted())
                                removeHighlight(currentElement);
                            else
                                addHighlight(currentElement);
                        }
                        else {
                            if (!currentElement.isHighlighted()) {
                                clearHighlights();
                                addHighlight(currentElement);
                            }
                            cursor = new Point2D(e.getX(), e.getY());
                        }
                    }
                });
            }
            else {  // Modifiable
                setOnKeyPressed(e -> {
                    if (e.isShortcutDown()) {
                        if (e.getCode() == KeyCode.A)
                            highlightAll();
                    }
                    else {
                        if (e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE)
                            removeElements(getHighlights());
                    }
                });

                setOnMousePressed(e -> {
                    EventTarget target = e.getTarget();

                    if (target == this) {  // Clicked on Canvas
                        if (e.isShiftDown())
                            createVertex(new Point2D(e.getX(), e.getY()));
                        else {
                            clearHighlights();
                            cursor = new Point2D(e.getX(), e.getY());
                        }
                    }
                    else {  // Clicked on a GraphElement
                        GraphElement currentElement = (GraphElement) ((javafx.scene.Node) target).getParent();
                        Vertex lastVertex = nomineeVertex();
                        if (e.isShiftDown() && lastVertex != null && currentElement instanceof Vertex currentVertex)
                            createEdge(lastVertex, currentVertex, true);
                        else if (e.isShortcutDown()) {
                            if (currentElement.isHighlighted())
                                removeHighlight(currentElement);
                            else
                                addHighlight(currentElement);
                        }
                        else {
                            if (!currentElement.isHighlighted()) {
                                clearHighlights();
                                addHighlight(currentElement);
                            }
                            cursor = new Point2D(e.getX(), e.getY());
                        }
                    }
                });
            }
        }
    }

    public void loadGraph(Graph graph) {
        clearElements();

        Long maxId = Stream.concat(graph.getNodes().stream(), graph.getRelations().stream())
                           .map(Entity::getId)
                           .max(Long::compareTo)
                           .get();
        GraphElement.raiseIdTo(maxId);

        graph.getNodes().forEach(node -> vertices.put(node.getId(), new Vertex(this, node)));
        graph.getRelations().forEach(relation -> edges.put(relation.getId(), new Edge(this, relation)));

        getChildren().addAll(getEdges());
        getChildren().addAll(getVertices());

        getElements().forEach(element -> element.addListener(elementListener));
        markInvalid();

        // Compute the layout of the graph
        ForceDirectedPlacementStatic placement = new ForceDirectedPlacementStatic(this);
        placement.simulate(0);
        placement.layout();

        frameAllElements(false, false);
    }

    public void loadCanvas(Canvas other) {
        other.getVertices().forEach(vertex -> vertices.put(vertex.getElementId(), new Vertex(this, vertex)));
        other.getEdges().forEach(edge -> edges.put(edge.getElementId(), new Edge(this, edge)));

        getChildren().addAll(getEdges());
        getChildren().addAll(getVertices());

        getElements().forEach(element -> element.addListener(elementListener));
        markInvalid();
    }

    public void rotateSearch(Callable<Double> criteria) {
        double stepAngle = Math.PI / SEARCH_SAMPLES;
        double cos = Math.cos(stepAngle);
        double sin = Math.sin(stepAngle);

        int bestPos = 0;
        double min = Double.POSITIVE_INFINITY;
        try {
            min = criteria.call();
        }
        catch (Exception ignored) {}

        for (int i = 1; i < SEARCH_SAMPLES; ++i) {
            rotate(cos, sin);

            double current = Double.POSITIVE_INFINITY;
            try {
                current = criteria.call();
            }
            catch (Exception ignored) {}

            if (current < min) {
                bestPos = i;
                min = current;
            }
        }

        double diffAngle = (bestPos - SEARCH_SAMPLES + 1) * stepAngle;
        rotate(Math.cos(diffAngle), Math.sin(diffAngle));
    }

    private void rotate(double cos, double sin) {
        for (Vertex vertex : getVertices()) {
            double x = vertex.getX();
            double y = vertex.getY();
            vertex.setPosition(new Point2D(x * cos - y * sin, x * sin + y * cos));
        }
    }

    public void frameAllElements(boolean highlighting, boolean force) {
        navigateTo(getElements(), highlighting, force);
    }

    public void navigateTo(Collection<GraphElement> elements, boolean highlighting, boolean force) {
        clearHighlights();

        if (Math.signum(getWidth()) == 0.0 || Math.signum(getHeight()) == 0.0)
            return;

        Bounds bounds = computeBounds(elements);
        if (bounds == null)
            return;

        if (highlighting)
            addHighlights(elements);

        camera.fit(bounds, force);
    }

    public Bounds computeBounds() {
        return computeBounds(getElements());
    }

    public Bounds computeBounds(Collection<GraphElement> elements) {
        if (elements.isEmpty())
            return null;

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (GraphElement element : elements) {
            Bounds bounds = element.getBoundsInParent();

            double boundMinX = bounds.getMinX();
            double boundMinY = bounds.getMinY();
            double boundMaxX = bounds.getMaxX();
            double boundMaxY = bounds.getMaxY();

            if (boundMinX < minX)
                minX = boundMinX;
            if (boundMinY < minY)
                minY = boundMinY;
            if (boundMaxX > maxX)
                maxX = boundMaxX;
            if (boundMaxY > maxY)
                maxY = boundMaxY;
        }

        minX = camera.screenToWorldX(minX);
        minY = camera.screenToWorldY(minY);
        maxX = camera.screenToWorldX(maxX);
        maxY = camera.screenToWorldY(maxY);

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    private void createVertex(Point2D position) {
        Vertex vertex = new Vertex(this);
        vertex.setPositionInScreen(position);
        vertices.put(vertex.getElementId(), vertex);
        addElement(vertex);
    }

    private void createEdge(Vertex start, Vertex end, boolean directed) {
        Edge edge = new Edge(this, start, end, directed);
        edges.put(edge.getElementId(), edge);
        addElement(edge);
        edge.toBack();
    }

    public Collection<GraphElement> getElements() {
        return getChildren().stream().map(node -> (GraphElement) node).toList();
    }

    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    public Collection<Edge> getEdges() {
        return edges.values();
    }

    public Vertex getVertex(long id) {
        return vertices.get(id);
    }

    public Edge getEdge(long id) {
        return edges.get(id);
    }

    // Should only be called in Edge:erase
    public void erase(Vertex vertex) {
        vertices.remove(vertex.getElementId());
    }

    // Should only be called in Vertex:erase
    public void erase(Edge edge) {
        edges.remove(edge.getElementId());
    }

    private void addElement(GraphElement element) {
        element.addListener(elementListener);
        getChildren().add(element);
        clearHighlights();
        addHighlight(element);
        markInvalid();
    }

    private void removeElement(GraphElement element) {
        element.removeListener(elementListener);
        getChildren().remove(element);
        element.erase();
        clearHighlights();
        markInvalid();
    }

    private void removeElements(Collection<GraphElement> elements) {
        elements.forEach(element -> element.removeListener(elementListener));
        getChildren().removeAll(elements);
        elements.forEach(GraphElement::erase);
        clearHighlights();
        markInvalid();
    }

    public void clearElements() {
        getElements().forEach(element -> element.removeListener(elementListener));
        vertices.clear();
        edges.clear();
        getChildren().clear();
        clearHighlights();
        markInvalid();
    }

    public ObservableSet<GraphElement> getHighlights() {
        return unmodifiableHighlights;
    }

    public GraphElement getSingleHighlight() {
        Set<GraphElement> elements = getHighlights();
        return elements.size() == 1 ?
               elements.iterator().next() : null;
    }

    // Returns the awaiting vertex for edge formation, returns null if no or multiple vertices are highlighted
    private Vertex nomineeVertex() {
        return (getSingleHighlight() instanceof Vertex vertex) ?
               vertex : null;
    }

    public void addHighlight(GraphElement element) {
        highlights.add(element);
    }
    public void addHighlights(Collection<GraphElement> elements) {
        highlights.addAll(elements);
    }
    public void highlightAll() {
        addHighlights(getChildren().stream().map(node -> (GraphElement) node).toList());
    }

    public void removeHighlight(GraphElement element) {
        highlights.remove(element);
    }

    public void clearHighlights() {
        highlights.clear();
    }

    public ReadOnlyObjectProperty<Point2D> sizeProperty() {
        return size;
    }
    public Point2D getSize() {
        return size.get();
    }

    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        Point2D newSize = new Point2D(width, height);
        if (!newSize.equals(getSize()))
            size.set(newSize);
    }

    protected void markInvalid() {
        if (valid) {
            valid = false;
            invalidated();
        }
    }

    private void invalidated() {
        listeners.forEach(listener -> listener.invalidated(this));
        valid = true;
    }

    @Override
    public void addListener(InvalidationListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        listeners.remove(listener);
    }
}
