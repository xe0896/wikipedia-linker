package api;

import java.io.IOException;
import java.net.HttpRetryException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Request {
    private static final int retries = 30;

    /** 
     * Given a list of queries, create a String that would allow it to be directly
     * appended after an API endpoint 
     * @param queries A list of queries where starting at an even index 'i', index 'i + 1'
     *                would denote a pair where 'i' is the query and 'i + 1' is the value
     * @return String
     */
    public static String queries(List<String> queries) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < queries.size(); i += 2) {
            String query = queries.get(i);
            String value = queries.get(i + 1);

            sb.append(query);
            sb.append("=");
            sb.append(value);

            if (i != queries.size() - 2)
                sb.append("&");
        }

        return sb.toString();
    }

    /** 
     * @param url
     * @param type
     * @return T
     * @throws IOException
     * @throws InterruptedException
     */
    public static <T> T fetchJsonAt(String url, TypeReference<T> type) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(response.body(), type);
    }

    public static String fetchAt(String url) throws IOException, InterruptedException {
        return fetchAt(url, retries);
    }

    /** 
     * @param url
     * @return String
     * @throws IOException
     * @throws InterruptedException
     */
    public static String fetchAt(String url, int retries) throws IOException, InterruptedException {
        if (retries < 0) {
            throw new HttpRetryException("Retries of 30 has been exceeded", 0);
        }

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().header("User-Agent", "What").build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 429) {
            Thread.sleep(5000);
            System.out.println(response.headers());
            System.out.printf("Fetch retry: %d\n", retries);
            return fetchAt(url, retries - 1);
        }

        if (response.statusCode() != 200) {
            System.out.println("HTTP " + response.statusCode());
            System.out.println(response.body());
            throw new HttpRetryException("Status code is not 200", 0);
        }

        return response.body();
    }

    public static String prettyPrint(String body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        String prettyJson = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(mapper.readTree(body));

        return prettyJson;
    }

    public static String prettyPrint(JsonNode node) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);

        return prettyJson;
    }
}
