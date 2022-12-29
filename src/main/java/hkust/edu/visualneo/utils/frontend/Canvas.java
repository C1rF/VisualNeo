package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.utils.backend.Graph;
import hkust.edu.visualneo.utils.backend.Node;
import hkust.edu.visualneo.utils.backend.Relation;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.util.*;

public class Canvas extends Pane {

    public enum CanvasType {
        NONE,
        STATIC,
        NAVIGABLE,
        MODIFIABLE
    }

    private static final double UNIT_SCROLL = 32.0;

    private CanvasType type = CanvasType.NONE;

    public final OrthogonalCamera camera = new OrthogonalCamera(this);

    private final ObservableSet<GraphElement> highlights =
            new SimpleSetProperty<>(this, "highlights", FXCollections.observableSet());
    private final ObservableSet<GraphElement> unmodifiableHighlights =
            FXCollections.unmodifiableObservableSet(highlights);

    private Point2D cursor;
    private boolean dragged;

    private final Map<Long, Vertex> vertices = new TreeMap<>();
    private final Map<Long, Edge> edges = new TreeMap<>();

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
                            new ArrayList<>(getChildren()).forEach(node -> addHighlight((GraphElement) node));
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
                            new ArrayList<>(getChildren()).forEach(node -> addHighlight((GraphElement) node));
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
        // Compute the layout of the graph
        ForceDirectedPlacement placement =
                new ForceDirectedPlacement(graph, new Point2D(this.getWidth(), this.getHeight()), 10000, 0.8);
        Map<Long, Point2D> layout = placement.layout();
        // Create the vertices and edges
        for (Node node : graph.nodes())
            createVertex(node, layout.get(node.getId()));
        for (Relation relation : graph.relations())
            createEdge(relation);
    }

    public void simulateLayout(Graph graph) {
        for (Node node : graph.nodes())
            createVertex(node, Point2D.ZERO);
        for (Relation relation : graph.relations())
            createEdge(relation);
        ForceDirectedPlacement placement =
                new ForceDirectedPlacement(graph, new Point2D(this.getWidth(), this.getHeight()), 10000, 0.8);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int iterNum = 0;

            public void run() {
                placement.simulate(getVertices(), iterNum++);
            }
        }, 0, 300);
    }

    private void createVertex(Point2D position) {
        Vertex vertex = new Vertex(this, position);
        vertices.put(vertex.getElementId(), vertex);
        addElement(vertex);
    }

    private void createVertex(Node node, Point2D position) {
        Vertex vertex = new Vertex(this, node, position);
        vertices.put(vertex.getElementId(), vertex);
        addElement(vertex);
    }

    private void createEdge(Vertex start, Vertex end, boolean directed) {
        Edge edge = new Edge(this, start, end, directed);
        edges.put(edge.getElementId(), edge);
        addElement(edge);
        edge.toBack();
    }

    private void createEdge(Relation relation) {
        Edge edge = new Edge(this, relation);
        edges.put(edge.getElementId(), edge);
        addElement(edge);
        edge.toBack();
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

    public void addElement(GraphElement element) {
        getChildren().add(element);
        clearHighlights();
        addHighlight(element);
    }

    public void removeElement(GraphElement element) {
        getChildren().remove(element);
        element.erase();
        clearHighlights();
    }

    public void removeElements(Collection<GraphElement> elements) {
        getChildren().removeAll(elements);
        elements.forEach(GraphElement::erase);
        clearHighlights();
    }

    public void clearElements() {
        getChildren().clear();
        clearHighlights();
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

    public void addHighlight(GraphElement e) {
        highlights.add(e);
    }

    public void removeHighlight(GraphElement e) {
        highlights.remove(e);
    }

    public void clearHighlights() {
        highlights.clear();
    }
}
