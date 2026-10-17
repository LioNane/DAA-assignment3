package mst;

import graph.DSU;
import graph.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Kruskal {

    public static Result findMST(Graph graph) {
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

        Collections.sort(allEdges);

        DSU dsu = new DSU(n);

        long totalWeight = 0;
        int components = n;
        for (Edge edge : allEdges) {
            int rootU = dsu.find(edge.u);
            int rootV = dsu.find(edge.v);

            if (rootU != rootV) {
                dsu.union(edge.u, edge.v);
                totalWeight += edge.weight;
                components--;
            }
        }

        return new Result(totalWeight, components);
    }
}