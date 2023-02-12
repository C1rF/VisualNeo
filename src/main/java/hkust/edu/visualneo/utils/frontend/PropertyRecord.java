package hkust.edu.visualneo.utils.frontend;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class PropertyRecord extends VBox {

    private static GraphElement host;
    private static VBox parent;
    private ContextMenu contextMenu;
    private Text property_name;
    private Text property_value;
    private String name;

    public PropertyRecord(boolean modifiable) {
        // initialization
        property_name = new Text();
        property_value = new Text();



        if(modifiable){
            contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Delete");
            contextMenu.getItems().add(deleteItem);

            // Set on click
            deleteItem.setOnAction(e -> {
                // Remove the property from the host
                host.removeProperty(name);
                // Remove the frontend display
                parent.getChildren().remove(this);
            });

            // show context menu on right-click
            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(this, event.getScreenX(), event.getScreenY());
                }
            });

            // hide context menu on left-click or outside of component
            setOnMousePressed(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    contextMenu.hide();
                }
            });
        }

        // Add hover effect
        getStyleClass().add("property_record");

        // Display
        setPrefWidth(160);
        property_name.setWrappingWidth(160);
        property_value.setWrappingWidth(160);

        getChildren().addAll(property_name, property_value);
    }

    public static void setParent(VBox p){
        parent = p;
    }
    public static void setHost(GraphElement h){
        host = h;
    }
    public void setDisplay(String name, String value){
        this.name = name;
        property_name.setText(name + ":");
        property_value.setText(value);
    }

}

