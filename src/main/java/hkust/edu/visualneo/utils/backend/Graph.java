package hkust.edu.visualneo.utils.backend;

import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.Vertex;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Graph implements Mappable {

    private final Map<Long, Node> nodes;
    private final Map<Long, Relation> relations;

    public Graph(Collection<Node> nodes, Collection<Relation> relations, boolean checkConnectivity) {
        this.nodes = nodes
                .stream()
                .collect(Collectors.toMap(Node::getId, Function.identity(), (e1, e2) -> e1, TreeMap::new));
        this.relations = relations
                .stream()
                .collect(Collectors.toMap(Relation::getId, Function.identity(), (e1, e2) -> e1, TreeMap::new));

        validate(checkConnectivity);
    }

    // Construct a graph from vertices and edges, generated nodes and relations are sorted
    public static Graph fromDrawing(Collection<Vertex> vertices, Collection<Edge> edges) {
        Map<Vertex, Node> nodes = vertices
                .stream()
                .collect(Collectors.toMap(Function.identity(), Node::new));

        Collection<Relation> relations = edges
                .stream()
                .map(edge -> new Relation(edge,
                                          nodes.get(edge.startVertex),
                                          nodes.get(edge.endVertex)))
                .collect(Collectors.toSet());

        Graph graph = new Graph(nodes.values(), relations, true);
        graph.index();

        return graph;
    }

    private void validate(boolean checkConnectivity) {
        if (!isNonNull())
            throw new NullPointerException("Null/Empty Entity!");
        if (!isComplete())
            throw new IllegalArgumentException("Node/Relation list is not complete!");
        if (checkConnectivity && !isConnected())
            throw new IllegalArgumentException("Graph is not connected!");
    }

    private boolean isNonNull() {
        return !(nodes() == null || relations() == null || nodes().isEmpty());
    }

    private boolean isComplete() {
        for (Node node : nodes())
            if (!relations().containsAll(node.relations()))
                return false;

        for (Relation relation : relations())
            if (!nodes().contains(relation.start) || !nodes().contains(relation.end))
                return false;

        return true;
    }

    private boolean isConnected() {
        if (nodes().size() == 1)
            return true;

        Collection<Node> uncoloredNodes = new HashSet<>(nodes());
        color(uncoloredNodes, nodes().iterator().next());

        return uncoloredNodes.isEmpty();
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

    private void index() {
        int nodeIndex = 0;
        for (Node node : nodes())
            node.setIndex(nodeIndex++);
        int relationIndex = 0;
        for (Relation relation : relations())
            relation.setIndex(relationIndex++);
    }

    public Collection<Node> nodes() {
        return nodes.values();
    }
    public Collection<Relation> relations() {
        return relations.values();
    }

    public int nodeCount() {
        return nodes().size();
    }
    public int relationCount() {
        return relations().size();
    }

    public Collection<Long> nodeIds() {
        return nodes.keySet();
    }
    public Collection<Long> relationIds() {
        return relations.keySet();
    }

    public Node getNode(long id) {
        return nodes.get(id);
    }
    public Relation getRelation(long id) {
        return relations.get(id);
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
