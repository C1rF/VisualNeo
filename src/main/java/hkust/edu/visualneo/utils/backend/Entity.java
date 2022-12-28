package hkust.edu.visualneo.utils.backend;

import org.neo4j.driver.Value;

import java.util.*;

abstract class Entity implements Comparable<Entity>, Mappable {

    protected static final int INITIAL_PRIME = 17;
    protected static final int MULTIPLIER_PRIME = 37;

    protected final long id;
    final String label;
    final Map<String, Value> properties;

    // Pass null for an unlabeled node/relationship
    protected Entity(long id, String label, Map<String, Value> properties) {
        this.id = id;
        this.label = label;
        this.properties = properties == null ? Collections.emptyMap() : properties;
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
    public boolean resembles(Entity other) {
        if (hasLabel() && other.hasLabel() && !label.equals(other.label))
            return false;

        for (String propertyKey : properties.keySet())
            if (other.properties.containsKey(propertyKey) &&
                !properties.get(propertyKey).equals(other.properties.get(propertyKey)))
                return false;

        return true;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return new TreePrinter().print(getName(), toMap());
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Label", label);
        map.put("Properties", properties);
        return map;
    }

    @Override
    public int hashCode() {
//        int hash = INITIAL_PRIME;
//        hash = MULTIPLIER_PRIME * hash + (int) id;
//        hash = MULTIPLIER_PRIME * hash + (label == null ? 0 : label.hashCode());
//        hash = MULTIPLIER_PRIME * hash + properties.hashCode();
//        return hash;
        return (int) (id ^ (id >>> 32));  // Implementation by Neo4j
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

    // Doesn't check for null
    @Override
    public int compareTo(Entity other) {
        return (int) (id - other.id);
    }
}
