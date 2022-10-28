package hkust.edu.visualneo.utils.backend;

import com.codepoetics.protonpack.Indexed;
import com.codepoetics.protonpack.StreamUtils;
import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.Vertex;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Graph implements Expandable {

    final Set<Node> nodes;
    final Set<Relation> relations;

    private final Map<Long, Node> nodesById;
    private final Map<Long, Relation> relationsById;

    public Graph(Set<Node> nodes,
                 Set<Relation> relations) {
        this.nodes = nodes;
        this.relations = relations;

        validate();
        
        nodesById = nodes
                .stream()
                .collect(Collectors.toMap(
                        Node::getId,
                        Function.identity(),
                        (e1, e2) -> e2,
                        HashMap::new));
        relationsById = relations
                .stream()
                .collect(Collectors.toMap(
                        Relation::getId,
                        Function.identity(),
                        (e1, e2) -> e2,
                        HashMap::new));
    }

    // Construct a graph from vertices and edges, generated nodes and relations are sorted
    public static Graph fromDrawing(List<Vertex> vertices, List<Edge> edges) {
        Map<Vertex, Node> links = StreamUtils
                .zipWithIndex(vertices.stream())
                .collect(Collectors.toMap(
                        Indexed<Vertex>::getValue,
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

                    if (!e1.directed && s1.compareTo(t1) > 0) {
                        Node temp = s1;
                        s1 = t1;
                        t1 = temp;
                    }

                    if (!e2.directed && s2.compareTo(t2) > 0) {
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
        return !(nodes == null || relations == null ||
                 nodes.isEmpty() ||
                 nodes.contains(null) || relations.contains(null));
    }

    private boolean checkCompleteness() {
        Set<Node> nodeSet = new HashSet<>(nodes);
        Set<Relation> relationSet = new HashSet<>(relations);

        for (Node node : nodeSet)
            if (!relationSet.containsAll(node.relations))
                return false;

        for (Relation relation : relationSet)
            if (!nodeSet.contains(relation.start) || !nodes.contains(relation.end))
                return false;

        return true;
    }

    private boolean checkConnectivity() {
        if (nodes.size() == 1)
            return true;

        // Temporary map for node visit states: false for unvisited, true for visited
        final Map<Node, Boolean> colorMap = new HashMap<>();
        nodes.forEach(node -> colorMap.put(node, false));
        color(colorMap, nodes.iterator().next());

        return !colorMap.containsValue(false);
    }

    // Recursively color nodes with depth first algorithm
    private void color(Map<Node, Boolean> colorMap, Node focus) {
        colorMap.replace(focus, true);
        focus.relations.forEach(relation -> {
            Node other = relation.other(focus);
            if (!colorMap.get(other))
                color(colorMap, other);
        });
    }

    Node getNode(long id) {
        return nodesById.get(id);
    }

    Relation getRelation(long id) {
        return relationsById.get(id);
    }

    //    // Generate all duplicate/indistinguishable node pairs, used for inequality constraints
    //    // TODO: Enhance efficiency
    //    ArrayList<Pair<Node>> getDuplicateNodePairs() {
    //        HashSet<Pair<Node>> dupPairs = new HashSet<>();
    //        for (Node node : nodes) {
    //            dupPairs.addAll(node.getDuplicateNeighborPairs());
    //        }
    //        return new ArrayList<>(dupPairs);
    //    }

    //    // Unused
    //    // Generate all duplicate/indistinguishable relation pairs, used for inequality constraints
    //    ArrayList<Pair<Relation>> getDuplicateRelationPairs() {
    //        HashSet<Relation> dups = new HashSet<>();
    //        ArrayList<ArrayList<Relation>> dupSets = new ArrayList<>();
    //        for (int i = 0; i < relations.size(); ++i) {
    //            Relation outer = relations.get(i);
    //            if (dups.contains(outer))
    //                continue;
    //            ArrayList<Relation> dupSet = new ArrayList<>();
    //            dupSet.add(outer);
    //            for (int j = i + 1; j < relations.size(); ++j) {
    //                Relation inner = relations.get(j);
    //                if (!dups.contains(inner) && outer.duplicates(inner)) {
    //                    dups.add(inner);
    //                    dupSet.add(inner);
    //                }
    //            }
    //            dupSets.add(dupSet);
    //        }
    //
    //        ArrayList<Pair<Relation>> dupPairs = new ArrayList<>();
    //        dupSets.forEach(dupSet -> {
    //            for (int i = 0; i < dupSet.size(); ++i)
    //                for (int j = i + 1; j < dupSet.size(); ++j)
    //                    dupPairs.add(Pair.ordered(dupSet.get(i), dupSet.get(j)));
    //        });
    //
    //        return dupPairs;
    //    }

    //    public String elaborate() {
    //        StringBuilder builder = new StringBuilder();
    //
    //        char[] sep = Queries.separator(40);
    //
    //        builder.append(sep)
    //                .append(NEW_LINE);
    //
    //        builder.append("Nodes")
    //                .append(NEW_LINE);
    //        nodes.forEach(node ->
    //                builder.append("|-").append(node.elaborate().replaceAll("(\r\n?|\n)", "$1" + "| "))
    //                        .append(NEW_LINE));
    //
    //        builder.append(sep)
    //                .append(NEW_LINE);
    //
    //        builder.append("Relations")
    //                .append(NEW_LINE);
    //        relations.forEach(relation ->
    //                builder.append("|-").append(relation.elaborate().replaceAll("(\r\n?|\n)", "$1" + "| "))
    //                        .append(NEW_LINE));
    //
    //        builder.append(sep)
    //                .append(NEW_LINE);
    //
    //        return builder.toString();
    //    }


    @Override
    public String toString() {
        return "Graph";
    }

    @Override
    public Map<Object, Object> expand() {
        Map<Object, Object> expansion = new LinkedHashMap<>();
        expansion.put("Nodes", nodes);
        expansion.put("Relations", relations);
        return expansion;
    }
}
