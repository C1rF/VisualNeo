package hkust.edu.visualneo.utils.backend;

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
                builder.append(",");
            }
        }
        unindent();
        newLine();

        // WHERE clause
        ArrayList<Pair<Node>> dupPairs = graph.getDuplicateNodePairs();
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

    private static void translateNode(Node node) {
        builder.append("(");
        builder.append(node);
        if (node.isLabeled()) {
            builder.append(":");
            builder.append(node.label);
        }
        //TODO Add property translation
        builder.append(")");
    }

    private static void translateRelation(Relation relation) {
        if (!relation.isLabeled() && !relation.hasProperty())
            builder.append("--");
        else {
            builder.append("-[");
//            builder.append(relation);
            if (relation.isLabeled()) {
                builder.append(":");
                builder.append(relation.label);
            }
            //TODO Add property translation
            builder.append("]-");
        }
        if (relation.directed)
            builder.append(">");
    }
}