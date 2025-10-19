package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DogApiBreedFetcher implements BreedFetcher {
    private static final String BASE = "https://dog.ceo/api/breed/";
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        if (breed == null || breed.isBlank()) {
            throw new BreedNotFoundException("Breed must be non-empty.");
        }
        String url = BASE + breed.toLowerCase(Locale.ROOT) + "/list";
        Request req = new Request.Builder().url(url).build();

        try (Response resp = client.newCall(req).execute()) {
            if (resp.body() == null) throw new IOException("Empty response body");

            // If the server returns 404 for an invalid breed, map it to the checked exception:
            if (resp.code() == 404) {
                throw new BreedNotFoundException(breed);
            }
            if (!resp.isSuccessful()) {
                throw new IOException("HTTP " + resp.code());
            }

            String body = resp.body().string();
            JSONObject root = new JSONObject(body);
            String status = root.optString("status", "error");
            if (!"success".equals(status)) {
                // Dog CEO typically includes an explanatory "message"
                String msg = root.optString("message", "Breed not found: " + breed);
                throw new BreedNotFoundException(msg);
            }

            JSONArray arr = root.getJSONArray("message");
            List<String> subs = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) subs.add(arr.getString(i));
            return subs;
        } catch (UnknownHostException | ConnectException | SocketTimeoutException e) {
            // Genuine network problem: keep as RuntimeException so tests fail loudly if offline.
            throw new RuntimeException("Network error: " + e.getMessage(), e);
        } catch (IOException io) {
            // Other I/O problems (bad HTTP, empty body, etc.)
            throw new RuntimeException("Network error: " + io.getMessage(), io);
        }
    }
}
