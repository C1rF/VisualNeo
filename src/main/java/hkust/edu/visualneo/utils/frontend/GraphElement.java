package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.VisualNeoController;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class GraphElement extends StackPane {

    // Radius of the Vertex
    final int VERTEX_RADIUS = 20;
    // A boolean variable indicates whether it can be selected and moved now
    public boolean canSelect = false;
    // A boolean variable indicates whether it is the focus right now
    public boolean isFocused = false;

    // Labels attached to the element(node/relation)
    ArrayList<String> labels;
    // UI display
    Text text_label;
    // Properties attached to the element(node/relation)
    ArrayList<String> properties;
    // UI display
    Text text_property;

    Scene scene;

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

}
