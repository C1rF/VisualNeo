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

public class ForceDirectedPlacementDynamic {

    private static final Random rand = new Random();

    private static final double OPT_DIST = 150.0;  // Optimal distance
    private static final double INV_OPT_DIST = 1.0 / OPT_DIST;
    private static final double SQR_OPT_DIST = OPT_DIST * OPT_DIST;

    private static final double MAX_REP_DIST = 1.5 * OPT_DIST;  // Maximum distance for applying repulsive force

    private static final double GRAV_COEF = 0.05;  // Gravity coefficient

    private static final double INIT_TEMP = 100.0;  // Initial temperature

    private final Canvas canvas;

    private final int vertexCount;

    private final Long[] ids;
    private final Map<Long, Integer> indices;

    private final Collection<Pair<Integer>> pairs;

    private final Point2D[] positions;
    private final Point2D[] velocities;
    private final Point2D[] forces;

    private double time = 0;

    private double temp;  // Current temperature

    public ForceDirectedPlacementDynamic(Canvas canvas) {
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

//        double max = OPT_DIST * Math.min(Math.sqrt(vertexCount), 10.0) * 0.5;
        double max = OPT_DIST * Math.sqrt(vertexCount) * 0.5;
        double min = -max;

        positions = new Point2D[vertexCount];
        velocities = new Point2D[vertexCount];
        forces = new Point2D[vertexCount];
        Arrays.setAll(positions, i -> randomPoint(min, max));
        Arrays.fill(velocities, Point2D.ZERO);
        Arrays.fill(forces, Point2D.ZERO);
    }

    public void layout() {
        for (int i = 0; i < vertexCount; ++i)
            canvas.getVertex(ids[i]).setPosition(positions[i]);
    }

    public Map<Long, Point2D> getPositionMap() {
        return IntStream.range(0, vertexCount).boxed().collect(Collectors.toMap(i -> ids[i], i -> positions[i]));
    }

    public void simulate(double duration) {

    }

    public void simulate() {
        // Compute repulsive displacements
        for (int i = 0; i < vertexCount; ++i) {
            for (int j = i + 1; j < vertexCount; ++j) {
                Point2D diff = positions[j].subtract(positions[i]);
                Point2D disp = repDisp(diff);
                velocities[i] = velocities[i].subtract(disp);
                velocities[j] = velocities[j].add(disp);
            }
        }

        // Compute attractive displacements
        for (Pair<Integer> pair : pairs) {
            int i = pair.head();
            int j = pair.tail();

            Point2D diff = positions[j].subtract(positions[i]);
            Point2D disp = attDisp(diff);
            velocities[i] = velocities[i].add(disp);
            velocities[j] = velocities[j].subtract(disp);
        }

        // Compute gravities
        for (int i = 0; i < vertexCount; ++i)
            velocities[i] = velocities[i].add(gravity(positions[i]));

        // Apply displacements
        for (int i = 0; i < vertexCount; ++i) {
            Point2D disp = velocities[i];
            positions[i] = positions[i].add(disp.multiply(Math.min(1.0, temp / disp.magnitude())));
        }

        // Cooling
        cool();

        // Clear all displacements
        Arrays.fill(velocities, Point2D.ZERO);

        // Increment iteration count
        ++time;
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
        if (dist > MAX_REP_DIST)
            return Point2D.ZERO;
        return diff.multiply(SQR_OPT_DIST / (dist * dist));
    }

    private Point2D gravity(Point2D pos) {
        return Point2D.ZERO.subtract(pos).multiply(GRAV_COEF);
    }

    private void cool() {
        temp = INIT_TEMP / (1 + Math.log(1 + time));
    }

    private static Point2D randomPoint(double min, double max) {
        return new Point2D(rand.nextDouble(min, max), rand.nextDouble(min, max));
    }
}
