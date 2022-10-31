package hkust.edu.visualneo.utils.frontend;

import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.Collection;
import java.util.List;

public class Canvas extends Pane {

    private static final double UNIT_SCROLL = 32.0;

    private static final byte SHIFT       = 0b00000001;
    private static final byte CTRL        = 0b00000010;
    private static final byte WAITING_END = 0b00000100;

    private byte stateFlag;

    public final OrthogonalCamera camera = new OrthogonalCamera(this);

    private final SetProperty<GraphElement> highlightElements =
            new SimpleSetProperty<>(FXCollections.observableSet());

    private Point2D cursor;
    private boolean dragged;

    public Canvas() {
        super();

        requestFocus();

        getChildren().addListener((ListChangeListener<Node>) c -> {
            while (c.next()) {
                if (c.wasRemoved())
                    c.getRemoved().forEach(element -> ((GraphElement) element).erase());
            }
        });

        highlightElements.addListener((SetChangeListener<GraphElement>) c -> {
            if (c.wasAdded())
                c.getElementAdded().setHighlight(true);
            else
                c.getElementRemoved().setHighlight(false);
        });

        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.isControlDown()) {
                if (e.getCode() == KeyCode.A)
                    getChildren().forEach(node -> addHighlight((GraphElement) node));
            }
            else {
                if (e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE)
                    removeElements(getHighlights());
            }
        });

        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
//            EventTarget target = e.getTarget();
//
//            if (target == this) {  // Clicked on Canvas
//                if (e.isShiftDown())
//                    createVertex(e.getX(), e.getY());
//                else
//                    clearHighlights();
//            }
//            else {  // Clicked on a GraphElement
//                GraphElement currentElement = (GraphElement) ((Node) target).getParent();
//                Vertex lastVertex = nominalVertex();
//                if (e.isShiftDown() && lastVertex != null && currentElement instanceof Vertex currentVertex)
//                    createEdge(lastVertex, currentVertex);
//                else if (e.isControlDown()) {
//                    if (currentElement.isHighlighted())
//                        removeHighlight(currentElement);
//                    else
//                        addHighlight(currentElement);
//                }
//                else {
//                    clearHighlights();
//                    addHighlight(currentElement);
//                }
//            }
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
                Vertex lastVertex = nominalVertex();
                if (e.isShiftDown() && lastVertex != null && currentElement instanceof Vertex currentVertex)
                    createEdge(lastVertex, currentVertex);
                else if (e.isControlDown()) {
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

        setOnMouseDragged(e -> {
            if (cursor == null)
                return;

            EventTarget target = e.getTarget();

            if (target == this) {
                camera.translate(-(e.getX() - cursor.getX()), -(e.getY() - cursor.getY()));
            }
            else {
                GraphElement currentElement = (GraphElement) ((Node) target).getParent();
                if (currentElement.isHighlighted()) {
                    Point2D delta = camera.canvasToViewScale(e.getX() - cursor.getX(), e.getY() - cursor.getY());  // To avoid redundant calculations
                    for (GraphElement element : getHighlights()) {
                        if (element instanceof Vertex)
                            element.translateInView(delta);
                    }
                }
                else if (currentElement instanceof Vertex)
                    currentElement.translate(e.getX() - cursor.getX(), e.getY() - cursor.getY());
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
            if (e.isControlDown())
                camera.zoom(e.getDeltaY() / UNIT_SCROLL, e.getX(), e.getY());
            else
                camera.translate(-e.getDeltaX(), -e.getDeltaY());
        });
    }

    public static Canvas buildCanvas(Pane parent) {
        Canvas canvas = new Canvas();
        parent.getChildren().add(canvas);

        canvas.prefWidthProperty().bind(parent.widthProperty());
        canvas.prefHeightProperty().bind(parent.heightProperty());
        canvas.toBack();
        return canvas;
    }

    private void createVertex(double x, double y) {
        addElement(new Vertex(this, x, y));
    }

    private void createEdge(Vertex start, Vertex end) {
        addElement(new Edge(this, start, end, false));  // TODO: Modify this
    }

    private Vertex nominalVertex() {
        List<GraphElement> elements = getHighlights();
        return (elements.size() == 1 && elements.get(0) instanceof Vertex vertex) ?
               vertex : null;
    }

    private boolean hasFlags(byte... flags) {
        byte stateFlag = 0;
        for (byte flag : flags)
            stateFlag += flag;
        return (this.stateFlag & stateFlag) == stateFlag;
    }

    public List<GraphElement> getElements() {
        return getChildren()
                .stream()
                .map(element -> (GraphElement) element)
                .toList();
    }
    public void addElement(GraphElement element) {
        getChildren().add(element);
        clearHighlights();
        addHighlight(element);
    }

    public void removeElement(GraphElement element) {
        getChildren().remove(element);
        clearHighlights();
    }

    public void removeElements(Collection<GraphElement> elements) {
        getChildren().removeAll(elements);
        clearHighlights();
    }

    public void clearElements() {
        getChildren().clear();
        clearHighlights();
    }

    public List<GraphElement> getHighlights() {
        return highlightElements.stream().toList();
    }
    public GraphElement singleHighlight() {
        List<GraphElement> elements = getHighlights();
        return elements.size() == 1 ?
               elements.get(0) : null;
    }
    public void addHighlight(GraphElement e) {
        highlightElements.add(e);
    }
    public void removeHighlight(GraphElement e) {
        highlightElements.remove(e);
    }
    public void clearHighlights() {
        highlightElements.clear();
    }
}
