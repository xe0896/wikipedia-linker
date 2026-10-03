package api;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WikipediaAPI {
    public static final String URL = "https://en.wikipedia.org/w/api.php";
    private static final int LIMIT = 50; // Amount of titles allowed in the titles= parameter

    public WikipediaAPI() {
    }

    public String encoder(String message) {
        return URLEncoder.encode(message, StandardCharsets.UTF_8);
    }

    private void addTitle(List<String> titles, JsonNode data) {
        // Given a list of current titles append extra titles provided by 'data'
        // which have the structure below and iterate over the `node` 
        JsonNode node = data.get("query").get("pages").elements().next().get("links");

        for (JsonNode entry : node) {
            titles.add(entry.get("title").asText());
        }
    }

    public void addBatchTitle(Map<String, Set<String>> map, JsonNode data) throws JsonProcessingException {
        for (JsonNode node : data.get("query").get("pages")) {
            System.out.println(node);
        }
    }

    public static void add(Map<String, Set<String>> map, JsonNode data) throws JsonProcessingException {
        //System.out.println(Request.prettyPrint(data.get("query").get("pages")));
        for (JsonNode node : data.get("query").get("pages")) {
            String title = node.get("title").asText();
            if (node.has("links")) {
                if (!map.containsKey(title))
                    map.put(title, new HashSet<>());

                Set<String> set = map.get(title);

                for (JsonNode entry : node.get("links")) {
                    if (entry.get("ns").asInt() == 0) {
                        set.add(entry.get("title").asText());
                    }
                }
            }
        }
    }

    public String fetchJsonWiki(List<String> parameters, String title, Optional<String> endpoint)
            throws IOException, InterruptedException {
        String encodedTitle = encoder(title);

        // Required as the parameters list was created via List.of() making it immutable
        List<String> mutable = new ArrayList<>(parameters);

        // 'mutable' as List.of() didnt make it mutable, adds an extra query=value to the endpoint
        // the title that has been encoded to UTF-8
        mutable.add("titles");
        mutable.add(encodedTitle);

        String queries = Request.queries(mutable);

        String fullUrl = URL + (endpoint.isEmpty() ? "" : endpoint.get());

        String initialResponse = Request
                .fetchAt(String.format("%s?%s", fullUrl, queries));

        return initialResponse;
    }

    public void bfs(List<String> parameters) throws IOException, InterruptedException {
        Map<String, Set<String>> map = new HashMap<>();
        Set<String> marked = new HashSet<>();
        Map<String, String> parent = new HashMap<>();

        Deque<String> queue = new ArrayDeque<>();

        queue.offer("Miniminter");

        while (!queue.isEmpty()) {
            String cur = queue.poll();
            System.out.println("Expanding: " + cur);

            List<String> neighbours = fetchTitles(parameters, cur, Optional.empty());

            for (String neighbour : neighbours) {
                if (!marked.contains(neighbour)) {
                    marked.add(neighbour);
                    if (neighbour.equals("JME")) {
                        String target = "JME";

                        List<String> path = new ArrayList<>();
                        while (target != null) {
                            path.add(target);
                            target = parent.get(target);
                        }

                        System.out.println(path.reversed());
                    }
                    queue.offer(neighbour);
                    parent.put(neighbour, cur);
                }
            }
        }
    }

    public List<String> endpointWithEncodedTitle(List<String> params, String title) {
        String encodedTitle = encoder(title);

        // Required as the parameters list was created via List.of() making it immutable
        List<String> mutable = new ArrayList<>(params);

        // 'mutable' as List.of() didnt make it mutable, adds an extra query=value to the endpoint
        // the title that has been encoded to UTF-8
        mutable.add("titles");
        mutable.add(encodedTitle);

        return mutable;
    }

    // Given a starting title, we grab the titles for that then perform a level by level search so BFS
    // so we apply the same thing for its titles and whenever we find the end title then we can use that
    // as the path, we need to store some path as we go along
    public List<String> fetchTitles(List<String> parameters, String title, Optional<String> endpoint)
            throws IOException, InterruptedException {
        List<String> mutable = endpointWithEncodedTitle(parameters, title);
        List<String> titles = new ArrayList<>();

        String queries = Request.queries(mutable);

        String fullUrl = URL + (endpoint.isEmpty() ? "" : endpoint.get());

        String initialResponse = Request
                .fetchAt(String.format("%s?%s", fullUrl, queries));

        ObjectMapper mapper = new ObjectMapper();

        // Assumes that a response is JSON, sometimes we may be given HTTP to say we have 
        // been rate-limited which should lead to some exception being thrown
        JsonNode initNode = mapper.readTree(initialResponse);

        addTitle(titles, initNode);

        if (!initNode.has("continue") && !initNode.has("batchcomplete")) {
            System.out.println("No continue or batchcomplete (initial)");
            System.exit(1);
        }

        if (initNode.has("batchcomplete"))
            return titles;

        String continueStr = initNode.get("continue").get("plcontinue").asText();

        while (continueStr != null) {
            String url = String.format("%s?%s&plcontinue=%s&continue=%s", fullUrl, queries, encoder(continueStr),
                    encoder("||"));

            String response = Request.fetchAt(url);
            try {
                JsonNode node = mapper.readTree(response);

                // Should not trigger but relies on it so seems sensible
                if (!node.has("continue") && !node.has("batchcomplete")) {
                    System.out.println("No continue or batchcomplete");
                    System.out.println("This has no node: " + node.has("continue"));
                    System.out.println(node.toPrettyString());
                    System.exit(1);
                }

                continueStr = (!node.has("continue")) ? null : node.get("continue").get("plcontinue").asText();
            } catch (Exception e) {
                System.out.println(Request.prettyPrint(response));
                System.exit(1);
            }
        }

        return titles;
    }
}
