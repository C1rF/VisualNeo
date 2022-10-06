package hkust.edu.visualneo.utils.backend;

import hkust.edu.visualneo.utils.frontend.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Graph {

    final Set<Node> nodes;
    final Set<Relation> relations;

    public Graph(Set<Node> nodes,
                 Set<Relation> relations) {
        this.nodes = nodes;
        this.relations = relations;

        validate();
    }

    // Construct a graph from vertices and edges, generated nodes and relations are sorted
    public static Graph fromDrawing(List<Vertex> vertices, List<Edge> edges) {
        Map<Vertex, Node> links = vertices.stream().collect(Collectors.toMap(
                Function.identity(),
                Node::new,
                (e1, e2) -> e2,
                LinkedHashMap::new));

        Set<Relation> relations = edges
                .stream()
                .map(edge -> new Relation(edge, links))
                .sorted(Comparator.comparing((Relation r) -> r.start).thenComparing(r -> r.end))
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
        for (Node node : nodeSet) {
            if (!relationSet.containsAll(node.relations))
                return false;
        }
        for (Relation relation : relationSet) {
            if (!nodeSet.contains(relation.start) || !nodes.contains(relation.end))
                return false;
        }
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

    public static void recount() {
        Entity.recount();
        Node.recount();
        Relation.recount();
    }
}
