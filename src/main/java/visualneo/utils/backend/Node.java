package visualneo.utils.backend;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

import visualneo.utils.frontend.Vertex;

public class Node extends Entity {

    private static int nodeCount;

    private final ArrayList<Relation> relations = new ArrayList<>();

    public Node(String label) {
        super(label);
    }

    public Node(Vertex vertex) {
        this((String) null);
    }

    boolean related() {
        return !relations.isEmpty();
    }

    int relationCount() {
        return relations.size();
    }

    Iterator<Relation> relationIter() {
        return relations.iterator();
    }

    void forEachRelation(Consumer<Relation> action) {
        relations.forEach(action);
    }

    void attach(Relation relation) {
        relations.add(relation);
    }

    void detach(Relation relation) {
        relations.remove(relation);
    }

    @Override
    String generateVarName() {
        return ("n" + String.valueOf(++nodeCount));
    }
}
