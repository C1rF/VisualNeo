package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.utils.backend.Graph;
import hkust.edu.visualneo.utils.backend.Relation;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.scene.Node;
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

    private final Map<Long, Vertex> vertices = new HashMap<>();
    private final Map<Long, Edge> edges = new HashMap<>();

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
                    GraphElement currentElement = (GraphElement) ((Node) target).getParent();
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
                        currentElement.translateInScreen(e.getX() - cursor.getX(), e.getY() - cursor.getY());
                }

                cursor = new Point2D(e.getX(), e.getY());
                dragged = true;
            });

            setOnMouseReleased(e -> {
                EventTarget target = e.getTarget();
                if (target != this && cursor != null && !dragged) {
                    clearHighlights();
                    addHighlight((GraphElement) ((Node) target).getParent());
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

            getHighlights().addListener((SetChangeListener<GraphElement>) c -> {
                if (c.wasAdded()) {
                    c.getElementAdded().setHighlight(true);
                    if (c.getElementAdded() instanceof Vertex vertex)
                        vertex.toFront();
                }
                else
                    c.getElementRemoved().setHighlight(false);
            });

            if (type == CanvasType.NAVIGABLE) {
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
                        GraphElement currentElement = (GraphElement) ((Node) target).getParent();
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
            else {
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
                            createVertex(e.getX(), e.getY());
                        else {
                            clearHighlights();
                            cursor = new Point2D(e.getX(), e.getY());
                        }
                    }
                    else {  // Clicked on a GraphElement
                        GraphElement currentElement = (GraphElement) ((Node) target).getParent();
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

    public void loadGraph(Graph graph){
        // Compute the layout of the graph
        ForceDirectedPlacement placement = new ForceDirectedPlacement(graph, new Point2D(this.getWidth(), this.getHeight()), 10000, 0.5, 0.9);
        Map<Long, Point2D> layout = placement.layout();
        // Create the vertices and edges
        for(hkust.edu.visualneo.utils.backend.Node node : graph.nodes()){
            createVertex(node, layout.get(node.getId()));
        }
        for(Relation relation : graph.relations()){
            createEdge(relation);
        }
    }

    private void createVertex(double x, double y) {
        Vertex vertex = new Vertex(this, x, y);
        vertices.put(vertex.id(), vertex);
        addElement(vertex);
    }

    private void createEdge(Vertex start, Vertex end, boolean directed) {
        Edge edge = new Edge(this, start, end, directed);
        edges.put(edge.id(), edge);
        addElement(edge);
        edge.toBack();
    }

    private void createVertex(hkust.edu.visualneo.utils.backend.Node node, Point2D position){
        Vertex vertex = new Vertex(this, node, position);
        vertices.put(vertex.id(), vertex);
        addElement(vertex);
    }

    private void createEdge(Relation relation) {
        Edge edge = new Edge(this, relation);
        edges.put(edge.id(), edge);
        addElement(edge);
        edge.toBack();
    }

    public List<GraphElement> getElements() {
        return getChildren()
                .stream()
                .map(element -> (GraphElement) element)
                .toList();
    }
    public List<Vertex> getVertices() {
//        return getChildren()
//                .stream()
//                .filter(element -> element instanceof Vertex)
//                .map(element -> (Vertex) element)
//                .toList();
        return vertices.values().stream().toList();
    }
    public List<Edge> getEdges() {
//        return getChildren()
//                .stream()
//                .filter(element -> element instanceof Edge)
//                .map(element -> (Edge) element)
//                .toList();
        return edges.values().stream().toList();
    }

    public Vertex getVertexById(long id) {
        return vertices.get(id);
    }

    public Edge getEdgeById(long id) {
        return edges.get(id);
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

    public void removeFromVertices(Long id, Vertex vertex){
        vertices.remove(id, vertex);
    }
    public void removeFromEdges(Long id, Edge edge){
        edges.remove(id, edge);
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
