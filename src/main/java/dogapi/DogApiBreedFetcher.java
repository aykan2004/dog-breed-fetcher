package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DogApiBreedFetcher implements BreedFetcher {
    private static final String BASE = "https://dog.ceo/api/breed/";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        if (breed == null || breed.isBlank()) {
            throw new BreedNotFoundException("Breed must be non-empty.");
        }
        String url = BASE + breed.toLowerCase(Locale.ROOT) + "/list";
        Request req = new Request.Builder().url(url).build();

        try (Response resp = client.newCall(req).execute()) {
            if (resp.body() == null) throw new IOException("Empty response body");
            String body = resp.body().string();

            JSONObject root = new JSONObject(body);
            String status = root.optString("status", "error");
            if (!"success".equals(status)) {
                String msg = root.optString("message", "Unknown API error");
                throw new BreedNotFoundException(msg);
            }

            JSONArray arr = root.getJSONArray("message");
            List<String> subs = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) subs.add(arr.getString(i));
            return subs;
        } catch (IOException io) {
            throw new RuntimeException("Network error: " + io.getMessage(), io);
        }
    }
}
