package dag;

import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Kahn {

    public static class Result {
        public List<Integer> order;
        public boolean hasCycle;

        public Result(List<Integer> order, boolean hasCycle) {
            this.order = order;
            this.hasCycle = hasCycle;
        }
    }

    public static Result topologicalSort(Graph graph) {
        int n = graph.getV();
        int[] inDegree = new int[n];

        for (int u = 0; u < n; u++) {
            List<Graph.Edge> edges = graph.getAdj(u);
            if (edges != null) {
                for (Graph.Edge edge : edges) {
                    inDegree[edge.to]++;
                }
            }
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }

        List<Integer> order = new ArrayList<>(n);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);

            List<Graph.Edge> edges = graph.getAdj(u);
            if (edges != null) {
                for (Graph.Edge edge : edges) {
                    int v = edge.to;
                    inDegree[v]--;

                    if (inDegree[v] == 0) {
                        queue.add(v);
                    }
                }
            }
        }

        if (order.size() < n) {
            return new Result(null, true);
        }

        return new Result(order, false);
    }
}
