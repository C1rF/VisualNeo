package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.*;
import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.Vertex;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QueryHandler {

    private final VisualNeoApp app;

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

    void exactSearch(ArrayList<Vertex> vertices, ArrayList<Edge> edges) {
        Graph queryPattern = Graph.fromDrawing(vertices, edges);
        String query = QueryBuilder.translate(queryPattern);
        System.out.println(query);

        Graph.recount();
    }

    private void initDriver(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        driver.verifyConnectivity();
    }

    private void retrieveMetadata() {
        try (Session session = driver.session(SessionConfig.builder().withDefaultAccessMode(AccessMode.READ).build())) {
            // Retrieve labels and corresponding counts
            Function<Record, String> mapper = record -> record.get(0).asString();

            Set<String> nodeLabels = session.readTransaction(tx ->
                    tx.run(Consts.LABELS_QUERY)
                            .stream()
                            .map(mapper)
                            .collect(Collectors.toCollection(TreeSet::new)));

            Set<String> relationLabels = session.readTransaction(tx ->
                    tx.run(Consts.RELATIONSHIP_TYPES_QUERY)
                            .stream()
                            .map(mapper)
                            .collect(Collectors.toCollection(TreeSet::new)));

            Map<String, Integer> nodeCountsByLabel = nodeLabels
                    .stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            label -> session.readTransaction(tx ->
                                    tx.run(Consts.nodeCountByLabelQuery(label))
                                            .single().get(0).asInt()),
                            (e1, e2) -> e2,
                            LinkedHashMap::new));

            Map<String, Integer> relationCountsByLabel = relationLabels
                    .stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            type -> session.readTransaction(tx ->
                                    tx.run(Consts.relationshipCountByTypeQuery(type))
                                            .single().get(0).asInt()),
                            (e1, e2) -> e2,
                            LinkedHashMap::new));

            // Retrieve property keys and types

            Map<String, Set<Pair<String>>> nodePropertiesByLabel = session.readTransaction(tx ->
                    tx.run(Consts.NODE_TYPE_PROPERTIES_QUERY)
                            .stream()
                            .collect(Collectors.toMap(
                                    record -> {
                                        String nodeLabel = record.get("nodeType").asString();
                                        return nodeLabel.substring(2, nodeLabel.length() - 1);
                                    },
                                    record -> {
                                        Set<Pair<String>> properties = new TreeSet<>(Comparator.comparing(Pair::head));
                                        record.get("properties").values().forEach(property -> {
                                            List<String> propertyPair = property.asList(Value::asString);
                                            properties.add(new Pair<>(propertyPair.get(0), propertyPair.get(1)));
                                        });
                                        return properties;
                                    })));

            Map<String, Set<Pair<String>>> relationPropertiesByLabel = session.readTransaction(tx ->
                    tx.run(Consts.REL_TYPE_PROPERTIES_QUERY)
                            .stream()
                            .collect(Collectors.toMap(
                                    record -> {
                                        String relationLabel = record.get("relType").asString();
                                        return relationLabel.substring(2, relationLabel.length() - 1);
                                    },
                                    record -> {
                                        Set<Pair<String>> properties = new TreeSet<>(Comparator.comparing(Pair::head));
                                        record.get("properties").values().forEach(property -> {
                                            List<String> propertyPair = property.asList(Value::asString);
                                            properties.add(new Pair<>(propertyPair.get(0), propertyPair.get(1)));
                                        });
                                        return properties;
                                    })));

            // Retrieve schema information
            Graph schemaGraph = session.readTransaction(tx -> {
                Record rec = tx.run(Consts.SCHEMA_QUERY).single();

                Map<Long, Node> schemaNodes = rec.get("nodes").asList(Value::asNode)
                        .stream()
                        .collect(Collectors.toMap(
                                org.neo4j.driver.types.Node::id,
                                node -> new Node(
                                        node.labels().iterator().next(),
                                        Collections.emptyMap())));

                Set<Relation> schemaRelations = rec.get("relationships").asList(Value::asRelationship)
                        .stream()
                        .map(relationship -> new Relation(
                                true,
                                schemaNodes.get(relationship.startNodeId()),
                                schemaNodes.get(relationship.endNodeId()),
                                relationship.type(),
                                Collections.emptyMap()))
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

    DbMetadata getMeta() {
        return meta;
    }
}
