package data_access;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import use_case.hand_odds.HandOddsGateway;

public class OanorHandOddsGateway implements HandOddsGateway {
    private static final String ENDPOINT = "https://api.oanor.com/poker-api/v1/odds";
    private static final String CARD = "[2-9TJQKA][cdhs]";

    private final HttpClient client = HttpClient.newHttpClient();
    private final String apiKey = System.getenv("OANOR_API_KEY");
    private long nextRequest = System.nanoTime();

    @Override
    public synchronized double getWinProbability(String holeCards, String board, int opponents)
            throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException("Set OANOR_API_KEY before requesting odds.");
        }
        if (holeCards == null || !holeCards.matches("(" + CARD + "){2}")) {
            throw new IllegalArgumentException("Expected two hole cards, for example AsKs.");
        }
        if (board == null || !(board.isEmpty() || board.matches("(" + CARD + "){3,5}"))) {
            throw new IllegalArgumentException("Expected an empty board or 3 to 5 cards.");
        }
        if (opponents < 1 || opponents > 5) {
            throw new IllegalArgumentException("Expected 1 to 5 opponents.");
        }

        String url = ENDPOINT + "?hero=" + holeCards + "&opponents=" + opponents;
        if (!board.isEmpty()) {
            url += "&board=" + board;
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("x-oanor-key", apiKey)
                .GET()
                .build();

        // Space requests from this gateway when several bots receive cards together.
        TimeUnit.NANOSECONDS.sleep(nextRequest - System.nanoTime());
        nextRequest = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(550);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Oanor odds request returned HTTP " + response.statusCode());
        }
        return readProbability(response.body());
    }

    static double readProbability(String body) throws IOException {
        try {
            double percent = new JSONObject(body).getJSONObject("data").getDouble("win_percent");
            if (!Double.isFinite(percent) || percent < 0 || percent > 100) {
                throw new IOException("Oanor returned an invalid win percentage.");
            }
            return percent / 100.0;
        } catch (JSONException ex) {
            throw new IOException("Oanor response is missing a valid data.win_percent.", ex);
        }
    }
}
