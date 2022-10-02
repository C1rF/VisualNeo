package hkust.edu.visualneo.utils.backend;

import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.Vertex;
import org.neo4j.driver.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Relation extends Entity {

    final boolean directed;

    final Node start;
    final Node end;

    public Relation(boolean directed, Node start, Node end,
            String label, HashMap<String, Value> properties) {
        super(label, properties);
        this.directed = directed;
        this.start = Objects.requireNonNull(start, "The start node is null!");
        this.end = Objects.requireNonNull(end, "The end node is null!");
        start.attach(this);
        end.attach(this);
    }

    public Relation(Edge edge, HashMap<Vertex, Node> links) {
        this(edge.directed,
                links.get(edge.startVertex),
                links.get(edge.endVertex),
                edge.label,
                edge.properties);
    }

    Node other(Node node) {
        if (node == start)
            return end;
        if (node == end)
            return start;
        return null;
    }

    // Check whether two relations are duplicate/indistinguishable
    // Two distinct relations are indistinguishable iff they have the same origin, target, label, and properties
    // This method assumes the other relation is non-null and the two relations are distinct
    boolean duplicates(Relation other) {
//        if (other == null || this == other)
//            return false;

        if (directed && other.directed) {
            if (start != other.start || end != other.end)
                return false;
        }
        else if ((start != other.start || end != other.end) && (start != other.end || end != other.start))
            return false;

        return resembles(other);
    }

    // Check whether two relations have the same label and properties
    // This method assumes the other relation is non-null and the two relations are distinct
    @Override
    boolean resembles(Entity other) {
        if (!(other instanceof Relation))
            return false;
        return super.resembles(other);
    }

    void detach() {
        start.detach(this);
        end.detach(this);
    }
}
