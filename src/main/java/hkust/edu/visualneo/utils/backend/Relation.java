package hkust.edu.visualneo.utils.backend;

import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.Vertex;
import org.neo4j.driver.Value;

import java.util.Map;
import java.util.Objects;

public class Relation extends Entity {

    final boolean directed;

    final Node start;
    final Node end;

    public Relation(long id, Edge edge, Map<Vertex, Node> links) {
        this(
                id,
                edge.directed,
                links.get(edge.startVertex),
                links.get(edge.endVertex),
                edge.getLabel(),
                edge.getProp());
    }

    public Relation(long id, boolean directed, Node start, Node end,
                    String label, Map<String, Value> properties) {
        super(id, label, properties);
        this.directed = directed;
        Objects.requireNonNull(start, "Node is null!");
        Objects.requireNonNull(end, "Node is null!");
        if (!directed && start.compareTo(end) > 0) {
            this.start = end;
            this.end = start;
        }
        else {
            this.start = start;
            this.end = end;
        }
        start.attach(this);
        end.attach(this);
    }

    public Node other(Node node) {
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
        else if ((start != other.start || end != other.end) &&
                 (start != other.end || end != other.start))
            return false;

        return resembles(other);
    }

    // Check whether two relations can match the same relation
    // This method assumes the other relation is non-null and the two relations are distinct
    @Override
    boolean resembles(Entity other) {
        if (!(other instanceof Relation))
            return false;
        return super.resembles(other);
    }

    @Override
    public String toString() {
        return 'r' + super.toString();
    }

    //    @Override
    //    public String elaborate() {
    //        return String.format("""
    //                %1$s
    //                |-Start Node: %2$s
    //                |-End Node: %3$s""",
    //                super.elaborate(),
    //                start.toString(),
    //                end.toString());
    //    }

    @Override
    public Map<Object, Object> expand() {
        Map<Object, Object> expansion = super.expand();
        expansion.put("Start Node", start.toString());
        expansion.put("End Node", end.toString());
        return expansion;
    }
}
