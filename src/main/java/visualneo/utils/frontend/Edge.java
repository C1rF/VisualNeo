package visualneo.utils.frontend;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.input.MouseEvent;

public class Edge extends Line {

    private final int VERTEX_RADIUS = 20;
    Vertex startVertex;
    Vertex endVertex;
    String relationship;
    boolean directed = false;

    public Edge(Vertex startVertex, Vertex endVertex) {
        super();
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        // Set the line
        this.setStartX(startVertex.x + VERTEX_RADIUS);
        this.setStartY(startVertex.y + VERTEX_RADIUS);
        this.setEndX(endVertex.x + VERTEX_RADIUS);
        this.setEndY(endVertex.y + VERTEX_RADIUS);
        setStroke(Color.BLACK);
        setStrokeWidth(3);
        System.out.println("An Edge from (" + startVertex.x + " , " + startVertex.y + ") to " +
                "(" + endVertex.x + " , " + endVertex.y + ")" + " is created!");
    }

}