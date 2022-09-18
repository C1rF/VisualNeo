package visualneo.utils.frontend;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import visualneo.FrontendUI;
import visualneo.VisualNeoApp;

public class Vertex extends StackPane {

    // record the position of center of the Vertex
    double x,y;
    // Radius of the Vertex
    private final int RADIUS = 20;
    private final Color COLOR = Color.LIGHTGRAY;
    // record the offset (only used for move the object)
    double offX, offY;
    // The shape contains a circle and a text(not necessary) on top of it
    Circle c;
    Text label;

    // A boolean variable indicates whether it can be selected and moved now
    public boolean canSelect = false;
    // A boolean variable indicates whether it is the focus right now
    public boolean isFocused = false;


    // Constructor
    public Vertex(double x, double y) {

        // set the Vertex
        this.x = x-RADIUS;
        this.y = y-RADIUS;
        setPrefWidth(RADIUS);
        setPrefHeight(RADIUS);
        c = new Circle(RADIUS, COLOR);
        label = new Text("");
        getChildren().addAll(c,label);
        setPos();

        // Set Focus Effect
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

        // Give them event handler
        addEventHandler( MouseEvent.MOUSE_PRESSED, e->{if(canSelect) { pressed(e);}  } );
        addEventHandler( MouseEvent.MOUSE_DRAGGED, e->{if(canSelect) { dragged(e);}  } );

        System.out.println("A new Vertex is created.");
    }

    public void pressed(MouseEvent m) {
        if(FrontendUI.getStatus() == FrontendUI.Status.SELECT)
            m.consume();
        offX = m.getX();
        offY = m.getY();
        if(!isFocused){
            requestFocus();
        }
    }

    public void dragged(MouseEvent m) {
        if(FrontendUI.getStatus() == FrontendUI.Status.SELECT)
            m.consume();
        // (m.getX() - offX) contains the minor changes of x coordinate
        // (m.getY() - offY) contains the minor changes of y coordinate
        x += m.getX() - offX; // keep updating the coordinate
        y += m.getY() - offY; // keep updating the coordinate
        //System.out.println(x+" "+y+" "+width+" "+height);
        setPos();

    }
    public void setPos() {
        setLayoutX(x);
        setLayoutY(y);
    }

    @Override
    public String toString() {
        String circle_info =  x +" "+y;
        return circle_info;
    }

    public void updatePos(double newX, double newY) {
        x = newX;
        y = newY;
    }

    public void setText(String input_text) {
        label.setText(input_text);
    }

}