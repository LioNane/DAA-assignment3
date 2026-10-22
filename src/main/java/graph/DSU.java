package graph;

import metrics.Metrics;

public class DSU {
    private int[] parent;
    private int[] rank;
    private int componentsCount;

    public DSU(int n) {
        parent = new int[n];
        rank = new int[n];
        componentsCount = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
    }

    public int find(int x, Metrics metrics) {
        metrics.addDsuOp();
        if (parent[x] != x) {
            parent[x] = find(parent[x], metrics);
        }
        return parent[x];
    }

    public void union(int x, int y, Metrics metrics) {
        int rootX = find(x, metrics);
        int rootY = find(y, metrics);

        if (rootX == rootY) {
            return;
        }

        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }

        componentsCount--;
        metrics.addDsuOp();
    }

    public int components() {
        return componentsCount;
    }
}