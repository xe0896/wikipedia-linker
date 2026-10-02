import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import api.Request;
import api.WikipediaAPI;
import structures.AdjacencyMatrix;
import structures.Digraph;

class Main {

    private static final List<String> standardParameters = List.of("action", "query", "prop", "links", "format", "json",
            "pllimit", "500");

    public static void main(String[] args) throws IOException, InterruptedException {
        WikipediaAPI api = new WikipediaAPI();
        ObjectMapper mapper = new ObjectMapper();

        List<String> response = api.fetchTitles(standardParameters, "Albert Einstein", Optional.empty());

        // Given two (v,w) that we want to find a connection on. Start with v and perform BFS to its neighbours
        // that would consider more, the marked array has to now be a marked set that grows. In order to grow 
        // we would do a request per queue response, but then doing n queries would be very slow

        String responseStr = api.fetchJsonWiki(standardParameters, "Zerkaa|Miniminter", Optional.empty());

        JsonNode data = mapper.readTree(responseStr);

        api.addBatchTitle(null, data);

        /*
        String titles = String.join("|", response.subList(0, 50));
        
        System.out.println(titles);
        
        String response50 = api.fetchJsonWiki(standardQueries, titles, Optional.empty());
        
        System.out.println(Request.prettyPrint(response50));
        
        */
    }
}