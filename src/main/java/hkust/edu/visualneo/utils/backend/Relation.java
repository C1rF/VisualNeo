package hkust.edu.visualneo.utils.backend;

import hkust.edu.visualneo.utils.frontend.Edge;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Relationship;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class Relation extends Entity {

    public final boolean directed;

    public final Node start;
    public final Node end;

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
    public Relation(Edge edge, Node start, Node end) {
        this(edge.getElementId(),
             edge.isDirected(),
             start,
             end,
             edge.getLabel(),
             edge.getProp());
    }
    public Relation(Relationship relationship, Map<Long, Node> nodes, boolean schema) {
        this(relationship.id(),
             true,
             nodes.get(relationship.startNodeId()),
             nodes.get(relationship.endNodeId()),
             relationship.type(),
             schema ? Collections.emptyMap() : relationship.asMap(Function.identity()));
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
    public boolean resembles(Entity other) {
        if (!(other instanceof Relation))
            return false;

        return super.resembles(other);
    }

    @Override
    public String getName() {
        return 'e' + (index == -1 ? String.valueOf(id) : String.valueOf(index));
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("Directed", directed ? "True" : "False");
        map.put("Start Node", start.getName());
        map.put("End Node", end.getName());
        return map;
    }
}
