package hkust.edu.visualneo.utils.backend;

import java.util.ArrayList;
import java.util.HashSet;

import hkust.edu.visualneo.utils.frontend.Vertex;

public class Node extends Entity {

    private static int nodeCount;

    private final int nodeId = ++nodeCount;

    final ArrayList<Relation> relations = new ArrayList<>();

    Node(String label) {
        super(label);
    }

    Node(Vertex vertex) {
        this((String) null);
    }

    boolean related() {
        return !relations.isEmpty();
    }

    int relationCount() {
        return relations.size();
    }

    // Two distinct node are indistinguishable in the neighborhood of this node
    // iff they and the relations between them and this node have the same label and properties
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
                    dupPairs.add(new Pair<>(dupSet.get(i), dupSet.get(j)));
        });

        return dupPairs;
    }

    // Check whether two distinct nodes have the same label and properties
    // This method assumes the other node is non-null
    boolean resembles(Node other) {
        if (this == other)
            return false;
        if (!label.equals(other.label))
            return false;
        //TODO Add equality check on properties
        return true;
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
        return "r" + String.valueOf(nodeId);
    }
}
