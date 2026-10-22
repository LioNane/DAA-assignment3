package metrics;

public class Metrics {
    private long dsuOps = 0;
    private long edgeScans = 0;
    private long comparisons = 0;
    private int maxDepth = 0;

    private long startTime;
    private long endTime;

    public long getDsuOps() { return dsuOps; }
    public long getEdgeScans() { return edgeScans; }
    public long getComparisons() { return comparisons; }
    public int getMaxDepth() { return maxDepth; }

    public void addDsuOp() { dsuOps++; }
    public void addEdgeScan() { edgeScans++; }
    public void addComparison() { comparisons++; }
    public void updateDepth(int currentDepth) {
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
    }

    public void startTimer() { startTime = System.nanoTime(); }
    public void stopTimer() { endTime = System.nanoTime(); }
    public double getTimeMs() { return (endTime - startTime) / 1000000.0; }

    public void reset() {
        dsuOps = 0;
        edgeScans = 0;
        comparisons = 0;
        maxDepth = 0;
        startTime = 0;
        endTime = 0;
    }
}
