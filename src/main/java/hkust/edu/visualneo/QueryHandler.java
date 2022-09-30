package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.Graph;
import hkust.edu.visualneo.utils.backend.QueryBuilder;
import hkust.edu.visualneo.utils.frontend.Edge;
import hkust.edu.visualneo.utils.frontend.Vertex;

import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.AuthTokens;

import java.util.ArrayList;

public class QueryHandler {

    private final VisualNeoApp app;

    QueryHandler(VisualNeoApp app) {
        this.app = app;
    }

    void loadDatabase(String uri, String user, String password) {
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    void exactSearch(ArrayList<Vertex> vertices, ArrayList<Edge> edges) {
        Graph queryPattern = Graph.fromDrawing(vertices, edges);
        String query = new QueryBuilder().translate(queryPattern);
        System.out.println(query);

        Graph.recount();
    }
}
