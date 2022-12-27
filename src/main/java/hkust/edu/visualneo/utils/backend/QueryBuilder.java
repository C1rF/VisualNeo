package hkust.edu.visualneo.utils.backend;

import org.neo4j.driver.Value;
import org.neo4j.driver.internal.types.InternalTypeSystem;

import java.util.*;

import static hkust.edu.visualneo.utils.backend.Queries.NEW_LINE;

// Class representing a Cypher query statement
public class QueryBuilder {

    private final StringBuilder builder = new StringBuilder();

    private int indent;

    // TODO: Modify this
    public String translate(Graph graph) {
        clear();

        Set<Node> unusedNodes = new HashSet<>(graph.nodes);

        // MATCH clause
        increaseIndent();
        builder.append("MATCH");
        Iterator<Relation> relationIter = graph.relations.iterator();
        if (!relationIter.hasNext()) {
            newLine();
            translate(graph.nodes.iterator().next(), unusedNodes);
        }
        else {
            while (true) {
                Relation relation = relationIter.next();
                newLine();
                translate(relation.start, unusedNodes);
                translate(relation);
                translate(relation.end, unusedNodes);
                if (!relationIter.hasNext())
                    break;
                builder.append(',');
            }
        }
        decreaseIndent();
        newLine();

        // WHERE clause
        //        ArrayList<Pair<Node>> dupPairs = graph.getDuplicateNodePairs();
        // TODO: Modify this naive approach
        List<Pair<Node>> dupPairs = new ArrayList<>();
        List<Node> nodes = new ArrayList<>(graph.nodes);
        for (int i = 0; i < nodes.size(); ++i) {
            for (int j = i + 1; j < nodes.size(); ++j) {
                dupPairs.add(Pair.ordered(nodes.get(i), nodes.get(j)));
            }
        }
        if (!dupPairs.isEmpty()) {
            increaseIndent();
            builder.append("WHERE");
            Iterator<Pair<Node>> pairIter = dupPairs.iterator();
            while (true) {
                Pair<Node> pair = pairIter.next();
                newLine();
                builder.append(pair.head().getName());
                builder.append(" <> ");
                builder.append(pair.tail().getName());
                if (!pairIter.hasNext())
                    break;
                builder.append(" AND");
            }
            decreaseIndent();
            newLine();
        }

        // RETURN clause
        increaseIndent();
        builder.append("RETURN *");
        decreaseIndent();

        System.out.println(graph);

        return builder.toString();
    }

    private void clear() {
        builder.setLength(0);
        indent = 0;
    }

    private void increaseIndent() {
        indent++;
    }

    private void newLine() {
        builder.append(NEW_LINE);
        char[] tabs = new char[2 * indent];
        Arrays.fill(tabs, ' ');
        builder.append(tabs);
    }

    private void translate(Node node, Set<Node> unusedNodes) {
        builder.append('(');
        builder.append(node.getName());
        if (unusedNodes.remove(node))
            translateEntity(node);
        builder.append(')');
    }

    private void translate(Relation relation) {
        builder.append("-[");
        builder.append(relation.getName());
        translateEntity(relation);
        builder.append("]-");
        if (relation.directed)
            builder.append('>');
    }

    private void decreaseIndent() {
        if (--indent < 0)
            indent = 0;
    }

    private void translateEntity(Entity entity) {
        if (entity.hasLabel()) {
            builder.append(':');
            builder.append(entity.label);
        }

        if (entity.hasProperty()) {
            builder.append(" {");
            Iterator<String> propertyIter = entity.properties.keySet().iterator();
            while (true) {
                String propertyKey = propertyIter.next();
                builder.append(propertyKey);
                builder.append(": ");
                Value value = entity.properties.get(propertyKey);
                if (value.hasType(InternalTypeSystem.TYPE_SYSTEM.STRING()))
                    builder.append(String.format("'%s'", value));
                else
                    builder.append(value);
                if (!propertyIter.hasNext())
                    break;
                builder.append(", ");
            }
            builder.append('}');
        }
    }
}