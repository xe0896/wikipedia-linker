import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import api.Request;
import api.WikipediaAPI;
import structures.AdjacencyMatrix;
import structures.Digraph;

class Main {

    private static final List<String> parameters = List.of("action", "query", "prop", "links", "format", "json",
            "pllimit", "500");

    public static void main(String[] args) throws IOException, InterruptedException {
        WikipediaAPI api = new WikipediaAPI();
        ObjectMapper mapper = new ObjectMapper();

        api.bfs(parameters);

        /*
        
        Map<String, Set<String>> map = new HashMap<>();
        
        List<String> mutable = new ArrayList<>(parameters);
        mutable.add("titles");
        mutable.add(api.encoder(
                "Zerkaa|Miniminter|Vikkstar123|Behzinga|TBJZL|Wroetoshaw|ChrisMD|Calfreezy|WillNE|Max Fosh|George Clarke (internet personality)|Theo Baker|Chunkz|Niko Omilana|Harry Pinero|Lachlan Power|LazarBeam|Muselk|FaZe Rug|GeorgeNotFound|Sapnap|Tubbo|Ranboo|TommyInnit|CaptainSparklez|Jaiden Animations|TheOdd1sOut|Ali-A|DanTDM|Grian"));
        
        String queries = Request.queries(mutable);
        
        System.out.println(queries);
        
        String initialResponse = Request.fetchAt(String.format("%s?%s", WikipediaAPI.URL, queries));
        
        JsonNode initNode = mapper.readTree(initialResponse);
        
        add(map, initNode);
        
        //System.out.println(Request.prettyPrint(initNode));
        
        if (initNode.has("batchcomplete")) {
            System.out.println(map);
        }
        
        String continueStr = initNode.get("continue").get("plcontinue").asText();
        
        while (continueStr != null) {
            String url = String.format("%s?%s&plcontinue=%s&continue=%s", WikipediaAPI.URL, queries,
                    api.encoder(continueStr),
                    api.encoder("||"));
        
            String response = Request.fetchAt(url);
        
            try {
                JsonNode node = mapper.readTree(response);
        
                // Should not trigger but relies on it so seems sensible
                if (!node.has("continue") && !node.has("batchcomplete")) {
                    System.out.println("Incorrect structure: " + node.toPrettyString());
                    System.exit(1);
                }
        
                add(map, node);
        
                continueStr = (!node.has("continue")) ? null : node.get("continue").get("plcontinue").asText();
            } catch (Exception e) {
                System.out.println(Request.prettyPrint(response));
                e.printStackTrace();
                System.exit(1);
            }
        }
        
        for (String title : map.keySet()) {
            System.out.printf("Title: %s, amount: %d\n", title, map.get(title).size());
        }
        
        */
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
}