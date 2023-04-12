package hkust.edu.visualneo.utils.backend;

import org.neo4j.driver.Value;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Entity implements Comparable<Entity>, Mappable {

    protected static final int INITIAL_PRIME = 17;
    protected static final int MULTIPLIER_PRIME = 37;

    protected final long id;
    protected int index = -1;
    private final String label;
    private final Map<String, Value> properties;

    // Pass null for an unlabeled node/relationship
    protected Entity(long id, String label, Map<String, Value> properties) {
        this.id = id;
        this.label = label;
        this.properties = properties == null ? Collections.emptyMap() : properties;
    }

    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    public Map<String, Value> getProperties() {
        return properties;
    }

    public boolean hasLabel() {
        return label != null;
    }

    public String getLabel() { return label; }

    public long getId() {
        return id;
    }

    public void setIndex(int index) {
        if (this.index != -1 || index < 0)
            return;
        this.index = index;
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

    // No null check
    @Override
    public int compareTo(Entity other) {
        return (int) (id - other.id);
    }
}
