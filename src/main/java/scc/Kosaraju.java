package scc;

import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Kosaraju {

    public static class Result {
        public int componentsCount;

        public Result(int componentsCount) {
            this.componentsCount = componentsCount;
        }
    }

    public static Result findSCC(Graph graph) {
        int n = graph.getV();
        List<Integer> finishOrder = new ArrayList<>(n);
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                iterativeDFS1(graph, i, visited, finishOrder);
            }
        }


        Graph reversedGraph = getReversedGraph(graph);

        boolean[] visited2 = new boolean[n];
        int componentsCount = 0;

        for (int i = finishOrder.size() - 1; i >= 0; i--) {
            int vertex = finishOrder.get(i);
            if (!visited2[vertex]) {
                componentsCount++;
                iterativeDFS2(reversedGraph, vertex, visited2);
            }
        }

        return new Result(componentsCount);
    }

    private static Graph getReversedGraph(Graph original) {
        int n = original.getV();
        Graph reversed = new Graph(n, true);

        for (int i = 0; i < n; i++) {
            List<Graph.Edge> edges = original.getAdj(i);
            if (edges != null) {
                for (Graph.Edge edge : edges) {
                    reversed.addEdge(edge.to, i, edge.weight);
                }
            }
        }

        return reversed;
    }

    private static void iterativeDFS1(Graph graph, int start, boolean[] visited, List<Integer> finishOrder) {
        Deque<Integer> stack = new ArrayDeque<>();
        int[] edgeIndex = new int[graph.getV()];

        stack.push(start);
        visited[start] = true;

        while (!stack.isEmpty()) {
            int u = stack.peek();
            List<Graph.Edge> edges = graph.getAdj(u);

            if (edges != null && edgeIndex[u] < edges.size()) {
                int v = edges.get(edgeIndex[u]).to;
                edgeIndex[u]++;

                if (!visited[v]) {
                    visited[v] = true;
                    stack.push(v);
                }
            } else {
                stack.pop();
                finishOrder.add(u);
            }
        }
    }

    private static void iterativeDFS2(Graph graph, int start, boolean[] visited) {
        Deque<Integer> stack = new ArrayDeque<>();

        stack.push(start);
        visited[start] = true;

        while (!stack.isEmpty()) {
            int u = stack.pop();

            List<Graph.Edge> edges = graph.getAdj(u);
            if (edges != null) {
                for (Graph.Edge edge : edges) {
                    int v = edge.to;
                    if (!visited[v]) {
                        visited[v] = true;
                        stack.push(v);
                    }
                }
            }
        }
    }
}
