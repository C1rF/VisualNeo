package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.VisualNeoController;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.StringUtil;

import java.util.Arrays;
import java.util.List;

public class Vertex extends GraphElement {

    // record the position of upper-corner of the StackPane
    double x,y;
    private final Color COLOR = Color.LIGHTGRAY;
    // record the offset (only used for move the object)
    double offX, offY;
    // The shape contains a circle and a text(not necessary) on top of it
    Circle c;

    // Constructor
    public Vertex(double x, double y) {

        super();
        // Set the position
        this.x = x-VERTEX_RADIUS;
        this.y = y-VERTEX_RADIUS;
        setPos();

        // Initialize the circle
        c = new Circle(VERTEX_RADIUS, COLOR);

        // Display the circle and label on the StackPane
        getChildren().addAll(c,label_displayed);

        // Give them event handler
        c.addEventHandler( MouseEvent.MOUSE_PRESSED, e->{if(canSelect) { pressed(e);}  } );
        c.addEventHandler( MouseEvent.MOUSE_DRAGGED, e->{if(canSelect) { dragged(e);}  } );
        c.addEventHandler( MouseEvent.MOUSE_ENTERED, e->{if(canSelect) { mouseEntered(e);}  } );
        c.addEventHandler( MouseEvent.MOUSE_EXITED, e->{if(canSelect) { mouseExited(e);}  } );
        c.addEventHandler( MouseEvent.MOUSE_RELEASED, e->{if(canSelect) { mouseReleased(e);}  } );

        // For Testing
        System.out.println("A new Vertex is created.");
    }

    @Override
    public void becomeFocused(){
        c.setStrokeWidth(2);
        c.setStroke(new Color(0, 0, 0, 1));
    }

    @Override
    public void removeFocused(){
        c.setStrokeWidth(0);
    }

    /**
     *  Request focus when pressed
     */
    @Override
    protected void pressed(MouseEvent m) {
        offX = m.getX();
        offY = m.getY();
        requestFocus();
        if(VisualNeoController.getStatus() == VisualNeoController.Status.SELECT) m.consume();
    }

    public void dragged(MouseEvent m) {
        if(VisualNeoController.getStatus() != VisualNeoController.Status.SELECT)
            return;
        getScene().setCursor(Cursor.CLOSED_HAND);
        // (m.getX() - offX) contains the minor changes of x coordinate
        // (m.getY() - offY) contains the minor changes of y coordinate
        x += m.getX() - offX; // keep updating the coordinate
        y += m.getY() - offY; // keep updating the coordinate
        setPos();
    }

    public void setPos() {
        setLayoutX(x);
        setLayoutY(y);
    }

    @Override
    public String toText() {
        String[] temp = new String[] {String.valueOf(x),
                                      String.valueOf(y),
                                      label_displayed.getText(),
                                      label_displayed.getText()};
        return (String) StringUtil.join(" ", Arrays.asList(temp));
    }

}
