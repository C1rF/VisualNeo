package hkust.edu.visualneo.utils.backend;

import org.neo4j.driver.Value;

import java.util.Map;
import java.util.Objects;

import static hkust.edu.visualneo.utils.backend.Consts.INITIAL_PRIME;
import static hkust.edu.visualneo.utils.backend.Consts.MULTIPLIER_PRIME;

abstract class Entity implements Comparable<Entity> {

    private static int count;

    private final int id = ++count;
    final String label;

    final Map<String, Value> properties;

    // Pass null for an unlabeled node/relationship
    protected Entity(String label, Map<String, Value> properties) {
        this.label = label;
        this.properties = Objects.requireNonNull(properties, "Property list is null!");
    }
    // TODO: Add constructor with assigned ID

    public boolean hasLabel() {
        return label != null;
    }

    public boolean hasProperty() {
        return !properties.isEmpty();
    }

    public static void recount() {
        count = 0;
    }

    // Check whether two entities can match the same entity,
    // i.e., whether the two sets of nodes that the two entities match have non-empty intersection
    // This method assumes the other entity is non-null and the two entities are distinct
    boolean resembles(Entity other) {
        if (hasLabel() && other.hasLabel() && !label.equals(other.label))
            return false;
        for (String propertyKey : properties.keySet()) {
            if (other.properties.containsKey(propertyKey) &&
                    !properties.get(propertyKey).equals(other.properties.get(propertyKey)))
                return false;
        }
        return true;
    }

    public String name() {
        return 'e' + String.valueOf(id);
    }

    @Override
    public int hashCode() {
        int hash = INITIAL_PRIME;
        hash = MULTIPLIER_PRIME * hash + id;
        hash = MULTIPLIER_PRIME * hash + (label == null ? 0 : label.hashCode());
        hash = MULTIPLIER_PRIME * hash + properties.hashCode();
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
    public String toString() {
        return String.format("""
                %1$s:
                Label: %2$s
                Properties: %3$s""",
                name(),
                label,
                properties);
    }

    // Doesn't check for null
    @Override
    public int compareTo(Entity other) {
        return id - other.id;
    }
}
