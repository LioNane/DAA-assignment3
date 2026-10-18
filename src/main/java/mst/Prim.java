package mst;

import graph.Graph;

import java.util.PriorityQueue;

public class Prim {

    public static Result findMST(Graph graph) {
        int n = graph.getV();
        boolean[] inMST = new boolean[n];
        PriorityQueue<Edge> pq = new PriorityQueue<>();
        long totalWeight = 0;
        int components = 0;

        for (int startVertex = 0; startVertex < n; startVertex++) {
            if (!inMST[startVertex]) {
                components++;

                pq.add(new Edge(-1, startVertex, 0));

                while (!pq.isEmpty()) {
                    Edge minEdge = pq.poll();
                    int currentVertex = minEdge.v;

                    if (inMST[currentVertex]) {
                        continue;
                    }

                    inMST[currentVertex] = true;
                    totalWeight += minEdge.weight;

                    for (Graph.Edge adjEdge : graph.getAdj(currentVertex)) {
                        if (!inMST[adjEdge.to]) {
                            pq.add(new Edge(currentVertex, adjEdge.to, adjEdge.weight));
                        }
                    }
                }
            }
        }

        return new Result(totalWeight, components);
    }
}