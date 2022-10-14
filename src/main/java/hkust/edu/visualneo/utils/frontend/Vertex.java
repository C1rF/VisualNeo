package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.VisualNeoController;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.StringUtil;
import java.util.Arrays;

public class Vertex extends GraphElement {

    // record the position of center of the circle
    double x,y;
    private final Color COLOR = Color.LIGHTGRAY;
    // record the offset (only used for move the object)
    double offX, offY;
    // The shape contains a circle and a text(not necessary) on top of it
    private Circle c;

    // Constructor
    public Vertex(Pane DrawBoard, double x, double y) {

        super();
        this.DrawBoard = DrawBoard;
        // Set the position
        this.x = x;
        this.y = y;

        // Initialize the circle
        c = new Circle(VERTEX_RADIUS, COLOR);
        setPos();

        // Set focus traversable
        c.setFocusTraversable(true);

        // Display the circle and label on the StackPane
        DrawBoard.getChildren().addAll(c,label_displayed);
        c.toFront();
        label_displayed.toFront();

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
    public void requestFocus(){
        c.requestFocus();
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
    @Override
    public void eraseShapes(){
        DrawBoard.getChildren().remove(c);
        DrawBoard.getChildren().remove(label_displayed);
    }
    @Override
    public javafx.scene.Node getShape(){
        return c;
    }


    /**
     *  Request focus when pressed
     */
    @Override
    protected void pressed(MouseEvent m) {
        offX = m.getX();
        offY = m.getY();
        c.requestFocus();
        if(VisualNeoController.getStatus() == VisualNeoController.Status.SELECT) m.consume();
    }

    public void dragged(MouseEvent m) {
        if(VisualNeoController.getStatus() != VisualNeoController.Status.SELECT)
            return;
        scene.setCursor(Cursor.CLOSED_HAND);
        // (m.getX() - offX) contains the minor changes of x coordinate
        // (m.getY() - offY) contains the minor changes of y coordinate
        x += m.getX() - offX; // keep updating the coordinate
        y += m.getY() - offY; // keep updating the coordinate
        setPos();
    }

    public void setPos() {
        c.setLayoutX(x);
        c.setLayoutY(y);
        label_displayed.setLayoutX(x-0.7*VERTEX_RADIUS);
        label_displayed.setLayoutY(y+0.2*VERTEX_RADIUS);
    }

    public boolean containCircle(Circle circle_to_compare){
        return (circle_to_compare == c);
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
