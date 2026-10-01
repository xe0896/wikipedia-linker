package api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Request {
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

    /** 
     * @param url
     * @return String
     * @throws IOException
     * @throws InterruptedException
     */
    public static String fetchAt(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().header("User-Agent", "What").build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        return response.body();
    }
}
