package hkust.edu.visualneo.utils.backend;

import hkust.edu.visualneo.utils.frontend.Vertex;
import org.neo4j.driver.Value;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Node extends Entity {

    private final Collection<Relation> relations = new TreeSet<>();

    public Node(long id, String label, Map<String, Value> properties) {
        super(id, label, properties);
    }
    public Node(Vertex vertex) {
        this(vertex.getElementId(), vertex.getLabel(), vertex.getElementProperties());
    }
    public Node(org.neo4j.driver.types.Node node, boolean schema) {
        this(node.id(),
             node.labels().iterator().next(),
             schema ? Collections.emptyMap() : node.asMap(Function.identity()));
    }

    public Collection<Relation> getRelations() {
        return relations;
    }

    public boolean hasRelations() {
        return !getRelations().isEmpty();
    }

    public int relationCount() {
        return getRelations().size();
    }

    public Set<Node> getNeighbors() {
        return getRelations()
                .stream()
                .map(relation -> relation.other(this))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<Relation> getRelationsWith(Node other) {
        if (other == null)
            return Collections.emptySet();

        return getRelations()
                .stream()
                .filter(relation -> other.equals(relation.other(this)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    void attach(Relation relation) {
        relations.add(relation);
    }

    // Check whether two distinct nodes can match the same node
    // This method assumes the other node is non-null
    @Override
    public boolean resembles(Entity other) {
        if (!(other instanceof Node))
            return false;

        if (this == other)
            return false;
        
        return super.resembles(other);
    }

    @Override
    public String getName() {
        return 'n' + (index == -1 ? String.valueOf(id) : String.valueOf(index));
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("Relations", getRelations().stream().map(Relation::getName).toList());
        return map;
    }
}
