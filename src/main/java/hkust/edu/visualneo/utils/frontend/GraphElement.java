package hkust.edu.visualneo.utils.frontend;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.neo4j.driver.Value;

import java.util.Map;
import java.util.TreeMap;

public abstract class GraphElement extends Group {

    protected final Canvas canvas;

    // String of the label
    private final StringProperty label =
            new SimpleStringProperty(this, "label", null);
    // Properties attached to the element(node/relation)
    Map<String, Value> properties;

    public final BooleanProperty highlight =
            new SimpleBooleanProperty(this, "highlight", false);

    private final ObjectProperty<Point2D> position =
            new SimpleObjectProperty<>(this, "position", Point2D.ZERO);

    protected Shape shape;
    // Label shown on the GraphElement
    protected Text text;

    GraphElement(Canvas canvas) {
        this.canvas = canvas;
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

    public BooleanProperty highlightProperty(){
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
    public void setPositionInView(Point2D p) {
        if (!getPosition().equals(p))  // To function with ObjectProperty
            positionProperty().set(p);
    }
    public void setPositionInView(double x, double y) {
        setPositionInView(new Point2D(x, y));
    }
    public void translateInView(Point2D delta) {
        setPositionInView(getPosition().add(delta));
    }
    public void translateInView(double deltaX, double deltaY) {
        translateInView(new Point2D(deltaX, deltaY));
    }

    public void setPosition(Point2D p) {
        setPositionInView(camera().canvasToView(p));
    }
    public void setPosition(double x, double y) {
        setPosition(new Point2D(x, y));
    }
    public void translate(Point2D p) {
        translateInView(camera().canvasToViewScale(p));
    }
    public void translate(double deltaX, double deltaY) {
        translate(new Point2D(deltaX,  deltaY));
    }


    protected void initializeGraphics() {
        layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> (getX() - camera().getX()) * camera().getRatio(),
                positionProperty(), camera().positionProperty(), camera().ratioProperty()));
        layoutYProperty().bind(Bindings.createDoubleBinding(
                () -> (getY() - camera().getY()) * camera().getRatio(),
                positionProperty(), camera().positionProperty(), camera().ratioProperty()));

        scaleXProperty().bind(camera().ratioProperty());
        scaleYProperty().bind(camera().ratioProperty());

        text = new Text();
        text.setBoundsType(TextBoundsType.VISUAL);
        getChildren().add(text);

        text.textProperty().bind(labelProperty());
        text.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            text.setTranslateX(-newValue.getWidth() / 2);
            text.setTranslateY(newValue.getHeight() / 2);
        });
    }

    protected void initializeHandlers() {
//        setOnMouseClicked(this::clicked);
        setOnMouseEntered(this::entered);
        setOnMouseExited(this::exited);
    }

    public abstract void erase();

    protected void clicked(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY)
            setHighlight(!isHighlighted());
    }
    protected void entered(MouseEvent e) {
        getScene().setCursor(Cursor.HAND);
    }
    protected void exited(MouseEvent e) {
        getScene().setCursor(Cursor.DEFAULT);
    }

    public abstract String toText();

    protected OrthogonalCamera camera() {
        return canvas.camera;
    }
}
