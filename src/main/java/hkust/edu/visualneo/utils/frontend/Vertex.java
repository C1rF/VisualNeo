package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.VisualNeoController;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class Vertex extends GraphElement {

    // Radius of the Vertex
    static final double VERTEX_RADIUS = 25.0;

    // record the position of center of the circle
    double x, y;
    private static final Color CIRCLE_COLOR = Color.LIGHTGRAY;
    // record the offset (only used for move the object)
    double offX, offY;
    // The shape contains a circle and a text(not necessary) on top of it
    private Circle circle;

    private final Set<Edge> edges = new LinkedHashSet<>();

    // Constructor
    public Vertex(VisualNeoController controller, double x, double y) {
        super(controller);
        this.x = x;
        this.y = y;
        // Initialize the shape
        initializeShape();
        setPos();
        // Set event handler
        MouseEventHandler handler = new MouseEventHandler();
        addEventHandler(MouseEvent.ANY, handler);
        // For Testing
        System.out.println("A new Vertex is created.");
    }

    @Override
    protected void initializeShape() {
        super.initializeShape();
        // Initialize the circle
        circle = new Circle(VERTEX_RADIUS, CIRCLE_COLOR);
        circle.setStrokeWidth(0.5);
        // Add circle and label to Vertex Group (Display)
        getChildren().add(circle);
        circle.toFront();
        label_displayed.toFront();
    }

    @Override
    public void becomeHighlight() {
        circle.setStrokeWidth(2);
        circle.setStroke(new Color(0, 0, 0, 1));
    }

    @Override
    public void removeHighlight() {
        circle.setStrokeWidth(0.5);
    }

    /**
     * Request focus when pressed
     */
    @Override
    protected void pressed(MouseEvent m) {
        GraphElement current_highlight = controller.getHighlight();
        if (m.isShiftDown() && current_highlight instanceof Vertex last_vertex) {
            // Meaning that we need to create an edge from current_highlight to this
            controller.createEdgeBetween(last_vertex, this);
            System.out.println("Edge CREATED");
        } else {
            // We simply select the vertex
            System.out.println("Vertex SELECTED");
            offX = m.getX();
            offY = m.getY();
            requestFocus();
        }
    }

    /**
     * Move the Vertex when dragged
     */
    public void dragged(MouseEvent m) {
        if (m.isShiftDown()) return;
        System.out.println("Vertex Dragged");
        getScene().setCursor(Cursor.CLOSED_HAND);
        // (m.getX() - offX) contains the minor changes of x coordinate
        // (m.getY() - offY) contains the minor changes of y coordinate
        x += m.getX() - offX; // keep updating the coordinate
        y += m.getY() - offY; // keep updating the coordinate
        setPos();
    }

    /**
     * Set the position of the group
     */
    public void setPos() {
        setLayoutX(x);
        setLayoutY(y);

        updateAllEdges();
        edges.forEach(edge -> {
            Vertex other = edge.other(this);
            if (!equals(other))
                other.updateLoops();
        });
    }

    /**
     * Event handler to handle all the MouseEvents
     */
    public class MouseEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            event.consume();
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
                pressed(event);
            else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED)
                dragged(event);
            else if (event.getEventType() == MouseEvent.MOUSE_ENTERED)
                getScene().setCursor(Cursor.HAND);
            else if (event.getEventType() == MouseEvent.MOUSE_EXITED)
                getScene().setCursor(Cursor.DEFAULT);
            else if (event.getEventType() == MouseEvent.MOUSE_RELEASED)
                getScene().setCursor(Cursor.HAND);
        }
    }

    public boolean hasLoop() {
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

    public double angleBetween(Vertex other) {
        return x == other.x ?
                y <= other.y ?
                        Math.PI / 2 :
                        3 * Math.PI / 2 :
                Math.atan2(other.y - y, other.x - x);
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
                .map(this::angleBetween)
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

    @Override
    public void erase() {
        Set<Edge> edges_copy = new HashSet<>(edges);
        edges_copy.forEach(edge -> edge.erase());
        ((Pane) getParent()).getChildren().remove(this);
        controller.listOfVertices.remove(this);
    }

    public Set<Edge> getAllEdges() {
        return edges;
    }

    /**
     * Convert the Vertex Object to a String that contains all the information
     */
    @Override
    public String toText() {
        String[] temp = new String[]{String.valueOf(x),
                String.valueOf(y),
                label_displayed.getText(),
                label_displayed.getText()};
        return (String) StringUtil.join(" ", Arrays.asList(temp));
    }

}
