package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.VisualNeoController;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class Vertex extends GraphElement {

    // record the position of center of the Vertex
    double x,y;
    private final Color COLOR = Color.LIGHTGRAY;
    // record the offset (only used for move the object)
    double offX, offY;
    // The shape contains a circle and a text(not necessary) on top of it
    Circle c;


    // Constructor
    public Vertex(double x, double y) {

        // set the Vertex
        this.x = x-VERTEX_RADIUS;
        this.y = y-VERTEX_RADIUS;
        setPrefWidth(VERTEX_RADIUS);
        setPrefHeight(VERTEX_RADIUS);
        c = new Circle(VERTEX_RADIUS, COLOR);
        label_displayed = new Text("");
        getChildren().addAll(c,label_displayed);
        setPos();

        // Set Focus Effect
        setFocusEffect();

        // Give them event handler
        c.addEventHandler( MouseEvent.MOUSE_PRESSED, e->{if(canSelect) { pressed(e);}  } );
        c.addEventHandler( MouseEvent.MOUSE_DRAGGED, e->{if(canSelect) { dragged(e);}  } );
        c.addEventHandler( MouseEvent.MOUSE_ENTERED, e->{if(canSelect) { mouseEntered(e);}  } );
        c.addEventHandler( MouseEvent.MOUSE_EXITED, e->{if(canSelect) { mouseExited(e);}  } );
        c.addEventHandler( MouseEvent.MOUSE_RELEASED, e->{if(canSelect) { mouseReleased(e);}  } );


        System.out.println("A new Vertex is created.");
    }

    public void setFocusEffect(){
        setFocusTraversable(true);
        focusedProperty().addListener(
                (obs, oldVal, newVal) ->
                {
                    if(newVal) {
                        // c.setStrokeType(StrokeType.OUTSIDE);
                        c.setStrokeWidth(2);
                        isFocused = true;
                        c.setStroke(new Color(0, 0, 0, 1));
                    }
                    if(oldVal) {
                        c.setStrokeWidth(0);
                        isFocused = false;
                    }
                }
        );
    }

    private void pressed(MouseEvent m) {
        offX = m.getX();
        offY = m.getY();
        if(!isFocused){
            requestFocus();
        }
        if(VisualNeoController.getStatus() == VisualNeoController.Status.SELECT){
            m.consume();
        }
    }

    public void dragged(MouseEvent m) {
        if(VisualNeoController.getStatus() != VisualNeoController.Status.SELECT)
            return;
        // (m.getX() - offX) contains the minor changes of x coordinate
        // (m.getY() - offY) contains the minor changes of y coordinate
        x += m.getX() - offX; // keep updating the coordinate
        y += m.getY() - offY; // keep updating the coordinate
        //System.out.println(x+" "+y+" "+width+" "+height);
        setPos();
        getScene().setCursor(Cursor.CLOSED_HAND);
    }

    public void setPos() {
        setLayoutX(x);
        setLayoutY(y);
    }

    @Override
    public String toString() {
        String circle_info =  x+" "+y +" "+label_displayed.getText()+" "+ label_displayed.getText();
        return circle_info;
    }

    public void updatePos(double newX, double newY) {
        x = newX;
        y = newY;
    }

}
