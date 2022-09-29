package visualneo;

import visualneo.utils.backend.Graph;
import visualneo.utils.backend.QueryBuilder;
import visualneo.utils.frontend.Edge;
import visualneo.utils.frontend.Vertex;

import java.util.ArrayList;

public class QueryHandler {

    private final VisualNeoApp app;

    QueryHandler(VisualNeoApp app) {
        this.app = app;
    }

    void search(ArrayList<Vertex> vertices, ArrayList<Edge> edges) {
        Graph queryPattern = Graph.fromDrawing(vertices, edges);
        String query = new QueryBuilder().translate(queryPattern);
        System.out.println(query);


    }
}
