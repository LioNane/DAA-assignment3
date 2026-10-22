package mst;

import graph.DSU;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Kruskal {

    public static Result findMST(Graph graph, Metrics metrics) {
        int n = graph.getV();
        List<Edge> allEdges = new ArrayList<>();

        for (int u = 0; u < n; u++) {
            for (Graph.Edge adjEdge : graph.getAdj(u)) {
                int v = adjEdge.to;
                int weight = adjEdge.weight;

                if (u < v) {
                    allEdges.add(new Edge(u, v, weight));
                }
            }
        }
        
        allEdges.sort((e1, e2) -> {
            metrics.addComparison();
            return Integer.compare(e1.weight, e2.weight);
        });

        DSU dsu = new DSU(n);

        long totalWeight = 0;
        int components = n;
        for (Edge edge : allEdges) {
            metrics.addEdgeScan();
            int rootU = dsu.find(edge.u, metrics);
            int rootV = dsu.find(edge.v, metrics);

            if (rootU != rootV) {
                dsu.union(edge.u, edge.v, metrics);
                totalWeight += edge.weight;
                components--;
            }
        }

        return new Result(totalWeight, components);
    }
}