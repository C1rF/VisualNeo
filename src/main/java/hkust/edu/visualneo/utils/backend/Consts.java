package hkust.edu.visualneo.utils.backend;

public class Consts {

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

    public static String nodeCountByLabelQuery(String label) {
        return String.format(NODE_COUNT_BY_LABEL_QUERY, label);
    }
    public static String relationshipCountByTypeQuery(String type) {
        return String.format(RELATIONSHIP_COUNT_BY_TYPE_QUERY, type);
    }
}
