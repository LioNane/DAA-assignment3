import dag.DagShortestPath;
import dag.Kahn;
import graph.Graph;
import metrics.Metrics;
import mst.Kruskal;
import mst.Prim;
import scc.Kosaraju;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

private static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
private static final int RUNS = 5;
private static final String CSV_FILE = "results.csv";
private static Random rnd = new Random(42);

void main() {
    try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
        writer.println("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");

        for (int n : SIZES) {
            runBenchmark(n, 4 * n, "sparse", writer);
            runBenchmark(n, 20 * n, "dense", writer);
        }

        System.out.println("Benchmark successfully completed. Data saved in " + CSV_FILE);
    } catch (IOException e) {
        System.err.println("File writing error: " + e.getMessage());
    }
}

private static void runBenchmark(int n, int m, String density, PrintWriter writer) {
    Graph undirectedGraph = generateGraph(n, m, "undirected");
    Graph directedGraph = generateGraph(n, m, "directed");
    Graph dag = generateGraph(n, m, "dag");

    long kruskalWeight = runTest("Kruskal", density, n, m, undirectedGraph, writer, (g, metrics) -> Kruskal.findMST(g, metrics).totalWeight);
    long primWeight = runTest("Prim", density, n, m, undirectedGraph, writer, (g, metrics) -> Prim.findMST(g, metrics).totalWeight);

    if (kruskalWeight != primWeight) {
        throw new IllegalStateException("Error! Different MST weights. Kruskal: " + kruskalWeight + ", Prim: " + primWeight);
    }

    runTest("Kosaraju", density, n, m, directedGraph, writer, (g, metrics) -> (long) Kosaraju.findSCC(g, metrics).componentsCount);

    runTest("Kahn", density, n, m, dag, writer, (g, metrics) -> {
        Kahn.Result res = Kahn.topologicalSort(g, metrics);
        if (res.hasCycle) {
            throw new IllegalStateException("Error! Kahn algorithm found cycle in DAG.");
        }

        validateTopologicalOrder(g, res.order);

        return (long) res.order.size();
    });

    runTest("DagShortestPath", density, n, m, dag, writer, (g, metrics) -> DagShortestPath.findShortestPaths(g, metrics).totalDistancesSum);
}

private static void validateTopologicalOrder(Graph graph, List<Integer> order) {
    int n = graph.getV();
    if (order.size() != n) {
        throw new IllegalStateException("Order does not contain all vertices!");
    }

    int[] pos = new int[n];
    for (int i = 0; i < n; i++) {
        pos[order.get(i)] = i;
    }

    for (int u = 0; u < n; u++) {
        List<Graph.Edge> edges = graph.getAdj(u);
        if (edges != null) {
            for (Graph.Edge edge : edges) {
                if (pos[u] >= pos[edge.to]) {
                    throw new IllegalStateException("Topological order is not correct: edge " + u + "->" + edge.to);
                }
            }
        }
    }
}

private interface AlgorithmRunner {
    long run(Graph graph, Metrics metrics);
}

private static long runTest(String algorithmName, String density, int n, int m, Graph graph, PrintWriter writer, AlgorithmRunner runner) {
    double[] times = new double[RUNS];
    Metrics metrics = new Metrics();
    long result = 0;

    for (int i = 0; i < RUNS; i++) {
        metrics.reset();
        metrics.startTimer();
        result = runner.run(graph, metrics);
        metrics.stopTimer();
        times[i] = metrics.getTimeMs();
    }

    double medianTime = calculateMedian(times);
    writer.printf("%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d%n",
            algorithmName, density, n, m, medianTime,
            metrics.getDsuOps(), metrics.getEdgeScans(), metrics.getComparisons(), metrics.getMaxDepth(), result);
    return result;
}

private static double calculateMedian(double[] times) {
    Arrays.sort(times);
    return times[times.length / 2];
}

private static Graph generateGraph(int n, int m, String type) {
    rnd = new Random(42);
    Graph graph = new Graph(n, !type.equals("undirected"));

    if (type.equals("undirected")) {
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int temp = perm[i];
            perm[i] = perm[j];
            perm[j] = temp;
        }
        for (int i = 1; i < n; i++) {
            int u = perm[rnd.nextInt(i)];
            int v = perm[i];
            graph.addEdge(u, v, 1 + rnd.nextInt(1000));
        }
    }

    int edgesToAdd = type.equals("undirected") ? m - (n - 1) : m;
    int[] rank = new int[n];
    if (type.equals("dag")) {
        for (int i = 0; i < n; i++) rank[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int temp = rank[i];
            rank[i] = rank[j];
            rank[j] = temp;
        }
    }

    for (int i = 0; i < edgesToAdd; i++) {
        int u = rnd.nextInt(n);
        int v = rnd.nextInt(n);
        while (u == v) v = rnd.nextInt(n);

        if (type.equals("dag") && rank[u] > rank[v]) {
            int temp = u;
            u = v;
            v = temp;
        }
        graph.addEdge(u, v, 1 + rnd.nextInt(1000));
    }
    return graph;
}
