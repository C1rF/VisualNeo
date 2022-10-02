package hkust.edu.visualneo.utils.backend;

import java.util.ArrayList;
import java.util.Objects;

public record DbMetadata(
        ArrayList<String> nodeLabels,
        ArrayList<String> relationLabels,
        ArrayList<String> propertyKeys) {

    public DbMetadata {
        Objects.requireNonNull(nodeLabels);
        Objects.requireNonNull(relationLabels);
        Objects.requireNonNull(propertyKeys);
    }

    @Override
    public String toString() {
        return String.format("""
                        Node Labels:     %1$s
                        Relation Labels: %2$s
                        Property Keys:   %3$s""",
                nodeLabels().toString(),
                relationLabels().toString(),
                propertyKeys().toString());
    }
}
