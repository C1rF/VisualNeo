package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.utils.backend.Graph;
import hkust.edu.visualneo.utils.backend.Relation;
import javafx.geometry.Point2D;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ForceDirectedPlacement {

    private static final Random rand = new Random();

    private final Graph graph;

    private final int nodeCount;
    private final int relationCount;

    private final Long[] nodeIds;
    private final Map<Long, Integer> nodeIndices;

    private final Point2D[] positions;
    private final Point2D[] displacements;

    private final Point2D size;
    private final double area;

    private final int numIt;

    private final double optDist;  // Optimal distance
    private final double invOptDist;
    private final double sqrOptDist;

    public ForceDirectedPlacement(Graph graph, Point2D size, int numIt, double spsCoef) {
        this.graph = graph;
        this.size = size;
        this.numIt = numIt;

        nodeCount = graph.nodeCount();
        relationCount = graph.relationCount();

        nodeIds = graph.nodeIds().toArray(new Long[nodeCount]);
        nodeIndices = IntStream.range(0, nodeCount)
                               .boxed()
                               .collect(Collectors.toMap(i -> nodeIds[i], Function.identity()));

        positions = new Point2D[nodeCount];
        displacements = new Point2D[nodeCount];
        Point2D min = size.multiply(-0.5);
        Point2D max = min.add(size);
        Arrays.setAll(positions, i -> randomPoint(min, max));
        Arrays.setAll(displacements, i -> new Point2D(0.0, 0.0));

        area = size.getX() * size.getY();

        optDist = spsCoef * Math.sqrt(area / nodeCount);
        invOptDist = 1.0 / optDist;
        sqrOptDist = optDist * optDist;
    }

    public Map<Long, Point2D> layout() {
        for (int itCount = 0; itCount < numIt; ++itCount) {
            // Compute repulsive displacements
            for (int i = 0; i < nodeCount; ++i) {
                for (int j = i + 1; j < nodeCount; ++j) {
                    Point2D diff = positions[j].subtract(positions[i]);
                    Point2D disp = repDisp(diff);
                    displacements[i] = displacements[i].subtract(disp);
                    displacements[j] = displacements[j].add(disp);
                }
            }

            // Compute attractive displacements
            for (Relation relation : graph.relations()) {
                int i = nodeIndices.get(relation.start.getId());
                int j = nodeIndices.get(relation.end.getId());

                Point2D diff = positions[j].subtract(positions[i]);
                Point2D disp = attDisp(diff);
                displacements[i] = displacements[i].add(disp);
                displacements[j] = displacements[j].subtract(disp);
            }

            // Apply displacements
            for (int i = 0; i < nodeCount; ++i) {
                positions[i] = positions[i].add(displacements[i]);
            }
        }

        return Collections.emptyMap();
    }

    // Attractive and repulsive displacements between u and v
    // diff = v - u
    // The displacements have the same direction as diff
    private Point2D attDisp(Point2D diff) {
        double dist = diff.magnitude();
        return diff.multiply(invOptDist * dist);
    }
    private Point2D repDisp(Point2D diff) {
        double dist = diff.magnitude();
        return diff.multiply(sqrOptDist / (dist * dist));
    }

    private static Point2D randomPoint(Point2D min, Point2D max) {
        return new Point2D(rand.nextDouble(min.getX(), max.getX()), rand.nextDouble(min.getY(), max.getY()));
    }
}
