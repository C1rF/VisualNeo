package hkust.edu.visualneo.utils.backend;

abstract class Entity implements Comparable<Entity> {

    private static int count;

    private final int id = ++count;
    final String label;

    //TODO Add properties

    // Pass null for an unlabeled node/relationship
    protected Entity(String label) {
        this.label = label;
    }

    boolean isLabeled() {
        return (label != null);
    }

    boolean hasProperty() {
        //TODO Implement this
        return false;
    }

    static void recount() {
        count = 0;
    }

    // Check whether two entities have the same label and properties
    // This method assumes the other entity is non-null and the two entities are distinct
    boolean resembles(Entity other) {
        if (label == null)
            return other.label == null;
        if (!label.equals(other.label))
            return false;
        //TODO Add equality check on properties
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + id;
        hash = 37 * hash + (label == null ? 0 : label.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Entity other))
            return false;
        return id == other.id;
    }

    @Override
    public int compareTo(Entity other) {
        return id - other.id;
    }
}
