package hkust.edu.visualneo.utils.frontend;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.PI;

public class Vertex extends GraphElement {

    // Radius of the Vertex
    static final double VERTEX_RADIUS = 25.0;
    private static final double DEFAULT_STROKE_WIDTH = 1.0;
    private static final double HIGHLIGHT_STROKE_WIDTH = 2.0;
    private static final Color DEFAULT_COLOR = Color.DARKGREY;
    private static final Color HIGHLIGHT_COLOR = Color.BLACK;
    private static final Color CIRCLE_COLOR = Color.LIGHTGRAY;
    // record the offset (only used for move the object)
    double anchorX, anchorY;
    // The shape contains a circle and a text(not necessary) on top of it

    private final Set<Edge> edges = new LinkedHashSet<>();

    private final DoubleProperty selfLoopAngle =
            new SimpleDoubleProperty(this, "selfLoopAngle", 0.0);

    // Constructor
    public Vertex(Canvas canvas, double x, double y) {
        super(canvas);
        setPosition(x, y);

        initializeGraphics();
//        // Set event handler
//        MouseEventHandler handler = new MouseEventHandler();
//        addEventHandler(MouseEvent.ANY, handler);
        // For debugging
        System.out.println("A new Vertex is created.");
    }

    @Override
    protected void initializeGraphics() {
        super.initializeGraphics();

        shape = new Circle(VERTEX_RADIUS, CIRCLE_COLOR);
        getChildren().add(shape);

        highlightProperty().addListener((observable, oldValue, newValue) -> {
            shape.setStrokeWidth(newValue ? HIGHLIGHT_STROKE_WIDTH : DEFAULT_STROKE_WIDTH);
            shape.setStroke(newValue ? HIGHLIGHT_COLOR : DEFAULT_COLOR);
        });

        selfLoopAngleProperty().bind(Bindings.createDoubleBinding(
                () -> {
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
                            return (angles.get(idx) + angles.get(idx - 1)) / 2;
                        }
                    }
                    return -PI / 2;
                },
                neighbors()
                        .stream()
                        .map(GraphElement::positionProperty).toList().toArray(new ObjectProperty[0])));  // TODO: ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR
    }

    @Override
    protected void initializeHandlers() {
        super.initializeHandlers();
//        setOnMousePressed(this::pressed);
        setOnMouseDragged(this::dragged);
        setOnMouseReleased(this::released);
    }

    /**
     * Request focus when pressed
     */
    private void pressed(MouseEvent e) {
        anchorX = e.getX();
        anchorY = e.getY();
    }

    /**
     * Move the Vertex when dragged
     */
    private void dragged(MouseEvent e) {
//        if (e.isShiftDown()) return;
        System.out.println("Vertex Dragged");
        getScene().setCursor(Cursor.CLOSED_HAND);
//        translate(e.getX() - anchorX, e.getY() - anchorY);
//        anchorX = e.getX();
//        anchorY = e.getY();
    }
    
    private void released(MouseEvent e) {
        getScene().setCursor(Cursor.HAND);
    }

//    /**
//     * Event handler to handle all the MouseEvents
//     */
//    public class MouseEventHandler implements EventHandler<MouseEvent> {
//        @Override
//        public void handle(MouseEvent event) {
//            event.consume();
//            if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
//                pressed(event);
//            else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED)
//                dragged(event);
//            else if (event.getEventType() == MouseEvent.MOUSE_ENTERED)
//                getScene().setCursor(Cursor.HAND);
//            else if (event.getEventType() == MouseEvent.MOUSE_EXITED)
//                getScene().setCursor(Cursor.DEFAULT);
//            else if (event.getEventType() == MouseEvent.MOUSE_RELEASED)
//                getScene().setCursor(Cursor.HAND);
//        }
//    }

    public boolean hasSelfLoop() {
        for (Edge edge : edges)
            if (equals(edge.other(this)))
                return true;
        return false;
    }

    public Set<Vertex> neighbors() {
        return edges
                .stream()
                .map(edge -> edge.other(this))
                .collect(Collectors.toSet());
    }

    public Set<Edge> edgesBetween(Vertex other) {
        if (other == null)
            return Collections.emptySet();

        return edges
                .stream()
                .filter(edge -> other.equals(edge.other(this)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void updateLoops() {
        updateEdgesBetween(this);
    }

    public void updateEdgesBetween(Vertex other) {
        Set<Edge> edges = edgesBetween(other);
        int numEdges = edges.size();
        int edgeIdx = 0;
        for (Edge edge : edges) {
            edge.updateIdx(edgeIdx, numEdges);
            edge.update();
            ++edgeIdx;
        }
    }

    public void updateAllEdges() {
        edges.forEach(Edge::update);
    }

    public List<Double> computeAngles() {
        return neighbors()
                .stream()
                .filter(other -> !other.equals(this))
                .map(other -> Point2D.ZERO.angle(other.getPosition().subtract(getPosition())))
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void attach(Edge new_edge) {
        edges.add(new_edge);

        if (equals(new_edge.startVertex))
            updateEdgesBetween(new_edge.endVertex);

        updateLoops();
    }

    public void detach(Edge edge_to_detach) {
        edges.remove(edge_to_detach);

        if (equals(edge_to_detach.startVertex))
            updateEdgesBetween(edge_to_detach.endVertex);

        updateLoops();
    }

    public DoubleProperty selfLoopAngleProperty() {
        return selfLoopAngle;
    }
    public double getSelfLoopAngle() {
        return selfLoopAngleProperty().get();
    }

    @Override
    public void erase() {
        Set<Edge> edgesCopy = new HashSet<>(edges);
        edgesCopy.forEach(Edge::erase);
    }

    public Set<Edge> getAllEdges() {
        return edges;
    }

    /**
     * Convert the Vertex Object to a String that contains all the information
     */
    @Override
    public String toText() {
        String[] temp = new String[]{positionProperty().toString(),
                                     getLabel()};
        return (String) StringUtil.join(" ", Arrays.asList(temp));
    }


//    public class VertexEvent extends Event {
//
//        @Serial
//        private static final long serialVersionUID = 1L;
//
//        public static final EventType<VertexEvent> ANY = new EventType<>("ANY");
//        public static final EventType<VertexEvent> MOVED = new EventType<>(ANY, "MOVED");
//        public static final EventType<VertexEvent> NEIGHBOR_MOVED = new EventType<>(ANY, "NEIGHBOR_MOVED");
//
//        public VertexEvent(Object source, EventTarget target, EventType<? extends VertexEvent> eventType) {
//            super(Objects.requireNonNull(source), Objects.requireNonNull(target), eventType);
//        }
//
//        @Override
//        public VertexEvent copyFor(Object newSource, EventTarget newTarget) {
//            return (VertexEvent) super.copyFor(newSource, newTarget);
//        }
//
//        @Override
//        public EventType<? extends VertexEvent> getEventType() {
//            return (EventType<? extends VertexEvent>) super.getEventType();
//        }
//    }
}
