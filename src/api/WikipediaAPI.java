package api;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public void addBatchTitle(Map<String, List<String>> map, JsonNode data) throws JsonProcessingException {
        for (JsonNode node : data.get("query").get("pages")) {
            System.out.println("----" + Request.prettyPrint(node.toString()));
        }
    }

    /*
    public Map<String, List<String>> fetchBatchTitles(List<String> parameters, List<String> titles,
            Optional<String> endpoint) {
        Map<String, List<String>> map = new HashMap<>();
    
        String encodedTitle = String.join("|", titles);
    
        List<String> mutable = new ArrayList<>(parameters);
    
        mutable.add("titles");
        mutable.add(encodedTitle);
    
        String queries = Request.queries(mutable);
    
        String fullUrl = URL + (endpoint.isEmpty() ? "" : endpoint.get());
    
        String initialResponse = Request
                .fetchAt(String.format("%s?%s", fullUrl, queries));
    
        ObjectMapper mapper = new ObjectMapper();
    
        JsonNode initNode = mapper.readTree(initialResponse);
    
        //addTitle(titles, initNode);
    
        if (!initNode.has("continue") && !initNode.has("batchcomplete")) {
            System.out.println("No continue or batchcomplete (initial)");
            System.exit(1);
        }
    
        String continueStr = initNode.get("continue").get("plcontinue").asText();
    }
    */

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

    public List<String> fetchTitles(List<String> parameters, String title, Optional<String> endpoint)
            throws IOException, InterruptedException {
        String encodedTitle = encoder(title);

        // Required as the parameters list was created via List.of() making it immutable
        List<String> mutable = new ArrayList<>(parameters);
        List<String> titles = new ArrayList<>();

        // 'mutable' as List.of() didnt make it mutable, adds an extra query=value to the endpoint
        // the title that has been encoded to UTF-8
        mutable.add("titles");
        mutable.add(encodedTitle);

        String queries = Request.queries(mutable);

        String fullUrl = URL + (endpoint.isEmpty() ? "" : endpoint.get());

        String initialResponse = Request
                .fetchAt(String.format("%s?%s", fullUrl, queries));

        ObjectMapper mapper = new ObjectMapper();

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

                addTitle(titles, node);

                continueStr = (!node.has("continue")) ? null : node.get("continue").get("plcontinue").asText();
            } catch (Exception e) {
                System.out.println(Request.prettyPrint(response));
                System.exit(1);
            }
        }

        return titles;
    }
}
