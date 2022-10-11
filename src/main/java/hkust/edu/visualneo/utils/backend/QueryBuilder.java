package hkust.edu.visualneo.utils.backend;

import org.neo4j.driver.Value;
import org.neo4j.driver.internal.types.InternalTypeSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static hkust.edu.visualneo.utils.backend.Consts.NEW_LINE;
import static hkust.edu.visualneo.utils.backend.Consts.SPACE;

// Class representing a Cypher query statement
public class QueryBuilder {

    private final static StringBuilder builder = new StringBuilder();

    private static int indent;

    // TODO: Modify this
    public static String translate(Graph graph) {
        clear();

        // MATCH clause
        increaseIndent();
        builder.append("MATCH");
        Iterator<Relation> relationIter = graph.relations.iterator();
        if (!relationIter.hasNext()) {
            newLine();
            translateNode(graph.nodes.iterator().next());
        }
        else {
            while (true) {
                Relation relation = relationIter.next();
                newLine();
                translateNode(relation.start);
                translateRelation(relation);
                translateNode(relation.end);
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
        ArrayList<Node> nodes = new ArrayList<>(graph.nodes);
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
                builder.append(pair.head());
                builder.append(" <> ");
                builder.append(pair.tail());
                if (!pairIter.hasNext())
                    break;
                builder.append(" AND");
            }
            decreaseIndent();
            newLine();
        }

        // RETURN clause
        increaseIndent();
        builder.append("RETURN");
        // TODO: Refine return conditions
        newLine();
        builder.append(nodes.get(0));
        decreaseIndent();

        System.out.println(new Expander().expand(graph));

        return builder.toString();
    }

    private static void clear() {
        builder.setLength(0);
        indent = 0;
    }

    private static void increaseIndent() {
        indent++;
    }

    private static void newLine() {
        builder.append(NEW_LINE);
        char[] tabs = new char[2 * indent];
        Arrays.fill(tabs, SPACE);
        builder.append(tabs);
    }

    private static void translateNode(Node node) {
        builder.append('(');
        builder.append(node);
        translateEntity(node);
        builder.append(')');
    }

    private static void translateRelation(Relation relation) {
        if (!relation.hasLabel() && !relation.hasProperty())
            builder.append("--");
        else {
            builder.append("-[");
            //            builder.append(relation.name());
            translateEntity(relation);
            builder.append("]-");
        }
        if (relation.directed)
            builder.append('>');
    }

    private static void decreaseIndent() {
        if (--indent < 0)
            indent = 0;
    }

    private static void translateEntity(Entity entity) {
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