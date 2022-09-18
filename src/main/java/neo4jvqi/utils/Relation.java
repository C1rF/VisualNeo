package neo4jvqi.utils;

public class Relation extends Entity {

    private static int relationCount;

    final boolean directed;

    final Node start;
    final Node end;

    public Relation(boolean directed, Node start, Node end, String label) throws IllegalArgumentException {
        super(label);
        this.directed = directed;
        if (start == null || end == null)
            throw new IllegalArgumentException("A relation cannot connect null node(s)!");
        start.attach(this);
        end.attach(this);
        this.start = start;
        this.end = end;
    }

    Node other(Node node) {
        if (node == start)
            return end;
        if (node == end)
            return start;
        return null;
    }

    void detach() {
        start.detach(this);
        end.detach(this);
    }

    @Override
    String generateVarName() {
        return ("r" + String.valueOf(++relationCount));
    }
}
