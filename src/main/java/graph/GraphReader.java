package graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GraphReader {

    public static Graph loadFromFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("File is empty");
            }

            String[] header = headerLine.trim().split("\\s+");
            int n = Integer.parseInt(header[0]);
            int m = Integer.parseInt(header[1]);
            boolean isDirected = header[2].equalsIgnoreCase("directed");

            Graph graph = new Graph(n, isDirected);

            for (int i = 0; i < m; i++) {
                String line = reader.readLine();
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                String[] edgeData = line.trim().split("\\s+");
                int u = Integer.parseInt(edgeData[0]);
                int v = Integer.parseInt(edgeData[1]);
                int weight = Integer.parseInt(edgeData[2]);

                graph.addEdge(u, v, weight);
            }
            return graph;
        }
    }
}
