package hkust.edu.visualneo.utils.frontend;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;

import java.util.Objects;

import static java.lang.Math.PI;

public class PositionProperty extends SimpleObjectProperty<Point2D> {

    public PositionProperty(Object bean, String name) {
        this(bean, name, Point2D.ZERO);
    }

    public PositionProperty(Object bean, String name, Point2D initialValue) {
        super(bean, name, initialValue);
    }

    @Override
    public void set(Point2D newValue) {
        if (!Objects.equals(get(), newValue))
            super.set(newValue);
    }

    public static double angle(Point2D source, Point2D target) {
        Point2D delta = target.subtract(source);
        return delta.getX() == 0.0 ?
               delta.getY() > 0.0 ? PI / 2 : -PI / 2 :
               Math.atan2(delta.getY(), delta.getX());
    }
}
