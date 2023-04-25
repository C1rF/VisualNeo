package hkust.edu.visualneo.utils.backend;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.neo4j.driver.Value;

import java.util.*;

import static hkust.edu.visualneo.utils.backend.Queries.singletonQuery;

// Class representing a Cypher query statement
public class QueryBuilder {

    public static final int MAXIMUM_RECORDS = 100;

    private static final String NEW_LINE = System.lineSeparator();
    private static final String TAB = "  ";
    private static final String NEW_LINE_INDENT = NEW_LINE + TAB;

    private final StringBuilder buffer = new StringBuilder();

    private final StringProperty translation = new SimpleStringProperty(this, "translation", null);

    public String translate(Graph graph, boolean simple) {
        if (graph.isEmpty())
            throw new Graph.BadTopologyException(Graph.BadTopologyException.TopologyType.EMPTY);
        if (!graph.isConnected())
            throw new Graph.BadTopologyException(Graph.BadTopologyException.TopologyType.DISCONNECTED);
        graph.index();

        if (graph.getRelations().isEmpty()) {
            Node singleton = graph.getNodes().iterator().next();
            translateEntity(singleton);
            String translation = buffer.toString();
            clear();
            return singletonQuery(translation, simple);
        }

        Set<Node> unusedNodes = new HashSet<>(graph.getNodes());
        Collection<String> nodeNames = graph.getNodes().stream().map(Node::getName).toList();
        Collection<String> relationNames = graph.getRelations().stream().map(Relation::getName).toList();

        String keywordSeparator = simple ? " " : NEW_LINE_INDENT;
        String commaSeparator = simple ? ", " : "," + NEW_LINE_INDENT;
        String andSeparator = simple ? " AND " : " AND" + NEW_LINE_INDENT;

        // CALL clause
        if (!simple) {
            buffer.append("CALL {");
            buffer.append(keywordSeparator);

            Node first = graph.getNodes().iterator().next();

            buffer.append("MATCH");
            buffer.append(keywordSeparator).append(TAB);
            translate(first, unusedNodes);
            buffer.append(keywordSeparator);

            buffer.append("RETURN");
            buffer.append(keywordSeparator).append(TAB);
            buffer.append(first.getName());
            buffer.append(keywordSeparator);

            buffer.append("LIMIT");
            buffer.append(keywordSeparator).append(TAB);
            buffer.append(MAXIMUM_RECORDS);
            buffer.append(NEW_LINE);

            buffer.append("}").append(NEW_LINE);
        }

        // MATCH clause
        buffer.append("MATCH");
        buffer.append(keywordSeparator);


        Iterator<Relation> relationIt = graph.getRelations().iterator();
        Relation relation = relationIt.next();
        translate(relation.start, unusedNodes);
        translate(relation);
        translate(relation.end, unusedNodes);
        while (relationIt.hasNext()) {
            buffer.append(commaSeparator);
            relation = relationIt.next();
            translate(relation.start, unusedNodes);
            translate(relation);
            translate(relation.end, unusedNodes);
        }

        buffer.append(NEW_LINE);

        // WHERE clause
        List<Pair<Node>> dupPairs = new ArrayList<>();
        List<Node> nodes = new ArrayList<>(graph.getNodes());
        for (int i = 0; i < nodes.size(); ++i) {
            for (int j = i + 1; j < nodes.size(); ++j) {
                Node left = nodes.get(i);
                Node right = nodes.get(j);
                if (left.resembles(right))
                    dupPairs.add(Pair.ordered(left, right));
            }
        }

        if (!dupPairs.isEmpty()) {
            buffer.append("WHERE");
            buffer.append(keywordSeparator);

            Iterator<Pair<Node>> pairIt = dupPairs.iterator();
            while (true) {
                Pair<Node> pair = pairIt.next();
                buffer.append(pair.head().getName());
                buffer.append(" <> ");
                buffer.append(pair.tail().getName());
                if (!pairIt.hasNext())
                    break;
                buffer.append(andSeparator);
            }
            buffer.append(NEW_LINE);
        }

        if (simple)
            buffer.append("RETURN *");
        else {
            // WITH and UNWIND clauses (for grouping distinct nodes and relations)
            buffer.append("WITH");
            buffer.append(NEW_LINE_INDENT);

            buffer.append("[");
            buffer.append(String.join(", ", nodeNames));
            buffer.append("] AS allNodes");
            buffer.append(commaSeparator);
            buffer.append("[");
            buffer.append(String.join(", ", relationNames));
            buffer.append("] AS allRelationships");
            buffer.append(NEW_LINE);

            buffer.append("UNWIND");
            buffer.append(NEW_LINE_INDENT);

            buffer.append("allNodes AS n");
            buffer.append(NEW_LINE);

            buffer.append("UNWIND");
            buffer.append(NEW_LINE_INDENT);

            buffer.append("allRelationships AS r");
            buffer.append(NEW_LINE);

            // RETURN clause
            buffer.append("RETURN");
            buffer.append(NEW_LINE_INDENT);

            buffer.append("collect(DISTINCT n) AS nodes");
            buffer.append(commaSeparator);
            buffer.append("collect(DISTINCT r) AS relationships");
            buffer.append(commaSeparator);
            buffer.append("collect(DISTINCT [[n IN allNodes | ID(n)], [r IN allRelationships | ID(r)]]) AS resultIds");
        }

        String query = buffer.toString();
        clear();

        return query;
    }

    public void update(Graph graph) {
        try {
            setTranslation(translate(graph, true));
        }
        catch (Graph.BadTopologyException e) {
            setTranslation("");
        }
    }

    public StringProperty translationProperty() {
        return translation;
    }
    public String getTranslation() {
        return translationProperty().get();
    }
    private void setTranslation(String translation) {
        translationProperty().set(translation);
    }

    private void clear() {
        buffer.setLength(0);
    }

    private void translate(Node node, Set<Node> unusedNodes) {
        buffer.append('(');
        buffer.append(node.getName());
        if (unusedNodes.remove(node))
            translateEntity(node);
        buffer.append(')');
    }

    private void translate(Relation relation) {
        buffer.append("-[");
        buffer.append(relation.getName());
        translateEntity(relation);
        buffer.append("]-");
        if (relation.directed)
            buffer.append('>');
    }

    private void translateEntity(Entity entity) {
        if (entity.hasLabel()) {
            buffer.append(':');
            buffer.append(entity.getLabel());
        }

        if (entity.hasProperties()) {
            buffer.append(" {");
            Iterator<String> propertyIt = entity.getProperties().keySet().iterator();
            while (true) {
                String propertyKey = propertyIt.next();
                buffer.append(propertyKey);
                buffer.append(": ");
                Value value = entity.getProperties().get(propertyKey);
                buffer.append(value);
                if (!propertyIt.hasNext())
                    break;
                buffer.append(", ");
            }
            buffer.append('}');
        }
    }
}