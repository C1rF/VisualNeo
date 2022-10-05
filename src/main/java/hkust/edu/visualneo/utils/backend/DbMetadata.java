package hkust.edu.visualneo.utils.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record DbMetadata(
        int nodeCount,
        int relationCount,
        Map<String, Integer> nodeCountByLabel,
        Map<String, Integer> relationCountByLabel,
        ArrayList<String> propertyKeys) {

    public DbMetadata {
        Objects.requireNonNull(nodeCountByLabel);
        Objects.requireNonNull(relationCountByLabel);
        Objects.requireNonNull(propertyKeys);
    }

    public ArrayList<String> nodeLabels() {
        return new ArrayList<>(nodeCountByLabel.keySet());
    }

    public ArrayList<String> relationLabels() {
        return new ArrayList<>(relationCountByLabel.keySet());
    }

    public int nodeCountOf(String label) {
        return nodeCountByLabel.get(label);
    }

    public int relationCountOf(String label) {
        return nodeCountByLabel.get(label);
    }

    @Override
    public String toString() {
        List<String> nodeLabelsWithCount = nodeCountByLabel.entrySet()
                .stream().map(entry -> String.format("%1$s(%2$s)", entry.getKey(), entry.getValue())).toList();
        List<String> relationLabelsWithCount = relationCountByLabel.entrySet()
                .stream().map(entry -> String.format("%1$s(%2$s)", entry.getKey(), entry.getValue())).toList();
        return String.format("""
                             Node Count: %1$s
                         Relation Count: %2$s
                            Node Labels: %3$s
                        Relation Labels: %4$s
                          Property Keys: %5$s""",
                nodeCount,
                relationCount,
                nodeLabelsWithCount,
                relationLabelsWithCount,
                propertyKeys);
    }
}
