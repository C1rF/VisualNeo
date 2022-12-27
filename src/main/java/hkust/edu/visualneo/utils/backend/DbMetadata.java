package hkust.edu.visualneo.utils.backend;

import java.util.*;
import java.util.stream.Collectors;

public record DbMetadata(
        Map<String, Integer> nodeCountsByLabel,
        Map<String, Integer> relationCountsByLabel,
        Map<String, Map<String, String>> nodePropertiesByLabel,
        Map<String, Map<String, String>> relationPropertiesByLabel,
        Graph schemaGraph) implements Mappable {

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

    public int nodeCountOf(String nodeLabel) {
        return nodeCountsByLabel.getOrDefault(nodeLabel, 0);
    }

    public int relationCountOf(String relationLabel) {
        return relationCountsByLabel.getOrDefault(relationLabel, 0);
    }

    public Set<String> nodeLabels() {
        return nodeCountsByLabel.keySet();
    }

    public Set<String> relationLabels() {
        return relationCountsByLabel.keySet();
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

    public Map<String, String> nodeProperties() {
        return nodePropertiesByLabel
                .values()
                .stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e2,
                        TreeMap::new));
    }

    public Map<String, String> relationProperties() {
        return relationPropertiesByLabel
                .values()
                .stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e2,
                        TreeMap::new));
    }

    public Map<String, String> nodePropertiesOf(String nodeLabel) {
        return nodePropertiesByLabel.get(nodeLabel);
    }

    public Map<String, String> relationPropertiesOf(String relationLabel) {
        return relationPropertiesByLabel.get(relationLabel);
    }

    @Override
    public String toString() {
        return new TreePrinter().print(getName(), toMap());
    }

    @Override
    public String getName() {
        return "Database Metadata";
    }

    @Override
    public Map<Object, Object> toMap() {
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
}
