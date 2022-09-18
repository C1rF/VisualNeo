package visualneo.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Graph {

    private final ArrayList<Node> nodes;
    private final ArrayList<Relation> relations;

    public Graph(ArrayList<Node> nodes, ArrayList<Relation> relations) {
        this.nodes = nodes;
        this.relations = relations;

        validate();
    }

    private void validate() throws IllegalArgumentException {
        if (!checkNonNull())
            throw new IllegalArgumentException("Null/Empty Entity!");
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
            Iterator<Relation> relationIter = node.relationIter();
            while (relationIter.hasNext())
                if (!relations.contains(relationIter.next()))
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
        nodes.forEach(node -> {
            colorMap.put(node, false);
        });
        color(colorMap, nodes.get(0));

        return !colorMap.containsValue(false);
    }

    // Recursively colors nodes, depth first algorithm
    private void color(HashMap<Node, Boolean> colorMap, Node focus) {
        colorMap.replace(focus, true);
        focus.forEachRelation(relation -> {
            Node other = relation.other(focus);
            if (!colorMap.get(other))
                color(colorMap, other);
        });
    }
}
