package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.Consts;
import hkust.edu.visualneo.utils.backend.DbMetadata;
import hkust.edu.visualneo.utils.backend.Graph;
import hkust.edu.visualneo.utils.backend.QueryBuilder;
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
            int nodeCount = session.readTransaction(tx ->
                    tx.run(Consts.NODE_COUNT_QUERY)
                            .single().get(0).asInt());

            int relationCount = session.readTransaction(tx ->
                    tx.run(Consts.RELATIONSHIP_COUNT_QUERY)
                            .single().get(0).asInt());

            Function<Record, String> mapper = record -> record.get(0).asString();

            Set<String> nodeLabels = session.readTransaction(tx ->
                    tx.run(Consts.LABELS_QUERY)
                            .stream().map(mapper).collect(Collectors.toCollection(TreeSet::new)));

            Set<String> relationLabels = session.readTransaction(tx ->
                    tx.run(Consts.RELATIONSHIP_TYPES_QUERY)
                            .stream().map(mapper).collect(Collectors.toCollection(TreeSet::new)));

            Set<String> propertyKeys = session.readTransaction(tx ->
                    tx.run(Consts.PROPERTY_KEYS_QUERY)
                            .stream().map(mapper).collect(Collectors.toCollection(TreeSet::new)));

            Map<String, Integer> nodeCountByLabel = nodeLabels.stream().collect(Collectors.toMap(
                    Function.identity(),
                    label -> session.readTransaction(tx ->
                            tx.run(Consts.nodeCountByLabelQuery(label))
                                    .single().get(0).asInt()),
                    (e1, e2) -> e2,
                    LinkedHashMap::new));

            Map<String, Integer> relationCountByLabel = relationLabels.stream().collect(Collectors.toMap(
                    Function.identity(),
                    type -> session.readTransaction(tx ->
                            tx.run(Consts.relationshipCountByTypeQuery(type))
                                    .single().get(0).asInt()),
                    (e1, e2) -> e2,
                    LinkedHashMap::new));

            meta = new DbMetadata(
                    nodeCount,
                    relationCount,
                    nodeCountByLabel,
                    relationCountByLabel,
                    propertyKeys);
        }
    }

    DbMetadata getMeta() {
        return meta;
    }
}
