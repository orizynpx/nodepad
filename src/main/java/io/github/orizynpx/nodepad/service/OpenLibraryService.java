package io.github.orizynpx.nodepad.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.orizynpx.nodepad.dao.BookCacheDao;
import io.github.orizynpx.nodepad.model.BookMetadata;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class OpenLibraryService {

    private final BookCacheDao bookCacheDao = new BookCacheDao();
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Async method to fetch book details.
     * 1. Checks DB.
     * 2. If missing, hits API.
     * 3. Saves to DB.
     */
    public CompletableFuture<BookMetadata> fetchBookInfo(String isbn) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. Check Cache
            var cached = bookCacheDao.findByIsbn(isbn);
            if (cached.isPresent()) {
                System.out.println("Cache Hit: " + isbn);
                return cached.get();
            }

            // 2. Fetch from API
            System.out.println("Cache Miss. Fetching API: " + isbn);
            try {
                // OpenLibrary API format: https://openlibrary.org/api/books?bibkeys=ISBN:9780134685991&jscmd=data&format=json
                String url = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&jscmd=data&format=json";
                Request request = new Request.Builder().url(url).build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) return null;

                    String jsonStr = response.body().string();
                    JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
                    String key = "ISBN:" + isbn;

                    if (json.has(key)) {
                        JsonObject bookData = json.getAsJsonObject(key);
                        String title = bookData.has("title") ? bookData.get("title").getAsString() : "Unknown Title";
                        String desc = "No description available."; // Description parsing can be complex in OL, keeping it simple

                        // Cover Image
                        String imageUrl = "";
                        if (bookData.has("cover")) {
                            imageUrl = bookData.getAsJsonObject("cover").get("medium").getAsString();
                        }

                        BookMetadata metadata = new BookMetadata(isbn, title, desc, imageUrl);

                        // 3. Save to Cache
                        bookCacheDao.save(metadata);
                        return metadata;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null; // Failed
        });
    }
}