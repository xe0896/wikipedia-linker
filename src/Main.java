import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import api.Request;
import api.WikipediaAPI;
import structures.AdjacencyMatrix;
import structures.Digraph;

class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        WikipediaAPI api = new WikipediaAPI();

        List<String> response = api.fetchTitles(
                List.of("action", "query", "prop", "links", "format", "json", "pllimit", "500"),
                "Albert Einstein", Optional.empty());

        System.out.println(response);
    }
}