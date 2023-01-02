package hkust.edu.visualneo.utils.backend;

import hkust.edu.visualneo.utils.frontend.Canvas;
import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.Vertex;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Graph implements Mappable {

    private Map<Long, Node> nodes;
    private Map<Long, Relation> relations;

    private QueryBuilder translator;

    public Graph() {
        nodes = new TreeMap<>();
        relations = new TreeMap<>();
    }

    public Graph(Collection<Node> nodes, Collection<Relation> relations) {
        this.nodes = nodes
                .stream()
                .collect(Collectors.toMap(Node::getId, Function.identity(), (e1, e2) -> e1, TreeMap::new));
        this.relations = relations
                .stream()
                .collect(Collectors.toMap(Relation::getId, Function.identity(), (e1, e2) -> e1, TreeMap::new));
    }

    public Graph(Canvas canvas) {
        nodes = canvas.getVertices()
                .stream()
                .collect(Collectors.toMap(Vertex::getElementId, Node::new, (e1, e2) -> e1, TreeMap::new));

        relations = canvas.getEdges()
                .stream()
                .collect(Collectors.toMap(Edge::getElementId,
                                          edge ->new Relation(edge,
                                                              nodes.get(edge.startVertex.getElementId()),
                                                              nodes.get(edge.endVertex.getElementId())),
                                                              (e1, e2) -> e1,
                                                              TreeMap::new));
    }

    public void set(Graph graph) {
        nodes = graph.nodes;
        relations = graph.relations;
        if (isBound())
            translator.update(this);
    }

    public void clear() {
        nodes.clear();
        relations.clear();
        if (isBound())
            translator.update(this);
    }

    public void bind(QueryBuilder builder) {
        translator = Objects.requireNonNull(builder);
    }

    public void unbind() {
        translator.unbind();
        translator = null;
    }

    public boolean isBound() {
        return translator != null;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public boolean isConnected() {
        if (nodeCount() <= 1)
            return true;

        Collection<Node> uncoloredNodes = new HashSet<>(getNodes());
        color(uncoloredNodes, getNodes().iterator().next());

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

    public void index() {
        int nodeIndex = 0;
        for (Node node : getNodes())
            node.setIndex(nodeIndex++);
        int relationIndex = 0;
        for (Relation relation : getRelations())
            relation.setIndex(relationIndex++);
    }

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        if (isBound())
            translator.update(this);
    }
    public void addRelation(Relation relation) {
        relations.put(relation.getId(), relation);
        if (isBound())
            translator.update(this);
    }

    public void removeNode(Long id) {
        nodes.remove(id);
        if (isBound())
            translator.update(this);
    }
    public void removeRelation(Long id) {
        relations.remove(id);
        if (isBound())
            translator.update(this);
    }

    public Collection<Node> getNodes() {
        return nodes.values();
    }
    public Collection<Relation> getRelations() {
        return relations.values();
    }

    public int nodeCount() {
        return nodes.size();
    }
    public int relationCount() {
        return relations.size();
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
        map.put("Nodes", getNodes().stream()
                                   .collect(Collectors.toMap(Node::getName,
                                                        Node::toMap,
                                                        (e1, e2) -> e2,
                                                        LinkedHashMap::new)));
        map.put("Relations", getRelations().stream()
                                           .collect(Collectors.toMap(Relation::getName,
                                                                Relation::toMap,
                                                                (e1, e2) -> e2,
                                                                LinkedHashMap::new)));
        return map;
    }

    public static class BadTopologyException extends IllegalArgumentException {

        public enum TopologyType {
            EMPTY,
            DISCONNECTED,
            DEFAULT
        }

        private static final String EMPTY_ERROR_MESSAGE = "Graph is empty!";
        private static final String DISCONNECTED_ERROR_MESSAGE = "Graph is disconnected!";

        private final TopologyType type;

        public BadTopologyException() {
            this(TopologyType.DEFAULT);
        }

        public BadTopologyException(TopologyType type) {
            super(type == TopologyType.EMPTY ? EMPTY_ERROR_MESSAGE :
                  type == TopologyType.DISCONNECTED ? DISCONNECTED_ERROR_MESSAGE :
                  "");

            this.type = type;
        }

        public TopologyType getType() {
            return type;
        }
    }
}
