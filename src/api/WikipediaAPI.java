package api;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WikipediaAPI {
    public static final String URL = "https://en.wikipedia.org/w/api.php";

    public WikipediaAPI() {
    }

    public String encoder(String message) {
        return URLEncoder.encode(message, StandardCharsets.UTF_8);
    }

    private void addTitle(List<String> titles, JsonNode data) {
        JsonNode node = data.get("query").get("pages").elements().next().get("links");

        for (JsonNode entry : node) {
            titles.add(entry.get("title").asText());
        }
    }

    public List<String> fetchTitles(List<String> parameters, String title, Optional<String> endpoint)
            throws IOException, InterruptedException {
        String encodedTitle = encoder(title);

        // Required as the parameters list was created via List.of() making it immutable
        List<String> mutable = new ArrayList<>(parameters);
        List<String> titles = new ArrayList<>();
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
                if (!node.has("continue") && !node.has("batchcomplete")) {
                    System.out.println("No continue or batchcomplete");
                    System.out.println("This has no node: " + node.has("continue"));
                    System.out.println(node.toPrettyString());
                    System.exit(1);
                }

                addTitle(titles, node);

                continueStr = (!node.has("continue")) ? null : node.get("continue").get("plcontinue").asText();
            } catch (Exception e) {
                System.out.println(prettyPrint(response));
                System.exit(1);
            }
        }

        return titles;
    }

    public String prettyPrint(String body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        String prettyJson = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(mapper.readTree(body));

        return prettyJson;
    }
}
