import dag.DagShortestPath;
import dag.Kahn;
import graph.DSU;
import graph.Graph;
import graph.GraphReader;
import metrics.Metrics;
import mst.Kruskal;
import mst.Prim;
import mst.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import scc.Kosaraju;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GraphAlgorithmsTest {

    private Metrics metrics;
    private Random rnd;

    @BeforeEach
    void setUp() {
        metrics = new Metrics();
        rnd = new Random(42);
    }

    @Test
    @DisplayName("DSU: Agreement with reference implementation and components() count")
    void testDSU() {
        int n = 200;
        DSU dsu = new DSU(n);

        int[] refComp = new int[n];
        for (int i = 0; i < n; i++) {
            refComp[i] = i;
        }
        int expectedComponents = n;

        for (int step = 0; step < 300; step++) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);

            dsu.union(u, v, metrics);

            if (refComp[u] != refComp[v]) {
                int oldComp = refComp[v];
                int newComp = refComp[u];
                for (int i = 0; i < n; i++) {
                    if (refComp[i] == oldComp) {
                        refComp[i] = newComp;
                    }
                }
                expectedComponents--;
            }

            assertEquals(expectedComponents, dsu.components(),
                    "Mismatch in component count at step " + step);
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                boolean sameInDsu = (dsu.find(i, metrics) == dsu.find(j, metrics));
                boolean sameInRef = (refComp[i] == refComp[j]);
                assertEquals(sameInRef, sameInDsu,
                        "Mismatch with reference implementation for pair (" + i + ", " + j + ")");
            }
        }
    }

    @Test
    @DisplayName("MST: Kruskal and Prim return same total weight and n - 1 edges on 100 random connected graphs")
    void testMSTOn100RandomGraphs() {
        for (int iter = 0; iter < 100; iter++) {
            int n = 20 + rnd.nextInt(80);
            int extraEdges = rnd.nextInt(n * 3);
            Graph g = new Graph(n, false);

            for (int i = 1; i < n; i++) {
                int parent = rnd.nextInt(i);
                int weight = 1 + rnd.nextInt(500);
                g.addEdge(parent, i, weight);
            }

            for (int e = 0; e < extraEdges; e++) {
                int u = rnd.nextInt(n);
                int v = rnd.nextInt(n);
                int weight = 1 + rnd.nextInt(500);
                g.addEdge(u, v, weight);
            }

            Metrics mKruskal = new Metrics();
            Metrics mPrim = new Metrics();

            Result kruskalRes = Kruskal.findMST(g, mKruskal);
            Result primRes = Prim.findMST(g, mPrim);

            assertEquals(kruskalRes.totalWeight, primRes.totalWeight,
                    "Kruskal and Prim total weights do not match on graph #" + iter);

            assertEquals(1, kruskalRes.components);
            assertEquals(1, primRes.components);
            int mstEdgesCount = n - kruskalRes.components;
            assertEquals(n - 1, mstEdgesCount, "MST must contain exactly n - 1 edges");
        }
    }

    @Test
    @DisplayName("MST: Disconnected graph returns minimum spanning forest without exceptions")
    void testMSTOnDisconnectedGraph() {
        Graph g = new Graph(6, false);
        g.addEdge(0, 1, 10);
        g.addEdge(1, 2, 20);
        g.addEdge(0, 2, 50);
        g.addEdge(3, 4, 15);

        Result kruskalRes = Kruskal.findMST(g, new Metrics());
        Result primRes = Prim.findMST(g, new Metrics());

        assertEquals(45L, kruskalRes.totalWeight);
        assertEquals(45L, primRes.totalWeight);
        assertEquals(3, kruskalRes.components);
        assertEquals(3, primRes.components);
    }

    @Test
    @DisplayName("SCC: Compare Kosaraju with brute force (BFS in both directions) on graphs with n <= 200")
    void testSCCAgainstBruteForce() {
        for (int iter = 0; iter < 30; iter++) {
            int n = 10 + rnd.nextInt(191);
            int m = rnd.nextInt(n * 3);

            Graph g = new Graph(n, true);
            Graph rev = new Graph(n, true);

            for (int e = 0; e < m; e++) {
                int u = rnd.nextInt(n);
                int v = rnd.nextInt(n);
                g.addEdge(u, v, 1);
                rev.addEdge(v, u, 1);
            }

            int kosarajuCount = Kosaraju.findSCC(g, new Metrics()).componentsCount;
            int bruteForceCount = countSCCBruteForce(g, rev, n);

            assertEquals(bruteForceCount, kosarajuCount,
                    "SCC count does not match brute force at iteration " + iter);
        }
    }

    private int countSCCBruteForce(Graph g, Graph rev, int n) {
        boolean[][] reachForward = new boolean[n][n];
        boolean[][] reachBackward = new boolean[n][n];

        for (int start = 0; start < n; start++) {
            bfsReach(g, start, reachForward[start]);
            bfsReach(rev, start, reachBackward[start]);
        }

        boolean[] assigned = new boolean[n];
        int components = 0;

        for (int u = 0; u < n; u++) {
            if (!assigned[u]) {
                components++;
                for (int v = 0; v < n; v++) {
                    if (reachForward[u][v] && reachBackward[u][v]) {
                        assigned[v] = true;
                    }
                }
            }
        }
        return components;
    }

    private void bfsReach(Graph g, int start, boolean[] visited) {
        Queue<Integer> q = new ArrayDeque<>();
        q.add(start);
        visited[start] = true;
        while (!q.isEmpty()) {
            int u = q.poll();
            for (Graph.Edge edge : g.getAdj(u)) {
                if (!visited[edge.to]) {
                    visited[edge.to] = true;
                    q.add(edge.to);
                }
            }
        }
    }

    @Test
    @DisplayName("Topological Order: Valid order on DAG and cycle detection on cyclic graph")
    void testTopologicalOrderAndCycleDetection() {
        for (int iter = 0; iter < 50; iter++) {
            int n = 20 + rnd.nextInt(80);
            int m = n * 2;
            Graph dag = generateRandomDAG(n, m, false);

            Kahn.Result res = Kahn.topologicalSort(dag, new Metrics());
            assertFalse(res.hasCycle, "DAG should not have a cycle");
            assertNotNull(res.order);
            assertEquals(n, res.order.size());

            int[] pos = new int[n];
            for (int i = 0; i < n; i++) {
                pos[res.order.get(i)] = i;
            }
            for (int u = 0; u < n; u++) {
                for (Graph.Edge edge : dag.getAdj(u)) {
                    assertTrue(pos[u] < pos[edge.to],
                            "Invalid topological order for edge " + u + " -> " + edge.to);
                }
            }
        }

        Graph cyclic = new Graph(4, true);
        cyclic.addEdge(0, 1, 1);
        cyclic.addEdge(1, 2, 1);
        cyclic.addEdge(2, 0, 1);
        cyclic.addEdge(2, 3, 1);

        Kahn.Result cyclicRes = Kahn.topologicalSort(cyclic, new Metrics());
        assertTrue(cyclicRes.hasCycle, "Kahn's algorithm must detect a cycle");
        assertNull(cyclicRes.order);
    }

    @Test
    @DisplayName("DAG Shortest Paths: Compare with Bellman-Ford on random DAGs including negative weights")
    void testDagShortestPathsAgainstBellmanFord() {
        for (int iter = 0; iter < 50; iter++) {
            int n = 15 + rnd.nextInt(45);
            int m = n * 3;
            Graph dag = generateRandomDAG(n, m, true);

            DagShortestPath.Result dagRes = DagShortestPath.findShortestPaths(dag, new Metrics());
            long[] bfDist = bellmanFordSuperSource(dag);

            assertArrayEquals(bfDist, dagRes.dist,
                    "DAG shortest path distances do not match Bellman-Ford at iteration " + iter);

            for (int v = 0; v < n; v++) {
                if (dagRes.parent[v] != -1) {
                    int p = dagRes.parent[v];
                    boolean foundMatchingEdge = false;
                    for (Graph.Edge e : dag.getAdj(p)) {
                        if (e.to == v && dagRes.dist[p] + e.weight == dagRes.dist[v]) {
                            foundMatchingEdge = true;
                            break;
                        }
                    }
                    assertTrue(foundMatchingEdge, "Invalid parent in parent[] for vertex " + v);
                }
            }
        }
    }

    private long[] bellmanFordSuperSource(Graph g) {
        int n = g.getV();
        int[] inDegree = new int[n];
        for (int u = 0; u < n; u++) {
            for (Graph.Edge e : g.getAdj(u)) {
                inDegree[e.to]++;
            }
        }

        long[] dist = new long[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                dist[i] = 0;
            }
        }

        for (int step = 0; step < n - 1; step++) {
            boolean changed = false;
            for (int u = 0; u < n; u++) {
                if (dist[u] != Long.MAX_VALUE) {
                    for (Graph.Edge e : g.getAdj(u)) {
                        if (dist[u] + e.weight < dist[e.to]) {
                            dist[e.to] = dist[u] + e.weight;
                            changed = true;
                        }
                    }
                }
            }
            if (!changed) break;
        }
        return dist;
    }

    private Graph generateRandomDAG(int n, int m, boolean allowNegativeWeights) {
        Graph dag = new Graph(n, true);
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = rank[i];
            rank[i] = rank[j];
            rank[j] = tmp;
        }

        Set<Long> usedPairs = new HashSet<>();
        for (int e = 0; e < m; e++) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);
            while (u == v) {
                v = rnd.nextInt(n);
            }
            if (rank[u] > rank[v]) {
                int tmp = u;
                u = v;
                v = tmp;
            }
            long key = ((long) u << 32) | (v & 0xFFFFFFFFL);
            if (usedPairs.add(key)) {
                int weight = allowNegativeWeights ? (rnd.nextInt(201) - 100) : (1 + rnd.nextInt(100));
                dag.addEdge(u, v, weight);
            }
        }
        return dag;
    }

    @Test
    @DisplayName("Stack Safety: SCC on a path of 100,000 vertices finishes without StackOverflowError")
    void testStackSafetyOn100kPath() {
        int n = 100_000;
        Graph pathGraph = new Graph(n, true);
        for (int i = 0; i < n - 1; i++) {
            pathGraph.addEdge(i, i + 1, 1);
        }

        Metrics stackMetrics = new Metrics();
        Kosaraju.Result res = assertDoesNotThrow(
                () -> Kosaraju.findSCC(pathGraph, stackMetrics),
                "Iterative DFS must not throw StackOverflowError on 100,000 vertices"
        );

        assertEquals(n, res.componentsCount);
        assertEquals(n, stackMetrics.getMaxDepth());
    }

    @Test
    @DisplayName("Data Files: Load and test graphs from data/ directory")
    void testDataFolderGraphs() throws Exception {
        Graph disconnected = GraphReader.loadFromFile("data/disconnected.txt");
        Result mstRes = Kruskal.findMST(disconnected, new Metrics());
        assertEquals(2, mstRes.components);
        assertEquals(45L, mstRes.totalWeight);

        Graph cyclic = GraphReader.loadFromFile("data/cyclic.txt");
        Kahn.Result kahnCyclic = Kahn.topologicalSort(cyclic, new Metrics());
        assertTrue(kahnCyclic.hasCycle);
        assertEquals(2, Kosaraju.findSCC(cyclic, new Metrics()).componentsCount);

        Graph dagSmall = GraphReader.loadFromFile("data/dag_small.txt");
        DagShortestPath.Result spRes = DagShortestPath.findShortestPaths(dagSmall, new Metrics());
        assertEquals(0L, spRes.dist[0]);
        assertEquals(7L, spRes.dist[1]);
        assertEquals(8L, spRes.dist[3]);
    }
}