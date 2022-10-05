package hkust.edu.visualneo.utils.backend;

import hkust.edu.visualneo.utils.frontend.*;

import java.util.*;

public class Graph {

    final ArrayList<Node> nodes;
    final ArrayList<Relation> relations;

    public Graph(ArrayList<Node> nodes,
                 ArrayList<Relation> relations) {
        this.nodes = nodes;
        this.relations = relations;

        validate();
    }

    // Construct a graph from vertices and edges, generated nodes and relations are sorted
    public static Graph fromDrawing(ArrayList<Vertex> vertices, ArrayList<Edge> edges) {
        Map<Vertex, Node> links = new LinkedHashMap<>();
        vertices.forEach(vertex -> links.put(vertex, new Node(vertex)));

        ArrayList<Relation> relations = new ArrayList<>();
        edges.forEach(edge -> relations.add(new Relation(edge, links)));
        relations.sort(Comparator.comparing(o -> o.start));

        return new Graph(new ArrayList<>(links.values()), relations);
    }

    private void validate() throws RuntimeException {
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
        for (Node node : nodes) {
            for (Relation relation : node.relations)
                if (!relations.contains(relation))
                    return false;
        }
        for (Relation relation : relations) {
            if (!nodes.contains(relation.start) || !nodes.contains(relation.end))
                return false;
        }
        return true;
    }

    private boolean checkConnectivity() {
        if (nodes.size() == 1)
            return true;

        // Temporary HashMap for node visit states: false for unvisited, true for visited
        final HashMap<Node, Boolean> colorMap = new HashMap<>();
        nodes.forEach(node -> colorMap.put(node, false));
        color(colorMap, nodes.get(0));

        return !colorMap.containsValue(false);
    }

    // Recursively color nodes with depth first algorithm
    private void color(HashMap<Node, Boolean> colorMap, Node focus) {
        colorMap.replace(focus, true);
        focus.relations.forEach(relation -> {
            Node other = relation.other(focus);
            if (!colorMap.get(other))
                color(colorMap, other);
        });
    }

    // Generate all duplicate/indistinguishable node pairs, used for inequality constraints
    // TODO: Enhance efficiency
    ArrayList<Pair<Node>> getDuplicateNodePairs() {
        HashSet<Pair<Node>> dupPairs = new HashSet<>();
        for (Node node : nodes) {
            dupPairs.addAll(node.getDuplicateNeighborPairs());
        }
        return new ArrayList<>(dupPairs);
    }

    // Unused
    // Generate all duplicate/indistinguishable relation pairs, used for inequality constraints
    ArrayList<Pair<Relation>> getDuplicateRelationPairs() {
        HashSet<Relation> dups = new HashSet<>();
        ArrayList<ArrayList<Relation>> dupSets = new ArrayList<>();
        for (int i = 0; i < relations.size(); ++i) {
            Relation outer = relations.get(i);
            if (dups.contains(outer))
                continue;
            ArrayList<Relation> dupSet = new ArrayList<>();
            dupSet.add(outer);
            for (int j = i + 1; j < relations.size(); ++j) {
                Relation inner = relations.get(j);
                if (!dups.contains(inner) && outer.duplicates(inner)) {
                    dups.add(inner);
                    dupSet.add(inner);
                }
            }
            dupSets.add(dupSet);
        }

        ArrayList<Pair<Relation>> dupPairs = new ArrayList<>();
        dupSets.forEach(dupSet -> {
            for (int i = 0; i < dupSet.size(); ++i)
                for (int j = i + 1; j < dupSet.size(); ++j)
                    dupPairs.add(Pair.ordered(dupSet.get(i), dupSet.get(j)));
        });

        return dupPairs;
    }

    public static void recount() {
        Entity.recount();
        Node.recount();
    }
}
