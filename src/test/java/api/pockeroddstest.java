package api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class pockeroddstest {
    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("OANOR_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("Set OANOR_API_KEY in this terminal before running.");
            return;
        }

        URI uri = URI.create("https://api.oanor.com/poker-api/v1/odds"
                + "?hero=AsKs&board=QsJsTs&opponents=2");

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("x-oanor-key", apiKey)
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("HTTP " + response.statusCode());
        System.out.println(response.body());
    }
}
