package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.*;
import hkust.edu.visualneo.utils.frontend.Canvas;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QueryHandler {

    private final QueryBuilder translator = new QueryBuilder();

    private Driver driver;
    private DbMetadata meta;

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
            Function<Record, String> recordToLabel = record -> record.get(0).asString();

            Set<String> nodeLabels = session.executeRead(tx -> tx
                    .run(Queries.LABELS_QUERY)
                    .stream()
                    .map(recordToLabel)
                    .collect(Collectors.toCollection(TreeSet::new)));

            Set<String> relationLabels = session.executeRead(tx -> tx
                    .run(Queries.RELATIONSHIP_TYPES_QUERY)
                    .stream()
                    .map(recordToLabel)
                    .collect(Collectors.toCollection(TreeSet::new)));

            Map<String, Integer> nodeCountsByLabel = nodeLabels
                    .stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            label -> session.executeRead(tx ->
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
                            type -> session.executeRead(tx ->
                                    tx.run(Queries.relationshipCountByTypeQuery(type))
                                      .single()
                                      .get(0)
                                      .asInt()),
                            (e1, e2) -> e2,
                            LinkedHashMap::new));

            // Retrieve property keys and types

            Map<String, Map<String, String>> nodePropertiesByLabel = session.executeRead(tx -> tx
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

            Map<String, Map<String, String>> relationPropertiesByLabel = session.executeRead(tx -> tx
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
            Graph schemaGraph = session.executeRead(tx -> {
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

    Results exactSearch(Canvas canvas) {
        Graph queryGraph = new Graph(canvas);
        String query = translator.translate(queryGraph, false);
        System.out.println(query);

        try (Session session = driver.session(SessionConfig.builder()
                                                           .withDefaultAccessMode(AccessMode.READ)
                                                           .build())) {

            return session.executeRead(tx -> {
                Record record = tx.run(query).single();

                Map<Long, Node> nodes = record
                        .get("nodes")
                        .asList(Value::asNode)
                        .stream()
                        .collect(Collectors.toMap(
                                org.neo4j.driver.types.Node::id,
                                node -> new Node(node, false)));

                Set<Relation> relations = record
                        .get("relationships")
                        .asList(Value::asRelationship)
                        .stream()
                        .map(relationship -> new Relation(relationship, nodes, false))
                        .collect(Collectors.toSet());

                Graph resultGraph = new Graph(new HashSet<>(nodes.values()), relations);

                List<Pair<List<Long>>> resultIds = new ArrayList<>(record
                        .get("resultIds")
                        .asList(value -> {
                            List<Value> pairList = value.asList(Function.identity());
                            List<Long> nodeIds = pairList.get(0).asList(Value::asLong);
                            List<Long> relationIds = pairList.get(1).asList(Value::asLong);
                            return new Pair<>(nodeIds, relationIds);
                        }));

                return new Results(resultGraph, resultIds);
            });
        }
    }

    public QueryBuilder getTranslator() {
        return translator;
    }

    DbMetadata getMeta() {
        return meta;
    }

    public record Results(Graph graph, List<Pair<List<Long>>> ids) implements Mappable {

        public Results {
            Objects.requireNonNull(graph);
            Objects.requireNonNull(ids);
        }

        @Override
        public String toString() {
            return new TreePrinter().print(getName(), toMap());
        }

        @Override
        public String getName() {
            return "Query Results";
        }

        @Override
        public Map<?, ?> toMap() {
            Map<Object, Object> map = new LinkedHashMap<>();

            map.put("Graph", graph.toMap());
            map.put("Node & Relation IDs",
                    IntStream.range(0, ids.size())
                             .boxed()
                             .collect(Collectors.toMap(
                                     Function.identity(),
                                     i -> {
                                         Pair<List<Long>> idPair = ids.get(i);
                                         Map<String, List<Long>> idMap = new LinkedHashMap<>();
                                         idMap.put("Nodes", idPair.head());
                                         idMap.put("Relations", idPair.tail());
                                         return idMap;
                                     },
                                     (e1, e2) -> e1,
                                     LinkedHashMap::new)));

            return map;
        }
    }
}
