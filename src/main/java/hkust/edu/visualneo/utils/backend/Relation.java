package hkust.edu.visualneo.utils.backend;

public class Relation extends Entity {

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

    // Check whether two relations are duplicate/indistinguishable
    // Two distinct relations are indistinguishable iff they have the same origin, target, label, and properties
    // This method assumes the other relation is non-null and the two relations are distinct
    boolean duplicates(Relation other) {
//        if (other == null || this == other)
//            return false;

        if (directed && other.directed) {
            if (start != other.start || end != other.end)
                return false;
        }
        else if ((start != other.start || end != other.end) && (start != other.end || end != other.start))
            return false;

        return resembles(other);
    }

    // Check whether two relations have the same label and properties
    // This method assumes the other relation is non-null and the two relations are distinct
    @Override
    boolean resembles(Entity other) {
        if (!(other instanceof Relation))
            return false;
        return super.resembles(other);
    }

    void detach() {
        start.detach(this);
        end.detach(this);
    }
}
