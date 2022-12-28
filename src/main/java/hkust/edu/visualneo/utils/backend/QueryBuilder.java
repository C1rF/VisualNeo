package hkust.edu.visualneo.utils.backend;

import org.neo4j.driver.Value;

import java.util.*;

import static hkust.edu.visualneo.utils.backend.Queries.NEW_LINE;
import static hkust.edu.visualneo.utils.backend.Queries.singletonQuery;

// Class representing a Cypher query statement
public class QueryBuilder {

    private final StringBuilder buffer = new StringBuilder();

    private int indent;

    // TODO: Modify this
    public String translate(Graph graph) {
        if (graph.relations().isEmpty()) {
            Node singleton = graph.nodes().iterator().next();
            translateEntity(singleton);
            String translation = buffer.toString();
            clear();
            return singletonQuery(singleton.getName(), translation);
        }

        Set<Node> unusedNodes = new HashSet<>(graph.nodes());
        Collection<String> nodeNames = graph.nodes().stream().map(Node::getName).toList();
        Collection<String> relationNames = graph.relations().stream().map(Relation::getName).toList();

        // MATCH clause
        increaseIndent();
        buffer.append("MATCH");
        Iterator<Relation> relationIt = graph.relations().iterator();
        while (true) {
            Relation relation = relationIt.next();
            newLine();
            translate(relation.start, unusedNodes);
            translate(relation);
            translate(relation.end, unusedNodes);
            if (!relationIt.hasNext())
                break;
            buffer.append(',');
        }
        decreaseIndent();
        newLine();

        // WHERE clause
        // TODO: Modify this naive approach
        List<Pair<Node>> dupPairs = new ArrayList<>();
        List<Node> nodes = new ArrayList<>(graph.nodes());
        for (int i = 0; i < nodes.size(); ++i) {
            for (int j = i + 1; j < nodes.size(); ++j) {
                dupPairs.add(Pair.ordered(nodes.get(i), nodes.get(j)));
            }
        }
        if (!dupPairs.isEmpty()) {
            increaseIndent();
            buffer.append("WHERE");
            Iterator<Pair<Node>> pairIt = dupPairs.iterator();
            while (true) {
                Pair<Node> pair = pairIt.next();
                newLine();
                buffer.append(pair.head().getName());
                buffer.append(" <> ");
                buffer.append(pair.tail().getName());
                if (!pairIt.hasNext())
                    break;
                buffer.append(" AND");
            }
            decreaseIndent();
            newLine();
        }

        // WITH and UNWIND clauses (for grouping distinct nodes and relations)
        increaseIndent();
        buffer.append("WITH");
        newLine();
        buffer.append('[');
        buffer.append(String.join(", ", nodeNames));
        buffer.append(']');
        buffer.append(" AS allNodes");
        buffer.append(',');
        newLine();
        buffer.append('[');
        buffer.append(String.join(", ", relationNames));
        buffer.append(']');
        buffer.append(" AS allRelationships");
        decreaseIndent();
        newLine();

        buffer.append("UNWIND allNodes AS n");
        newLine();
        buffer.append("UNWIND allRelationships AS r");
        newLine();

        // RETURN clause
        increaseIndent();
        buffer.append("RETURN");
        newLine();
        buffer.append("collect(DISTINCT n) AS nodes");
        buffer.append(',');
        newLine();
        buffer.append("collect(DISTINCT r) AS relationships");
        buffer.append(',');
        newLine();
        buffer.append("collect(DISTINCT [[n IN allNodes | ID(n)], [r IN allRelationships | ID(r)]]) AS resultIds");
        decreaseIndent();

        System.out.println(graph);

        String query = buffer.toString();
        clear();
        return query;
    }

    private void clear() {
        buffer.setLength(0);
        indent = 0;
    }

    private void increaseIndent() {
        indent++;
    }

    private void newLine() {
        buffer.append(NEW_LINE);
        char[] tabs = new char[2 * indent];
        Arrays.fill(tabs, ' ');
        buffer.append(tabs);
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

    private void decreaseIndent() {
        if (--indent < 0)
            indent = 0;
    }

    private void translateEntity(Entity entity) {
        if (entity.hasLabel()) {
            buffer.append(':');
            buffer.append(entity.label);
        }

        if (entity.hasProperty()) {
            buffer.append(" {");
            Iterator<String> propertyIt = entity.properties.keySet().iterator();
            while (true) {
                String propertyKey = propertyIt.next();
                buffer.append(propertyKey);
                buffer.append(": ");
                Value value = entity.properties.get(propertyKey);
                buffer.append(value);
                if (!propertyIt.hasNext())
                    break;
                buffer.append(", ");
            }
            buffer.append('}');
        }
    }
}