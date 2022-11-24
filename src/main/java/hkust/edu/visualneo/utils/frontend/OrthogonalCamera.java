package hkust.edu.visualneo.utils.frontend;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;

import java.util.Objects;

public class OrthogonalCamera {

    private static final double DEFAULT_RATIO = 1.0;
    private static final double MIN_RATIO = 0.5;
    private static final double MAX_RATIO  = 1.5;
    private static final double UNIT_RATIO = 0.05;

    private final Canvas canvas;

    // View port size to canvas size
    private final DoubleProperty ratio =
            new SimpleDoubleProperty(this, "ratio", DEFAULT_RATIO);
    private final DoubleProperty inverseRatio =
            new SimpleDoubleProperty(this, "inverseRatio", 1 / DEFAULT_RATIO);

    private final DoubleProperty viewWidth =
            new SimpleDoubleProperty(this, "viewWidth", 0.0);
    private final DoubleProperty viewHeight =
            new SimpleDoubleProperty(this, "viewHeight", 0.0);

    private final ObjectProperty<Point2D> position =
            new SimpleObjectProperty<>(this, "position", Point2D.ZERO);

    public OrthogonalCamera(Canvas canvas) {
        this.canvas = Objects.requireNonNull(canvas);

        inverseRatioProperty().bind(Bindings.divide(1, ratioProperty()));

        viewWidthProperty().bind(Bindings.multiply(canvas.widthProperty(), inverseRatioProperty()));
        viewHeightProperty().bind(Bindings.multiply(canvas.heightProperty(), inverseRatioProperty()));
    }

    public double viewToCanvasScale(double delta) {
        return delta * getRatio();
    }
    public double canvasToViewScale(double delta) {
        return delta * getInverseRatio();
    }
    public Point2D viewToCanvasScale(Point2D p) {
        return p.multiply(getRatio());
    }
    public Point2D viewToCanvasScale(double deltaX, double deltaY) {
        return viewToCanvasScale(new Point2D(deltaX, deltaY));
    }
    public Point2D canvasToViewScale(Point2D p) {
        return p.multiply(getInverseRatio());
    }
    public Point2D canvasToViewScale(double deltaX, double deltaY) {
        return canvasToViewScale(new Point2D(deltaX, deltaY));
    }
    public Point2D viewToCanvas(Point2D p) {
        return p.subtract(getPosition()).multiply(getRatio());
    }
    public Point2D viewToCanvas(double x, double y) {
        return viewToCanvas(new Point2D(x, y));
    }
    public Point2D canvasToView(Point2D p) {
        return getPosition().add(p.multiply(getInverseRatio()));
    }
    public Point2D canvasToView(double x, double y) {
        return canvasToView(new Point2D(x, y));
    }

    public DoubleProperty ratioProperty() {
        return ratio;
    }
    public double getRatio() {
        return ratioProperty().get();
    }
    public void setRatio(double ratio) {
        ratioProperty().set(
                ratio < MIN_RATIO ? MIN_RATIO :
                ratio > MAX_RATIO ? MAX_RATIO :
                ratio);
    }

    public DoubleProperty inverseRatioProperty() {
        return inverseRatio;
    }
    public double getInverseRatio() {
        return inverseRatioProperty().get();
    }

    // The input pivot is in Pane coordinate system
    public void zoom(double delta, Point2D pivot) {
        double originalInverseRatio = getInverseRatio();
        setRatio(getRatio() + delta * UNIT_RATIO);
        translateInView(pivot.multiply(originalInverseRatio - getInverseRatio()));
    }
    public void zoom(double delta, double pivotX, double pivotY) {
        zoom(delta, new Point2D(pivotX, pivotY));
    }
    public void zoom(double delta) {
        zoom(delta, viewToCanvas(getCenter()));
    }
    public void zoomIn() {
        zoom(1.0);
    }
    public void zoomOut() {
        zoom(-1.0);
    }

    public DoubleProperty viewWidthProperty() {
        return viewWidth;
    }
    public double getWidth() {
        return viewWidthProperty().get();
    }
    public DoubleProperty viewHeightProperty() {
        return viewHeight;
    }
    public double getHeight() {
        return viewHeightProperty().get();
    }

    public Point2D getCenter() {
        return getPosition().add(Point2D.ZERO.midpoint(getWidth(), getHeight()));
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
        setPositionInView(canvasToView(p));
    }
    public void setPosition(double x, double y) {
        setPosition(new Point2D(x, y));
    }
    public void translate(Point2D p) {
        translateInView(canvasToViewScale(p));
    }
    public void translate(double deltaX, double deltaY) {
        translate(new Point2D(deltaX,  deltaY));
    }
}
