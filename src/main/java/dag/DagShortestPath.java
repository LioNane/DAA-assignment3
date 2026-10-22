package dag;

import graph.Graph;
import metrics.Metrics;

import java.util.Arrays;
import java.util.List;

public class DagShortestPath {

    public static class Result {
        public long totalDistancesSum;
        public long[] dist;
        public int[] parent;

        public Result(long totalDistancesSum, long[] dist, int[] parent) {
            this.totalDistancesSum = totalDistancesSum;
            this.dist = dist;
            this.parent = parent;
        }
    }

    public static Result findShortestPaths(Graph graph, Metrics metrics) {
        int n = graph.getV();

        Kahn.Result kahnResult = Kahn.topologicalSort(graph, metrics);
        if (kahnResult.hasCycle) {
            throw new IllegalStateException("Graph has a cycle. DAG Shortest Path is not applicable.");
        }
        List<Integer> topOrder = kahnResult.order;

        long[] dist = new long[n];
        int[] parent = new int[n];

        Arrays.fill(dist, Long.MAX_VALUE);
        Arrays.fill(parent, -1);

        int[] inDegree = new int[n];
        for (int u = 0; u < n; u++) {
            List<Graph.Edge> edges = graph.getAdj(u);
            if (edges != null) {
                for (Graph.Edge edge : edges) {
                    metrics.addEdgeScan();
                    inDegree[edge.to]++;
                }
            }
        }

        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                dist[i] = 0;
            }
        }

        for (int u : topOrder) {
            if (dist[u] != Long.MAX_VALUE) {
                List<Graph.Edge> edges = graph.getAdj(u);
                if (edges != null) {
                    for (Graph.Edge edge : edges) {
                        metrics.addEdgeScan();
                        int v = edge.to;
                        int weight = edge.weight;

                        if (dist[u] + weight < dist[v]) {
                            dist[v] = dist[u] + weight;
                            parent[v] = u;
                        }
                    }
                }
            }
        }

        long totalDistancesSum = 0;
        for (int i = 0; i < n; i++) {
            if (dist[i] != Long.MAX_VALUE) {
                totalDistancesSum += dist[i];
            }
        }

        return new Result(totalDistancesSum, dist, parent);
    }
}
