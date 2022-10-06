package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.VisualNeoController;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.neo4j.driver.Value;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class GraphElement extends StackPane {

    // Radius of the Vertex
    final int VERTEX_RADIUS = 25;
    // A boolean variable indicates whether it can be selected and moved now
    public boolean canSelect = false;
    // A boolean variable indicates whether it is the focus right now

    // UI display
    Text label_displayed;
    // Labels attached to the element(node/relation)
    String label;
    // Properties attached to the element(node/relation)
    HashMap<String, Value> properties;
    Scene scene;

    GraphElement(){

        // Initialize the variable
        label = "";
        properties = new HashMap<>();
        label_displayed = new Text("");

        // Set focus effect
        setFocusTraversable(true);

    }

     void mouseEntered(MouseEvent m) {
         scene = getScene();
        if(VisualNeoController.getStatus() == VisualNeoController.Status.SELECT){
            scene.setCursor(Cursor.HAND);
        }
        else if(VisualNeoController.getStatus() == VisualNeoController.Status.ERASE){
            scene.setCursor(Cursor.DISAPPEAR);
        }
    }
    void mouseExited(MouseEvent m) {
         scene.setCursor(Cursor.DEFAULT);
    }

    void mouseReleased(MouseEvent m){
        if(VisualNeoController.getStatus() == VisualNeoController.Status.SELECT)
            mouseEntered(m);
    }

    public void addLabel(String new_label) {
        // Add the new label
        System.out.println("Add Label: " + new_label);
        label = new_label;
        label_displayed.setText(new_label);
    }
    public String getLabel(){return label;}

    public HashMap<String,Value> getProp(){return properties;}

    abstract public void becomeFocused();
    abstract public void removeFocused();
    abstract protected void pressed(MouseEvent m);
    abstract public String toText();

}
