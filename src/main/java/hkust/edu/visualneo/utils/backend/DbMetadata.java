package hkust.edu.visualneo.utils.backend;

import java.util.*;
import java.util.stream.Collectors;

import static hkust.edu.visualneo.utils.backend.Consts.NEW_LINE;

public record DbMetadata(
        Map<String, Integer> nodeCountsByLabel,
        Map<String, Integer> relationCountsByLabel,
        Map<String, Set<Pair<String>>> nodePropertiesByLabel,
        Map<String, Set<Pair<String>>> relationPropertiesByLabel,
        Graph schemaGraph) {

    public DbMetadata {
        Objects.requireNonNull(nodeCountsByLabel);
        Objects.requireNonNull(relationCountsByLabel);
        Objects.requireNonNull(nodePropertiesByLabel);
        Objects.requireNonNull(relationPropertiesByLabel);
        Objects.requireNonNull(schemaGraph);
    }

    public Set<String> nodeLabels() {
        return nodeCountsByLabel.keySet();
    }
    public Set<String> relationLabels() {
        return relationCountsByLabel.keySet();
    }

    public int nodeCount() {
        return nodeCountsByLabel.values().stream().reduce(0, Integer::sum);
    }
    public int relationCount() {
        return relationCountsByLabel.values().stream().reduce(0, Integer::sum);
    }

    public int nodeCountOf(String nodeLabel) {
        return nodeCountsByLabel.get(nodeLabel);
    }
    public int relationCountOf(String relationLabel) {
        return relationCountsByLabel.get(relationLabel);
    }

    public Set<Pair<String>> nodePropertiesOf(String nodeLabel) {
        return nodePropertiesByLabel.getOrDefault(nodeLabel, Collections.emptySet());
    }
    public Set<Pair<String>> relationPropertiesOf(String relationLabel) {
        return relationPropertiesByLabel.getOrDefault(relationLabel, Collections.emptySet());
    }

    public Set<String> sourcesOf(String relationLabel) {
        if (!relationLabels().contains(relationLabel))
            return Collections.emptySet();
        return schemaGraph.relations
                .stream()
                .filter(relation -> Objects.equals(relation.label, relationLabel))
                .map(relation -> relation.start.label)
                .collect(Collectors.toCollection(TreeSet::new));
    }
    public Set<String> targetsOf(String relationLabel) {
        if (!relationLabels().contains(relationLabel))
            return Collections.emptySet();
        return schemaGraph.relations
                .stream()
                .filter(relation -> Objects.equals(relation.label, relationLabel))
                .map(relation -> relation.end.label)
                .collect(Collectors.toCollection(TreeSet::new));
    }
    public Set<String> relationsFrom(String sourceLabel) {
        if (!nodeLabels().contains(sourceLabel))
            return Collections.emptySet();
        return schemaGraph.relations
                .stream()
                .filter(relation -> Objects.equals(relation.start.label, sourceLabel))
                .map(relation -> relation.label)
                .collect(Collectors.toCollection(TreeSet::new));
    }
    public Set<String> targetsFrom(String sourceLabel) {
        if (!nodeLabels().contains(sourceLabel))
            return Collections.emptySet();
        return schemaGraph.relations
                .stream()
                .filter(relation -> Objects.equals(relation.start.label, sourceLabel))
                .map(relation -> relation.end.label)
                .collect(Collectors.toCollection(TreeSet::new));
    }
    public Set<String> sourcesTo(String targetLabel) {
        if (!nodeLabels().contains(targetLabel))
            return Collections.emptySet();
        return schemaGraph.relations
                .stream()
                .filter(relation -> Objects.equals(relation.end.label, targetLabel))
                .map(relation -> relation.start.label)
                .collect(Collectors.toCollection(TreeSet::new));
    }
    public Set<String> relationsTo(String targetLabel) {
        if (!nodeLabels().contains(targetLabel))
            return Collections.emptySet();
        return schemaGraph.relations
                .stream()
                .filter(relation -> Objects.equals(relation.end.label, targetLabel))
                .map(relation -> relation.label)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // TODO: Add schemaGraph
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("------------------------------")
                .append(NEW_LINE);
        builder.append("Node Labels:")
                .append(NEW_LINE);
        nodeLabels().forEach(nodeLabel -> {
            builder.append("|-").append(nodeLabel)
                    .append(NEW_LINE);
            builder.append("| |-").append("Count: ").append(nodeCountOf(nodeLabel))
                    .append(NEW_LINE);
            builder.append("| |-").append("Properties");
            if (nodePropertiesOf(nodeLabel).isEmpty())
                builder.append(": None")
                        .append(NEW_LINE);
            else {
                builder.append(NEW_LINE);
                nodePropertiesOf(nodeLabel).forEach(property ->
                        builder.append("| | |-").append(property.head()).append(": ").append(property.tail())
                                .append(NEW_LINE));
            }
        });

        builder.append("------------------------------")
                .append(NEW_LINE);
        builder.append("Relation Labels:")
                .append(NEW_LINE);
        relationLabels().forEach(relationLabel -> {
            builder.append("|-").append(relationLabel)
                    .append(NEW_LINE);
            builder.append("| |-").append("Count: ").append(relationCountOf(relationLabel))
                    .append(NEW_LINE);
            builder.append("| |-").append("Properties");
            if (relationPropertiesOf(relationLabel).isEmpty())
                builder.append(": None")
                        .append(NEW_LINE);
            else {
                builder.append(NEW_LINE);
                relationPropertiesOf(relationLabel).forEach(property ->
                        builder.append("| | |-").append(property.head()).append(": ").append(property.tail())
                                .append(NEW_LINE));
            }
        });

        builder.append("------------------------------");

        return builder.toString();
    }

    // TODO: Remove this
    public Iterable<String> propertyKeys() {
        return Collections.emptyList();
    }
}
