package visualneo.utils.backend;

// Class representing a Cypher query statement
class QueryBuilder {

    private static final StringBuilder builder = new StringBuilder();
    private static final int tabSize = 2;
    private static int indentCount;

    public static String translate(Graph graph) {
        builder.setLength(0);
        indentCount = 0;



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
        builder.append(new char[tabSize * indentCount]);
    }
}
