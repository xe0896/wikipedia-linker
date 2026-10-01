package structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Digraph {
    private int E;
    private int V;
    private List<Integer> indegree; // Amount of vertex pointed at vertex i
    private List<List<Integer>> adj; // Adjacency list, id points to other id's that it points to
    private Map<String, Integer> articleIds; // Maps topics to id's

    public Digraph() {
        this.E = 0; // Amount of edges
        this.V = 0; // Amount of vertices
        this.articleIds = new HashMap<>();
        this.indegree = new ArrayList<>();
        this.adj = new ArrayList<>();
    }

    public void validateInput(String v) {
        if (!articleIds.containsKey(v))
            throw new IllegalArgumentException(String.format("Provided string: %s does not exist in the graph", v));
    }

    public List<String> bfs(String v, String w) {
        // A list that contains an edgeTo[to] = from path, since BFS
        // would encounter nodes one distance layer by layer, the shortest
        // path to a node would guranteed to be the shortest, making it
        // not overwritten hence the if(!marked[to])

        // Only one predecessor for a newly discovered vertex and BFS finds the
        // shortest path to it so we can mark it as visited and not overwrite
        int[] edgeTo = new int[V];

        validateInput(v);
        validateInput(w);

        Deque<Integer> queue = new ArrayDeque<>();

        boolean[] marked = new boolean[V];

        // Starting point ID is located at articleIds, so we index that
        // first and get the idx associated with it and mark it
        marked[articleIds.get(v)] = true;

        // Kick off the BFS
        queue.offer(articleIds.get(v));

        while (!queue.isEmpty()) {
            int from = queue.poll();
            // Enqueue each node into the queue, if it has been seen already
            // no need to process as BFS would of found the shortest path
            // to that vertex
            for (int to : adj.get(from)) {
                if (!marked[to]) {
                    // Ideally if to == articleIds.get(w) then we would
                    // stop there as we found our path, requires a outer break
                    edgeTo[to] = from;
                    marked[to] = true;
                    queue.offer(to);
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
        // An id that already exists can grab its id from the map
        if (articleIds.containsKey(v))
            return articleIds.get(v);

        // A new id would be 'V' and also add a new indegree entry for 
        // this id with a indegree of 0 as nothing has pointed to it yet
        articleIds.put(v, V);
        indegree.add(0);
        // A new list entry in the adjacency list, the location would be
        // at location V meaning that it points to this new entry
        adj.add(new ArrayList<>());

        // Return V++ as it would post-increment to point to the next
        // incoming id
        return V++;
    }

    public void add(String v, String w) {
        // Grab the index, may create or use the already stateful map
        int v_id = id(v);

        // Grab the adjacency list, may be new or may have some elements already
        List<Integer> adjList = adj.get(v_id);

        // id of what is being pointed to, may be already seen or be a new V--
        int w_id = id(w);

        // Increment the amount of vertices pointed to w
        indegree.set(w_id, indegree.get(w_id) + 1);
        adjList.add(id(w));

        // Amount of edges has increased by one
        E++;
    }

    @Override
    public String toString() {
        Map<Integer, String> articleNames = new HashMap<>();
        for (var entry : articleIds.entrySet()) {
            articleNames.put(entry.getValue(), entry.getKey());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(V + " vertices, " + E + " edges " + "\n");
        for (int v = 0; v < V; v++) {
            sb.append(String.format("%s: ", articleNames.get(v)));
            sb.append("[");
            for (int _w = 0; _w < adj.get(v).size(); _w++) {
                int w = adj.get(v).get(_w);
                sb.append(String.format("%s%s", articleNames.get(w), (_w != adj.get(v).size() - 1) ? ", " : ""));
            }
            sb.append("]");
            if (v != V - 1)
                sb.append("\n");
        }

        return sb.toString();
    }
}
