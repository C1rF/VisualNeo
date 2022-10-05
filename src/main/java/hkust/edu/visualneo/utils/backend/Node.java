package hkust.edu.visualneo.utils.backend;

import hkust.edu.visualneo.utils.frontend.Vertex;
import org.neo4j.driver.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class Node extends Entity {

    private static int nodeCount;

    private final int nodeId = ++nodeCount;

    final ArrayList<Relation> relations = new ArrayList<>();

    Node(String label, Map<String, Value> properties) {
        super(label, properties);
    }

    Node(Vertex vertex) {
        this(vertex.getLabel(), vertex.getProp());
    }

    boolean related() {
        return !relations.isEmpty();
    }

    int relationCount() {
        return relations.size();
    }

    // Two distinct node are indistinguishable in the neighborhood of this node
    // iff they and the relations between them and this node can match the same node
    ArrayList<Pair<Node>> getDuplicateNeighborPairs() {
        HashSet<Node> dups = new HashSet<>();
        ArrayList<ArrayList<Node>> dupSets = new ArrayList<>();
        for (int i = 0; i < relations.size(); ++i) {
            Relation outerRelation = relations.get(i);
            Node outerNode = outerRelation.other(this);
            if (dups.contains(outerNode))
                continue;
            ArrayList<Node> dupSet = new ArrayList<>();
            dupSet.add(outerNode);
            for (int j = i + 1; j < relations.size(); ++ j) {
                Relation innerRelation = relations.get(j);
                Node innerNode = innerRelation.other(this);
                if (!outerRelation.resembles(innerRelation))
                    continue;
                if (dups.contains(innerNode) || !outerNode.resembles(innerNode))
                    continue;
                dups.add(innerNode);
                dupSet.add(innerNode);
            }
            dupSets.add(dupSet);
        }

        ArrayList<Pair<Node>> dupPairs = new ArrayList<>();
        dupSets.forEach(dupSet -> {
            for (int i = 0; i < dupSet.size(); ++i)
                for (int j = i + 1; j < dupSet.size(); ++j)
                    dupPairs.add(Pair.ordered(dupSet.get(i), dupSet.get(j)));
        });

        return dupPairs;
    }

    // Check whether two distinct nodes can match the same node
    // This method assumes the other node is non-null
    @Override
    boolean resembles(Entity other) {
        if (!(other instanceof Node))
            return false;
        if (this == other)
            return false;
        return super.resembles(other);
    }

    void attach(Relation relation) {
        relations.add(relation);
    }

    void detach(Relation relation) {
        relations.remove(relation);
    }

    static void recount() {
        nodeCount = 0;
    }

    @Override
    public String toString() {
        return "n" + String.valueOf(nodeId);
    }
}
