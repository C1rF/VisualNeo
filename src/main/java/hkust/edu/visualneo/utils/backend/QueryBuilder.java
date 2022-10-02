package hkust.edu.visualneo.utils.backend;

import org.neo4j.driver.Value;
import org.neo4j.driver.internal.types.InternalTypeSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

// Class representing a Cypher query statement
public class QueryBuilder {

    private final static StringBuilder builder = new StringBuilder();
    private static int indentCount;

    public final static String metadataQuery = """
            CALL
              db.labels() YIELD label
            WITH
              label ORDER BY label
            WITH
              collect(label) AS labels
            CALL
              db.relationshipTypes() YIELD relationshipType
            WITH
              labels, relationshipType ORDER BY relationshipType
            WITH
              labels, collect(relationshipType) AS relationshipTypes
            CALL
              db.propertyKeys() YIELD propertyKey
            WITH
              labels, relationshipTypes, propertyKey ORDER BY propertyKey
            WITH
              labels, relationshipTypes, collect(propertyKey) AS propertyKeys
            RETURN
              labels, relationshipTypes, propertyKeys""";

    //TODO Modify this
    public static String translate(Graph graph) {
        clear();

        // MATCH clause
        indent();
        builder.append("MATCH");
        Iterator<Relation> relationIter = graph.relations.iterator();
        if (!relationIter.hasNext()) {
            newLine();
            translateNode(graph.nodes.get(0));
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
        unindent();
        newLine();

        // WHERE clause
//        ArrayList<Pair<Node>> dupPairs = graph.getDuplicateNodePairs();
        ArrayList<Pair<Node>> dupPairs = new ArrayList<>();
        for (int i = 0; i < graph.nodes.size(); ++i) {
            for (int j = i + 1; j < graph.nodes.size(); ++j) {
                dupPairs.add(new Pair<>(graph.nodes.get(i), graph.nodes.get(j)));
            }
        }
        if (!dupPairs.isEmpty()) {
            indent();
            builder.append("WHERE");
            Iterator<Pair<Node>> pairIter = dupPairs.iterator();
            while (true) {
                Pair<Node> pair = pairIter.next();
                newLine();
                builder.append(pair.head);
                builder.append(" <> ");
                builder.append(pair.tail);
                if (!pairIter.hasNext())
                    break;
                builder.append(" AND");
            }
            unindent();
            newLine();
        }

        // RETURN clause
        indent();
        builder.append("RETURN");
        //TODO Refine return conditions
        newLine();
        builder.append(graph.nodes.get(0));
        unindent();

        return builder.toString();
    }

    private static void indent() {
        indentCount++;
    }

    private static void unindent() {
        if (--indentCount < 0)
            indentCount = 0;
    }

    private static void newLine() {
        builder.append(System.lineSeparator());
        char[] tabs = new char[2 * indentCount];
        Arrays.fill(tabs, ' ');
        builder.append(tabs);
    }

    private static void clear() {
        builder.setLength(0);
        indentCount = 0;
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
//            builder.append(relation);
            translateEntity(relation);
            builder.append("]-");
        }
        if (relation.directed)
            builder.append('>');
    }
}