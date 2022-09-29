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
