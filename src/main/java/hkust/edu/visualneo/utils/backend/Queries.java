package hkust.edu.visualneo.utils.backend;

import java.util.Arrays;

public class Queries {

    public static final String NODE_COUNT_QUERY = """
            MATCH
              ()
            RETURN
              count(*) AS nodeCount""";
    public static final String RELATIONSHIP_COUNT_QUERY = """
            MATCH
              ()-->()
            RETURN
              count(*) AS relationshipCount""";
    public static final String NODE_COUNT_BY_LABEL_QUERY = """
            MATCH
              (:%s)
            RETURN
              count(*) AS nodeCountByLabel""";
    public static final String RELATIONSHIP_COUNT_BY_TYPE_QUERY = """
            MATCH
              ()-[:%s]->()
            RETURN
              count(*) AS relationshipCountByType""";
    public static final String LABELS_QUERY = """
            CALL
              db.labels()""";
    public static final String RELATIONSHIP_TYPES_QUERY = """
            CALL
              db.relationshipTypes()""";
    public static final String PROPERTY_KEYS_QUERY = """
            CALL
              db.propertyKeys()""";
    public static final String SCHEMA_QUERY = """
            CALL
              db.schema.visualization""";
    public static final String NODE_TYPE_PROPERTIES_QUERY = """
            CALL
              db.schema.nodeTypeProperties()
            YIELD
              nodeType, propertyName, propertyTypes, mandatory
            WHERE
              propertyName IS NOT NULL
            WITH
              nodeType, collect([propertyName, propertyTypes[0]]) AS properties
            RETURN
              nodeType, properties""";
    public static final String REL_TYPE_PROPERTIES_QUERY = """
            CALL
              db.schema.relTypeProperties()
            YIELD
              relType, propertyName, propertyTypes, mandatory
            WHERE
              propertyName IS NOT NULL
            WITH
              relType, collect([propertyName, propertyTypes[0]]) AS properties
            RETURN
              relType, properties""";

    public static final String SINGLETON_QUERY = """
            MATCH
              (n%s)
            RETURN
              [n] AS nodes,
              [] AS relationships,
              [[[ID(n)], []]] AS resultIds""";

    public static final String SIMPLE_SINGLETON_QUERY = """
            MATCH (n%s)
            RETURN n""";

    public static char[] separator(int length) {
        char[] sep = new char[length];
        Arrays.fill(sep, '-');
        return sep;
    }

    public static String nodeCountByLabelQuery(String label) {
        return String.format(NODE_COUNT_BY_LABEL_QUERY, label);
    }
    public static String relationshipCountByTypeQuery(String type) {
        return String.format(RELATIONSHIP_COUNT_BY_TYPE_QUERY, type);
    }

    public static String singletonQuery(String translation, boolean simple) {
        return String.format(simple ? SIMPLE_SINGLETON_QUERY : SINGLETON_QUERY, translation);
    }
}
