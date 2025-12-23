package io.github.orizynpx.nodepad.infrastructure.external;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.orizynpx.nodepad.domain.model.BookMetadata;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class OpenLibraryClient {
    private final OkHttpClient client = new OkHttpClient();

    public CompletableFuture<BookMetadata> fetchBook(String isbn) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String cleanIsbn = isbn.replaceAll("[^0-9]", "");
                String url = "https://openlibrary.org/api/books?bibkeys=ISBN:" + cleanIsbn + "&jscmd=data&format=json";

                Request request = new Request.Builder().url(url).build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) return null;

                    String jsonStr = response.body().string();
                    JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
                    String key = "ISBN:" + cleanIsbn;

                    if (json.has(key)) {
                        JsonObject data = json.getAsJsonObject(key);
                        String title = data.has("title") ? data.get("title").getAsString() : "Unknown";
                        String img = data.has("cover") ? data.getAsJsonObject("cover").get("medium").getAsString() : "";

                        return new BookMetadata(cleanIsbn, title, "From OpenLibrary", img);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}