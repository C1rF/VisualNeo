package neo4jvqi.utils;

import java.util.UUID;

abstract class Entity {

    private final UUID id = UUID.randomUUID();
    final String varName;
    final String label;

    // Pass null for an unlabeled node/relationship
    protected Entity(String label) {
        this.label = label;
        varName = generateVarName();
    }

    boolean isLabeled() {
        return (label != null);
    }

    abstract String generateVarName();

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Entity))
            return false;
        Entity other = (Entity)obj;
        return this.id.equals(other.id);
    }
}
