package hkust.edu.visualneo.utils.backend;

import java.util.*;
import java.util.stream.Collectors;

public record DbMetadata(
        Map<String, Integer> nodeCountsByLabel,
        Map<String, Integer> relationCountsByLabel,
        Map<String, Map<String, String>> nodePropertiesByLabel,
        Map<String, Map<String, String>> relationPropertiesByLabel,
        Graph schemaGraph) implements Expandable {

    public DbMetadata {
        Objects.requireNonNull(nodeCountsByLabel);
        Objects.requireNonNull(relationCountsByLabel);
        Objects.requireNonNull(nodePropertiesByLabel);
        Objects.requireNonNull(relationPropertiesByLabel);
        Objects.requireNonNull(schemaGraph);
    }

    public int nodeCount() {
        return nodeCountsByLabel.values().stream().reduce(0, Integer::sum);
    }

    public int relationCount() {
        return relationCountsByLabel.values().stream().reduce(0, Integer::sum);
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

    public Set<String> relationLabels() {
        return relationCountsByLabel.keySet();
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

    public Set<String> nodeLabels() {
        return nodeCountsByLabel.keySet();
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

    @Override
    public String toString() {
        return "Database Metadata";
    }

    @Override
    public Map<Object, Object> expand() {
        Map<Object, Object> expansion = new LinkedHashMap<>();

        Map<Object, Object> nodesExpansion = new LinkedHashMap<>();
        nodeLabels().forEach(nodeLabel -> {
            Map<Object, Object> nodeLabelExpansion = new LinkedHashMap<>();
            nodeLabelExpansion.put("Count", nodeCountOf(nodeLabel));
            Map<String, String> properties = nodePropertiesOf(nodeLabel);
            nodeLabelExpansion.put("Properties", properties);
            nodesExpansion.put(nodeLabel, nodeLabelExpansion);
        });
        expansion.put("Node Labels", nodesExpansion);

        Map<Object, Object> relationsExpansion = new LinkedHashMap<>();
        relationLabels().forEach(relationLabel -> {
            Map<Object, Object> relationLabelExpansion = new LinkedHashMap<>();
            relationLabelExpansion.put("Count", relationCountOf(relationLabel));
            Map<String, String> properties = relationPropertiesOf(relationLabel);
            relationLabelExpansion.put("Properties", properties);
            relationsExpansion.put(relationLabel, relationLabelExpansion);
        });
        expansion.put("Relation Labels", relationsExpansion);

        expansion.put("Schema Graph", schemaGraph);

        return expansion;
    }

    public int nodeCountOf(String nodeLabel) {
        return nodeCountsByLabel.getOrDefault(nodeLabel, 0);
    }

    public Map<String, String> nodePropertiesOf(String nodeLabel) {
        return nodePropertiesByLabel.getOrDefault(nodeLabel, Collections.emptyMap());
    }

    //    @Override
    //    public String toString() {
    //        StringBuilder builder = new StringBuilder();
    //
    //        char[] sep = Consts.separator(30);
    //
    //        builder.append(sep)
    //                .append(NEW_LINE);
    //
    //        builder.append("Node Labels")
    //                .append(NEW_LINE);
    //        nodeLabels().forEach(nodeLabel -> {
    //            builder.append("|-").append(nodeLabel)
    //                    .append(NEW_LINE);
    //            builder.append("| |-").append("Count: ").append(nodeCountOf(nodeLabel))
    //                    .append(NEW_LINE);
    //            builder.append("| |-").append("Properties");
    //            if (nodePropertiesOf(nodeLabel).isEmpty())
    //                builder.append(": None")
    //                        .append(NEW_LINE);
    //            else {
    //                builder.append(NEW_LINE);
    //                nodePropertiesOf(nodeLabel).forEach((key, value) ->
    //                        builder.append("| | |-").append(key).append(": ").append(value)
    //                                .append(NEW_LINE));
    //            }
    //        });
    //
    //        builder.append(sep)
    //                .append(NEW_LINE);
    //
    //        builder.append("Relation Labels")
    //                .append(NEW_LINE);
    //        relationLabels().forEach(relationLabel -> {
    //            builder.append("|-").append(relationLabel)
    //                    .append(NEW_LINE);
    //            builder.append("| |-").append("Count: ").append(relationCountOf(relationLabel))
    //                    .append(NEW_LINE);
    //            builder.append("| |-").append("Properties");
    //            if (relationPropertiesOf(relationLabel).isEmpty())
    //                builder.append(": None")
    //                        .append(NEW_LINE);
    //            else {
    //                builder.append(NEW_LINE);
    //                relationPropertiesOf(relationLabel).forEach((key, value) ->
    //                        builder.append("| | |-").append(key).append(": ").append(value)
    //                                .append(NEW_LINE));
    //            }
    //        });
    //
    //        builder.append(sep)
    //                .append(NEW_LINE);
    //
    //        return builder.toString();
    //    }

    public int relationCountOf(String relationLabel) {
        return relationCountsByLabel.getOrDefault(relationLabel, 0);
    }

    public Map<String, String> relationPropertiesOf(String relationLabel) {
        return relationPropertiesByLabel.getOrDefault(relationLabel, Collections.emptyMap());
    }

    // TODO: Remove this
    public Iterable<String> propertyKeys() {
        return Collections.emptyList();
    }
}
