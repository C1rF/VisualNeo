package hkust.edu.visualneo.utils.backend;

import com.codepoetics.protonpack.Indexed;
import com.codepoetics.protonpack.StreamUtils;
import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.Vertex;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Graph implements Mappable {

    private final Map<Long, Node> unmodifiableNodeMap;
    private final Map<Long, Relation> unmodifiableRelationMap;

    private final Collection<Node> unmodifiableNodeSet;
    private final Collection<Relation> unmodifiableRelationSet;

    public Graph(Set<Node> nodes,
                 Set<Relation> relations) {
        unmodifiableNodeMap = nodes
                .stream()
                .collect(Collectors.toUnmodifiableMap(Node::getId, Function.identity()));
        unmodifiableRelationMap = relations
                .stream()
                .collect(Collectors.toUnmodifiableMap(Relation::getId, Function.identity()));
        unmodifiableNodeSet = Set.copyOf(nodes);
        unmodifiableRelationSet = Set.copyOf(relations);

        validate();
    }

    // Construct a graph from vertices and edges, generated nodes and relations are sorted
    public static Graph fromDrawing(List<Vertex> vertices, List<Edge> edges) {
        Map<Vertex, Node> links = StreamUtils
                .zipWithIndex(vertices.stream())
                .collect(Collectors.toMap(
                        Indexed::getValue,
                        vertexIndexed -> new Node(
                                vertexIndexed.getIndex(),
                                vertexIndexed.getValue()),
                        (e1, e2) -> e2,
                        LinkedHashMap::new));

        Stream<Edge> edgesSorted = edges
                .stream()
                .sorted((e1, e2) -> {
                    Node s1 = links.get(e1.startVertex);
                    Node t1 = links.get(e1.endVertex);
                    Node s2 = links.get(e2.startVertex);
                    Node t2 = links.get(e2.endVertex);

                    if (!e1.isDirected() && s1.compareTo(t1) > 0) {
                        Node temp = s1;
                        s1 = t1;
                        t1 = temp;
                    }

                    if (!e2.isDirected() && s2.compareTo(t2) > 0) {
                        Node temp = s2;
                        s2 = t2;
                        t2 = temp;
                    }

                    return s1.compareTo(s2) == 0 ? t1.compareTo(t2) : s1.compareTo(s2);
                });

        Set<Relation> relations = StreamUtils
                .zipWithIndex(edgesSorted)
                .map(edgeIndexed -> new Relation(
                        edgeIndexed.getIndex(),
                        edgeIndexed.getValue(),
                        links))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new Graph(new LinkedHashSet<>(links.values()), relations);
    }

    private void validate() {
        if (!checkNonNull())
            throw new NullPointerException("Null/Empty Entity!");
        if (!checkCompleteness())
            throw new IllegalArgumentException("Node/Relation list is not complete!");
        if (!checkConnectivity())
            throw new IllegalArgumentException("Graph is not connected!");
    }

    private boolean checkNonNull() {
        return !(nodes() == null || relations() == null ||
                 nodes().isEmpty() ||
                 nodes().contains(null) || relations().contains(null));
    }

    private boolean checkCompleteness() {
        for (Node node : nodes())
            if (!relations().containsAll(node.relations()))
                return false;

        for (Relation relation : relations())
            if (!nodes().contains(relation.start) || !nodes().contains(relation.end))
                return false;

        return true;
    }

    private boolean checkConnectivity() {
        if (nodes().size() == 1)
            return true;

        Collection<Node> uncoloredNodes = new HashSet<>(nodes());
        color(uncoloredNodes, nodes().iterator().next());

        return !uncoloredNodes.isEmpty();
    }

    // Recursively color nodes with depth first algorithm
    private void color(Collection<Node> uncoloredNodes, Node focus) {
        uncoloredNodes.remove(focus);
        focus.relations().forEach(relation -> {
            Node other = relation.other(focus);
            if (uncoloredNodes.contains(other))
                color(uncoloredNodes, other);
        });
    }

    public Collection<Node> nodes() {
        return unmodifiableNodeSet;
    }
    public Collection<Relation> relations() {
        return unmodifiableRelationSet;
    }

    public int nodeCount() {
        return nodes().size();
    }
    public int relationCount() {
        return relations().size();
    }

    public Collection<Long> nodeIds() {
        return unmodifiableNodeMap.keySet();
    }
    public Collection<Long> relationIds() {
        return unmodifiableRelationMap.keySet();
    }

    public Node getNode(long id) {
        return unmodifiableNodeMap.get(id);
    }
    public Relation getRelation(long id) {
        return unmodifiableRelationMap.get(id);
    }

    @Override
    public String toString() {
        return new TreePrinter().print(getName(), toMap());
    }

    @Override
    public String getName() {
        return "Graph";
    }

    @Override
    public Map<Object, Object> toMap() {
        Map<Object, Object> map = new LinkedHashMap<>();
        map.put("Nodes", nodes().stream()
                                .collect(Collectors.toMap(Node::getName,
                                                        Node::toMap,
                                                        (e1, e2) -> e2,
                                                        LinkedHashMap::new)));
        map.put("Relations", relations().stream()
                                        .collect(Collectors.toMap(Relation::getName,
                                                                Relation::toMap,
                                                                (e1, e2) -> e2,
                                                                LinkedHashMap::new)));
        return map;
    }
}
