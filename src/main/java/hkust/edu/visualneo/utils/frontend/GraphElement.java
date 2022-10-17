package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.VisualNeoController;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.neo4j.driver.Value;

import java.util.HashMap;

abstract public class GraphElement extends Group {

    // Radius of the Vertex
    protected static final int VERTEX_RADIUS = 25;
    // A boolean variable indicates whether it can be selected and moved now
    public boolean canSelect = false;
    // Label shown on the GraphElement
    Text label_displayed;
    // String of the label
    String label;
    // Properties attached to the element(node/relation)
    HashMap<String, Value> properties;

    GraphElement() {
        // initialize the arraylist
        properties = new HashMap<>();
        label_displayed = new Text("");
    }

    void mouseExited(MouseEvent m) {
        getScene().setCursor(Cursor.DEFAULT);
    }

    void mouseReleased(MouseEvent m) {
        if (VisualNeoController.getStatus() == VisualNeoController.Status.SELECT)
            mouseEntered(m);
    }

    public void addLabel(String new_label) {
        // Add the new label
        System.out.println("Add Label: " + new_label);
        label = new_label;
        label_displayed.setText(new_label);
    }

    public void addProperty(String name, Value val) {
        properties.put(name, val);
    }

    public String getLabel() {
        return label;
    }

    public HashMap<String, Value> getProp() {
        return properties;
    }

    abstract protected void initializeShape();

    abstract public void becomeHighlight();

    abstract public void removeHighlight();

    abstract public void eraseFrom(VisualNeoController controller);

    abstract protected void pressed(MouseEvent m);

    abstract protected void mouseEntered(MouseEvent m);

    abstract public String toText();

}
