package hkust.edu.visualneo.utils.frontend;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.scene.transform.Scale;
import org.neo4j.driver.Value;

import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.PI;

public abstract class GraphElement extends Group implements Comparable<GraphElement> {

    protected static long currentId = 0;

    protected final Canvas canvas;

    private final long id;

    protected static final Color HOVER_COLOR = Color.color(0.0f, 0.1f, 0.4f, 0.1f);
    protected static final Color HIGHLIGHT_COLOR = Color.color(0.0f, 0.1f, 0.4f, 0.3f);

    // String of the label
    private final StringProperty label =
            new SimpleStringProperty(this, "label", null);
    // Properties attached to the element(node/relation)
    protected Map<String, Value> properties;
    // Whether the element is highlighted
    private final BooleanProperty highlight =
            new SimpleBooleanProperty(this, "highlight", false);
    // Position of the element
    private final ObjectProperty<Point2D> position =
            new SimpleObjectProperty<>(this, "position", Point2D.ZERO);
    protected Shape shape;
    protected Shape highlightShape;
    // Label shown on the GraphElement
    protected Text text;

    GraphElement(Canvas canvas) {
        this(canvas, currentId++);
    }

    GraphElement(Canvas canvas, long id) {
        this.canvas = canvas;
        this.id = id;
        properties = new TreeMap<>();
        initializeHandlers();
    }

    public void addProperty(String name, Value val) {
        properties.put(name, val);
    }

    public Map<String, Value> getProp() {
        return properties;
    }

    public StringProperty labelProperty() {
        return label;
    }

    public String getLabel() {
        return labelProperty().get();
    }

    public boolean hasLabel() {
        return labelProperty().get() != null;
    }

    public void setLabel(String label) {
        // Add the new label
        System.out.println("Add Label: " + label);
        labelProperty().set(label);
    }

    public BooleanProperty highlightProperty() {
        return highlight;
    }

    public boolean isHighlighted() {
        return highlightProperty().get();
    }

    public void setHighlight(boolean highlight) {
        highlightProperty().set(highlight);
    }

    public ObjectProperty<Point2D> positionProperty() {
        return position;
    }

    public Point2D getPosition() {
        return positionProperty().get();
    }

    public double getX() {
        return positionProperty().get().getX();
    }

    public double getY() {
        return positionProperty().get().getY();
    }

    public long getElementId() { return id; }

    public void setPosition(Point2D p) {
        if (!getPosition().equals(p))  // To function with ObjectProperty
            positionProperty().set(p);
    }

    public void translate(Point2D delta) {
        setPosition(getPosition().add(delta));
    }

    public void setPositionInScreen(Point2D p) {
        setPosition(camera().screenToWorld(p));
    }

    public void translateInScreen(Point2D p) {
        translate(camera().screenToWorldScale(p));
    }


    protected void initializeGraphics() {
        layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> camera().worldToScreenX(getX()),
                positionProperty(), camera().positionProperty(), camera().ratioProperty()));
        layoutYProperty().bind(Bindings.createDoubleBinding(
                () -> camera().worldToScreenY(getY()),
                positionProperty(), camera().positionProperty(), camera().ratioProperty()));

        Scale scale = new Scale();
        scale.xProperty().bind(camera().ratioProperty());
        scale.yProperty().bind(camera().ratioProperty());
        getTransforms().add(scale);

        text = new Text();
        text.setBoundsType(TextBoundsType.VISUAL);
        getChildren().add(text);

        text.textProperty().bind(labelProperty());
        text.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            text.setTranslateX(-newValue.getWidth() / 2);
            text.setTranslateY(newValue.getHeight() / 2);
        });

        setHighlight(false);
    }

    protected void initializeHandlers() {
        setOnMouseEntered(this::entered);
        setOnMouseExited(this::exited);
        setOnMouseDragged(this::dragged);
        setOnMouseReleased(this::released);
    }

    public abstract void erase();

    protected void entered(MouseEvent e) {
        getScene().setCursor(Cursor.HAND);
    }
    protected void exited(MouseEvent e) {
        getScene().setCursor(Cursor.DEFAULT);
    }
    protected void dragged(MouseEvent e) {}
    protected void released(MouseEvent e) {}

    public abstract String toText();

    protected OrthogonalCamera camera() {
        return canvas.camera;
    }

    public String propertyToText(){
        if(properties.isEmpty()) return "null";
        StringBuilder propertyText = new StringBuilder();
        for (Map.Entry<String,Value> entry : properties.entrySet())
            propertyText
                    .append(entry.getKey())
                    .append(":")
                    .append(entry.getValue().toString().replaceAll("\"", ""))
                    .append(" ");
        return propertyText.toString().trim();
    }

    public static double angle(Vertex start, Vertex end) {
        Point2D delta = end.getPosition().subtract(start.getPosition());
        return delta.getX() == 0.0 ?
                delta.getY() > 0.0 ? PI / 2 : -PI / 2 :
                Math.atan2(delta.getY(), delta.getX());
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));  // Implementation by Neo4j
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (!getClass().equals(other.getClass()))
            return false;
        return id == ((GraphElement) other).id;
    }

    @Override
    public int compareTo(GraphElement other) {
        return (int) (id - other.id);
    }

    public static void raiseIdTo(Long id) {
        currentId = id + 1;
    }
}
