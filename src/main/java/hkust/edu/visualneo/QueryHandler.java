package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.DbMetadata;
import hkust.edu.visualneo.utils.backend.Graph;
import hkust.edu.visualneo.utils.backend.QueryBuilder;
import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.Vertex;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;

public class QueryHandler {

    private final VisualNeoApp app;

    private Driver driver;
    private DbMetadata meta;

    QueryHandler(VisualNeoApp app) {
        this.app = app;
    }

    private void initDriver(String uri, String user, String password) {
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        driver.verifyConnectivity();
        this.driver = driver;
    }

    private void retrieveMetadata() {
        try (Session session = driver.session(SessionConfig.builder().withDefaultAccessMode(AccessMode.READ).build())) {
            meta = session.readTransaction(tx -> {
                Record metaRecord = tx.run(QueryBuilder.metadataQuery).single();

                ArrayList<String> nodeLabels = new ArrayList<>();
                metaRecord.get("labels").asList().forEach(label -> nodeLabels.add((String) label));
                ArrayList<String> relationLabels = new ArrayList<>();
                metaRecord.get("relationshipTypes").asList().forEach(label -> relationLabels.add((String) label));
                ArrayList<String> propertyKeys = new ArrayList<>();
                metaRecord.get("propertyKeys").asList().forEach(label -> propertyKeys.add((String) label));

                return new DbMetadata(nodeLabels, relationLabels, propertyKeys);
            });
        }
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

    DbMetadata getMeta() {
        return meta;
    }
}
