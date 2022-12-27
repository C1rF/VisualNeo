package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.*;
import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.Vertex;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QueryHandler {

    private final VisualNeoApp app;

    private final QueryBuilder builder = new QueryBuilder();

    private Driver driver;
    private DbMetadata meta;

    QueryHandler(VisualNeoApp app) {
        this.app = app;
    }

    void loadDatabase(String uri, String user, String password) {
        initDriver(uri, user, password);
        retrieveMetadata();

        System.out.println(meta);
    }

    private void initDriver(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        driver.verifyConnectivity();
    }

    private void retrieveMetadata() {
        try (Session session = driver.session(SessionConfig.builder()
                                                           .withDefaultAccessMode(AccessMode.READ)
                                                           .build())) {
            // Retrieve labels and corresponding counts
            Function<Record, String> mapper = record -> record.get(0).asString();

            Set<String> nodeLabels = session.readTransaction(tx -> tx
                    .run(Queries.LABELS_QUERY)
                    .stream()
                    .map(mapper)
                    .collect(Collectors.toCollection(TreeSet::new)));

            Set<String> relationLabels = session.readTransaction(tx -> tx
                    .run(Queries.RELATIONSHIP_TYPES_QUERY)
                    .stream()
                    .map(mapper)
                    .collect(Collectors.toCollection(TreeSet::new)));

            Map<String, Integer> nodeCountsByLabel = nodeLabels
                    .stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            label -> session.readTransaction(tx ->
                                    tx.run(Queries.nodeCountByLabelQuery(label))
                                      .single()
                                      .get(0)
                                      .asInt()),
                            (e1, e2) -> e2,
                            LinkedHashMap::new));

            Map<String, Integer> relationCountsByLabel = relationLabels
                    .stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            type -> session.readTransaction(tx ->
                                    tx.run(Queries.relationshipCountByTypeQuery(type))
                                      .single()
                                      .get(0)
                                      .asInt()),
                            (e1, e2) -> e2,
                            LinkedHashMap::new));

            // Retrieve property keys and types

            Map<String, Map<String, String>> nodePropertiesByLabel = session.readTransaction(tx -> tx
                    .run(Queries.NODE_TYPE_PROPERTIES_QUERY)
                    .stream()
                    .collect(Collectors.toMap(
                            record -> {
                                String nodeLabel = record.get("nodeType").asString();
                                return nodeLabel.substring(2, nodeLabel.length() - 1);
                            },
                            record -> {
                                Map<String, String> properties = new TreeMap<>();
                                record.get("properties").values().forEach(property -> {
                                    List<String> propertyPair = property.asList(Value::asString);
                                    properties.putIfAbsent(propertyPair.get(0), propertyPair.get(1));
                                });
                                return properties;
                            })));

            Map<String, Map<String, String>> relationPropertiesByLabel = session.readTransaction(tx -> tx
                    .run(Queries.REL_TYPE_PROPERTIES_QUERY)
                    .stream()
                    .collect(Collectors.toMap(
                            record -> {
                                String relationLabel = record.get("relType").asString();
                                return relationLabel.substring(2, relationLabel.length() - 1);
                            },
                            record -> {
                                Map<String, String> properties = new TreeMap<>();
                                record.get("properties").values().forEach(property -> {
                                    List<String> propertyPair = property.asList(Value::asString);
                                    properties.putIfAbsent(propertyPair.get(0), propertyPair.get(1));
                                });
                                return properties;
                            })));

            // Retrieve schema information
            Graph schemaGraph = session.readTransaction(tx -> {
                Record record = tx.run(Queries.SCHEMA_QUERY).single();

                Map<Long, Node> schemaNodes = record
                        .get("nodes")
                        .asList(Value::asNode)
                        .stream()
                        .collect(Collectors.toMap(
                                org.neo4j.driver.types.Node::id,
                                node -> new Node(node, true)));

                Set<Relation> schemaRelations = record
                        .get("relationships")
                        .asList(Value::asRelationship)
                        .stream()
                        .map(relationship -> new Relation(relationship, schemaNodes, true))
                        .collect(Collectors.toSet());

                return new Graph(new HashSet<>(schemaNodes.values()), schemaRelations);
            });

            meta = new DbMetadata(
                    nodeCountsByLabel,
                    relationCountsByLabel,
                    nodePropertiesByLabel,
                    relationPropertiesByLabel,
                    schemaGraph);
        }
    }

    void exactSearch(List<Vertex> vertices, List<Edge> edges) {
        Graph queryPattern = Graph.fromDrawing(vertices, edges);
        String query = builder.translate(queryPattern);
        System.out.println(query);
    }

    DbMetadata getMeta() {
        return meta;
    }
}
