package visualneo.utils.backend;

import java.util.ArrayList;
import java.util.Iterator;

// Class representing a Cypher query statement
class QueryBuilder {

    private static final StringBuilder builder = new StringBuilder();
    private static final int indentSize = 2;
    private static int indentCount;

    public static String translate(Graph graph) {
        clear();

        // MATCH clause
        indent();
        builder.append("MATCH");
        Iterator<Relation> relationIter = graph.relations.iterator();
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
                if (!relationIter.hasNext())
                    break;
                builder.append(",");
            }
        }
        unindent();
        newLine();

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
        builder.append(new char[indentSize * indentCount]);
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
        builder.append("-[");
        builder.append(relation);
        if (relation.isLabeled()) {
            builder.append(":");
            builder.append(relation.label);
        }
        //TODO Add property translation
        if (relation.directed)
            builder.append("]->");
        else
            builder.append("]-");
    }
}
