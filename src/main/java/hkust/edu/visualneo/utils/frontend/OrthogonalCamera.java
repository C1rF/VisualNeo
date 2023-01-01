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
    private static final double MIN_RATIO = 0.2;
    private static final double MAX_RATIO  = 2.0;
    private static final double UNIT_RATIO = 0.1;
    private static final double ZOOM_UNITS = 2.5;

    private final Canvas canvas;

    // World port size to canvas size
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

    public Point2D worldToScreen(Point2D p) {
        return getCanvasCenter().add(p.subtract(getPosition()).multiply(getRatio()));
    }
    public Point2D worldToScreen(double x, double y) {
        return worldToScreen(new Point2D(x, y));
    }
    public Point2D screenToWorld(Point2D p) {
        return getPosition().add(p.subtract(getCanvasCenter()).multiply(getInverseRatio()));
    }
    public Point2D screenToWorld(double x, double y) {
        return screenToWorld(new Point2D(x, y));
    }
    public double worldToScreenX(double x) {
        return getCanvasWidth() * 0.5 + (x - getX()) * getRatio();
    }
    public double worldToScreenY(double y) {
        return getCanvasHeight() * 0.5 + (y - getY()) * getRatio();
    }
    public double screenToWorldX(double x) {
        return getX() + (x - getCanvasWidth() * 0.5) * getInverseRatio();
    }
    public double screenToWorldY(double y) {
        return getY() + (y - getCanvasHeight() * 0.5) * getInverseRatio();
    }
    public double worldToScreenScale(double delta) {
        return delta * getRatio();
    }
    public double screenToWorldScale(double delta) {
        return delta * getInverseRatio();
    }
    public Point2D worldToScreenScale(Point2D p) {
        return p.multiply(getRatio());
    }
    public Point2D worldToScreenScale(double deltaX, double deltaY) {
        return worldToScreenScale(new Point2D(deltaX, deltaY));
    }
    public Point2D screenToWorldScale(Point2D p) {
        return p.multiply(getInverseRatio());
    }
    public Point2D screenToWorldScale(double deltaX, double deltaY) {
        return screenToWorldScale(new Point2D(deltaX, deltaY));
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

    // The input pivot is in Screen coordinate system
    public void zoomWithPivot(double delta, Point2D pivot) {
        double originalInverseRatio = getInverseRatio();
        zoom(delta);
        translate(pivot.subtract(getCanvasCenter()).multiply(originalInverseRatio - getInverseRatio()));
    }
    public void zoomWithPivot(double delta, double pivotX, double pivotY) {
        zoomWithPivot(delta, new Point2D(pivotX, pivotY));
    }
    public void zoom(double delta) {
        setRatio(getRatio() + delta * UNIT_RATIO);
    }
    public void zoomIn() {
        zoom(ZOOM_UNITS);
    }
    public void zoomOut() {
        zoom(-ZOOM_UNITS);
    }
    public void fit(Point2D min, Point2D max) {
        setPosition(min.midpoint(max));
        setRatio(canvas.getWidth() / (max.getX() - min.getX()));
    }

    public double getCanvasWidth() {
        return canvas.getWidth();
    }
    public double getCanvasHeight() {
        return canvas.getHeight();
    }
    public Point2D getCanvasCenter() {
        return new Point2D(getCanvasWidth() * 0.5, getCanvasHeight() * 0.5);
    }

    public DoubleProperty viewWidthProperty() {
        return viewWidth;
    }
    public double getViewWidth() {
        return viewWidthProperty().get();
    }
    public DoubleProperty viewHeightProperty() {
        return viewHeight;
    }
    public double getViewHeight() {
        return viewHeightProperty().get();
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
    public void setPosition(Point2D p) {
        if (!getPosition().equals(p))  // To function with ObjectProperty
            positionProperty().set(p);
    }
    public void setPosition(double x, double y) {
        setPosition(new Point2D(x, y));
    }
    public void translate(Point2D delta) {
        setPosition(getPosition().add(delta));
    }
    public void translate(double deltaX, double deltaY) {
        translate(new Point2D(deltaX, deltaY));
    }

    public void setPositionInScreen(Point2D p) {
        setPosition(screenToWorld(p));
    }
    public void setPositionInScreen(double x, double y) {
        setPositionInScreen(new Point2D(x, y));
    }
    public void translateInScreen(Point2D p) {
        translate(screenToWorldScale(p));
    }
    public void translateInScreen(double deltaX, double deltaY) {
        translateInScreen(new Point2D(deltaX, deltaY));
    }
    public void returnToOrigin() {
        setPosition(Point2D.ZERO);
    }
}
