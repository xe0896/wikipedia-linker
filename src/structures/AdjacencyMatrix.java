package structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdjacencyMatrix {
    private int V;
    private int E;

    private List<List<Boolean>> adj;
    private Map<String, Integer> articleIds;

    public AdjacencyMatrix() {
        this.V = 0;
        this.E = 0;
        this.adj = new ArrayList<>();
        this.articleIds = new HashMap<>();
    }

    public void validateInput(String v) {
        if (!articleIds.containsKey(v))
            throw new IllegalArgumentException(String.format("Provided string: %s does not exist in the graph", v));
    }

    public List<String> dfs(String v, String w) {
        int[] edgeTo = new int[V];
        validateInput(v);
        validateInput(w);

        Deque<Integer> queue = new ArrayDeque<>();

        boolean[] marked = new boolean[V];

        queue.offer(articleIds.get(v));
        marked[articleIds.get(v)] = true;

        while (!queue.isEmpty()) {
            int from = queue.poll();

            // List of booleans where the index denotes the ID of the vertex
            // meaning we can iterate and whenever we find a true that 'i'
            // is a valid vertex ID that we can get a connection 
            List<Boolean> row = adj.get(from);

            for (int i = 0; i < row.size(); i++) {
                if (!row.get(i))
                    continue;

                if (!marked[i]) {
                    edgeTo[i] = from;
                    marked[i] = true;
                    queue.offer(i);
                }
            }
        }

        Map<Integer, String> reverseIds = new HashMap<>();
        for (var entry : articleIds.entrySet()) {
            reverseIds.put(entry.getValue(), entry.getKey());
        }

        // The edgeTo array is doing src <- x <- y <- z <- dest
        // so to get our path we would need to first start at dest
        // which is gonna be the idx at the String we want (w), then
        // we reverse the list
        int cur = articleIds.get(w);

        if (!marked[cur])
            return List.of();

        List<String> path = new ArrayList<>();

        for (int t = 0; t < V; t++) {
            if (reverseIds.get(cur).equals(v)) {
                path.add(v);
                return path.reversed();
            }
            path.add(reverseIds.get(cur));
            cur = edgeTo[cur];
        }

        return List.of();
    }

    private int id(String v) {
        if (articleIds.containsKey(v))
            return articleIds.get(v);

        // Create a new list to add an extra vertex which has all false and is V+1
        // as we have created a new list and must make the old lists have an extra element
        List<Boolean> adjList = new ArrayList<>(Collections.nCopies(V + 1, false));
        adj.add(adjList);
        articleIds.put(v, V);

        // V is still the old length, so it wont include to the new list we
        // added above
        for (int w = 0; w < V; w++) {
            // Add the new vertex to each list by adding an extra false
            List<Boolean> list = adj.get(w);
            list.add(false);
        }

        return V++;
    }

    public boolean add(String v, String w) {
        int _v = id(v);
        List<Boolean> adjList = adj.get(_v);
        int _w = id(w);

        // Given that we have not established this extra connection and not
        // increment E incorrectly, we would set it to true
        if (!adjList.get(_w)) {
            adjList.set(_w, true);
            E++;
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d vertices, %d edges\n", V, E));
        for (List<Boolean> list : adj) {
            sb.append(list.toString());
            sb.append("\n");
        }

        return sb.toString();
    }
}
