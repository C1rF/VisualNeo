package hkust.edu.visualneo.utils.backend;

import org.neo4j.driver.Value;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static hkust.edu.visualneo.utils.backend.Consts.INITIAL_PRIME;
import static hkust.edu.visualneo.utils.backend.Consts.MULTIPLIER_PRIME;

abstract class Entity implements Comparable<Entity>, Expandable {

    protected final long id;
    final String label;

    final Map<String, Value> properties;

    // Pass null for an unlabeled node/relationship
    protected Entity(long id, String label, Map<String, Value> properties) {
        this.id = id;
        this.label = label;
        this.properties = Objects.requireNonNull(properties, "Property list is null!");
    }

    public boolean hasProperty() {
        return !properties.isEmpty();
    }

    public boolean hasLabel() {
        return label != null;
    }

    // Check whether two entities can match the same entity,
    // i.e., whether the two sets of nodes that the two entities match have non-empty intersection
    // This method assumes the other entity is non-null and the two entities are distinct
    boolean resembles(Entity other) {
        if (hasLabel() && other.hasLabel() && !label.equals(other.label))
            return false;

        for (String propertyKey : properties.keySet())
            if (other.properties.containsKey(propertyKey) &&
                !properties.get(propertyKey).equals(other.properties.get(propertyKey)))
                return false;

        return true;
    }

    //    public String elaborate() {
    //        StringBuilder propertiesStr = new StringBuilder();
    //        properties.forEach((key, value) ->
    //                propertiesStr.append("| |-").append(key).append(':').append(value)
    //                        .append(NEW_LINE));
    //
    //        return String.format("""
    //                %1$s
    //                |-Label: %2$s
    //                |-Properties: %3$s""",
    //                this,
    //                label == null ? "None" : label,
    //                properties.isEmpty() ? "None" : propertiesStr);
    //    }

    @Override
    public int hashCode() {
        int hash = INITIAL_PRIME;
        hash = MULTIPLIER_PRIME * hash + (int) id;
        hash = MULTIPLIER_PRIME * hash + (label == null ? 0 : label.hashCode());
        hash = MULTIPLIER_PRIME * hash + properties.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (!getClass().equals(other.getClass()))
            return false;
        return id == ((Entity) other).id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    // Doesn't check for null
    @Override
    public int compareTo(Entity other) {
        return (int) (id - other.id);
    }

    @Override
    public Map<Object, Object> expand() {
        Map<Object, Object> expansion = new LinkedHashMap<>();
        expansion.put("Label", label);
        expansion.put("Properties", properties);
        return expansion;
    }
}
