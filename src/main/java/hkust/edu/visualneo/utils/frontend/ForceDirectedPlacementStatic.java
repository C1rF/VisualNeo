package hkust.edu.visualneo.utils.frontend;

import hkust.edu.visualneo.utils.backend.Pair;
import javafx.geometry.Point2D;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ForceDirectedPlacementStatic {

    private static final Random rand = new Random();

    private static final double OPT_DIST = 200.0;  // Optimal distance
    private static final double INV_OPT_DIST = 1.0 / OPT_DIST;
    private static final double SQR_OPT_DIST = OPT_DIST * OPT_DIST;

    private static final double GRAV_COEF = 0.05;  // Gravity coefficient
    private static final double MAX_REP_DIST_COEF = 0.8;

    private static final double INIT_TEMP = 20.0;  // Initial temperature

    private static final double STOP_AVG_DISP = 80.0;  // Average displacement threshold for stopping simulation
    private static final int MAX_ITERATIONS = 10000;  // Max number of iterations of simulation;
    private static final double MAX_TIME = 20.0;

    private final Canvas canvas;

    private final int vertexCount;
    private final int edgeCount;

    private final Long[] ids;
    private final Map<Long, Integer> indices;

    private final Collection<Pair<Integer>> pairs;

    private final Point2D[] positions;
    private final Point2D[] displacements;

    private final double maxRepDist;  // Maximum distance for applying repulsive force

    private int itCount = 0;

    private double temp;  // Current temperature

    public ForceDirectedPlacementStatic(Canvas canvas) {
        this.canvas = canvas;
        vertexCount = canvas.getVertices().size();

        ids = canvas.getVertices().stream().map(Vertex::getElementId).toArray(Long[]::new);
        indices = IntStream.range(0, vertexCount)
                           .boxed()
                           .collect(Collectors.toMap(i -> ids[i], Function.identity()));

        // Remove self-loops and only count
        pairs = canvas.getEdges()
                      .stream()
                      .filter(edge -> !edge.isSelfLoop())
                      .map(edge -> new Pair<>(indices.get(edge.startVertex.getElementId()), indices.get(edge.endVertex.getElementId())))
                      .collect(Collectors.toSet());
        edgeCount = pairs.size();

        double scale = Math.sqrt(vertexCount);
        double fullness = Math.sqrt(edgeCount * 2.0 / (vertexCount * (vertexCount - 1)));

        maxRepDist = MAX_REP_DIST_COEF * OPT_DIST * scale * fullness;

        double max = OPT_DIST * scale * 0.5;
        double min = -max;

        positions = new Point2D[vertexCount];
        displacements = new Point2D[vertexCount];
        Arrays.setAll(positions, i -> randomPoint(min, max));
    }

    public void layout() {
        Point2D sum = Point2D.ZERO;
        for (Point2D pos : positions)
            sum = sum.add(pos);
        Point2D centroid = sum.multiply(1.0 / vertexCount);

        for (int i = 0; i < vertexCount; ++i)
            canvas.getVertex(ids[i]).setPosition(positions[i].subtract(centroid));
    }

    public Map<Long, Point2D> getPositionMap() {
        return IntStream.range(0, vertexCount).boxed().collect(Collectors.toMap(i -> ids[i], i -> positions[i]));
    }

    public void simulate(int iterations) {
        long startTime = System.currentTimeMillis();
        if (iterations == 0) {
            int equilibriumCount = 0;
            while (equilibriumCount < 5 && itCount < MAX_ITERATIONS &&
                   (System.currentTimeMillis() - startTime) / 1000.0 < MAX_TIME) {
                simulate();
                if (itCount % 100 == 0 && computeAvgDisp() < STOP_AVG_DISP)
                    ++equilibriumCount;
            }
        }
        else {
            for (int i = 0;
                 i < iterations && itCount < MAX_ITERATIONS &&
                 (System.currentTimeMillis() - startTime) / 1000.0 < MAX_TIME;
                 ++i)
                simulate();
        }

//        System.out.println(time);
//        System.out.println(computeAvgDisp());
    }

    public void simulate() {
        // Clear all displacements
        Arrays.fill(displacements, Point2D.ZERO);

        // Compute repulsive displacements
        for (int i = 0; i < vertexCount; ++i) {
            for (int j = i + 1; j < vertexCount; ++j) {
                Point2D diff = positions[j].subtract(positions[i]);
                Point2D disp = repDisp(diff);
                displacements[i] = displacements[i].subtract(disp);
                displacements[j] = displacements[j].add(disp);
            }
        }

        // Compute attractive displacements
        for (Pair<Integer> pair : pairs) {
            int i = pair.head();
            int j = pair.tail();

            Point2D diff = positions[j].subtract(positions[i]);
            Point2D disp = attDisp(diff);
            displacements[i] = displacements[i].add(disp);
            displacements[j] = displacements[j].subtract(disp);
        }

        // Apply displacements
        for (int i = 0; i < vertexCount; ++i) {
            Point2D disp = displacements[i];
            positions[i] = positions[i].add(disp.multiply(Math.min(1.0, temp / disp.magnitude())));
        }

        // Cooling
        cool();

        // Increment iteration count
        ++itCount;
    }

    // Attractive and repulsive displacements between u and v
    // diff = v - u
    // The displacements have the same direction as diff
    private Point2D attDisp(Point2D diff) {
        double dist = diff.magnitude();
        return diff.multiply(INV_OPT_DIST * dist);
    }
    private Point2D repDisp(Point2D diff) {
        double dist = diff.magnitude();
        if (dist > maxRepDist)
            return Point2D.ZERO;
        return diff.multiply(SQR_OPT_DIST / (dist * dist));
    }

    private Point2D gravity(Point2D pos) {
        return Point2D.ZERO.subtract(pos).multiply(GRAV_COEF);
    }

    private void cool() {
        temp = INIT_TEMP / (1 + Math.log(1 + itCount));
    }

    private double computeAvgDisp() {
        double sum = 0.0;
        for (Point2D disp : displacements) {
            sum += disp.magnitude();
        }
        return sum / vertexCount;
    }

    private static Point2D randomPoint(double min, double max) {
        return new Point2D(rand.nextDouble(min, max), rand.nextDouble(min, max));
    }
}
