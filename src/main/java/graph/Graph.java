package graph;

import java.util.ArrayList;
import java.util.List;

public class Graph {

    public static class Edge {
        public int to;
        public int weight;

        public Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    private int n;
    private boolean isDirected;
    private List<List<Edge>> adjList;

    public Graph(int n, boolean isDirected) {
        this.n = n;
        this.isDirected = isDirected;
        adjList = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adjList.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v, int weight) {
        adjList.get(u).add(new Edge(v, weight));
        if (!isDirected && u != v){
            adjList.get(v).add(new Edge(u, weight));
        }
    }

    public List<Edge> getAdj(int u) {
        return adjList.get(u);
    }

    public int getV() {
        return n;
    }
}
