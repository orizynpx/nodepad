package io.github.orizynpx.nodepad.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.orizynpx.nodepad.dao.BookRepository;
import io.github.orizynpx.nodepad.model.entity.BookMetadata;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class OpenLibraryService {

    private final BookRepository bookRepository;
    private final OkHttpClient client = new OkHttpClient();

    // Keep track of what we are currently fetching to prevent duplicates
    private final Set<String> pendingRequests = Collections.synchronizedSet(new HashSet<>());

    public OpenLibraryService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public CompletableFuture<BookMetadata> fetchBookInfo(String rawIsbn) {
        // 1. Sanitize, remove dashes to ensure consistent DB keys
        String isbn = rawIsbn.replaceAll("[^0-9]", "");

        if (isbn.isEmpty()) return CompletableFuture.completedFuture(null);

        return CompletableFuture.supplyAsync(() -> {
            // 2. Check DB cache
            var cached = bookRepository.findByIsbn(isbn);
            if (cached.isPresent()) {
                return cached.get();
            }

            // 3. Check requests that are ongoing
            // If we are already fetching this ISBN, don't start a new request
            if (pendingRequests.contains(isbn)) {
                return null;
            }
            pendingRequests.add(isbn);

            System.out.println("Cache Miss. Fetching API: " + isbn);

            try {
                // 4. Fetch API
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

                        String imageUrl = "";
                        if (bookData.has("cover")) {
                            imageUrl = bookData.getAsJsonObject("cover").get("medium").getAsString();
                        }

                        // 5. SAVE TO DB (Use the SANITIZED isbn)
                        BookMetadata metadata = new BookMetadata(isbn, title, "No description", imageUrl);
                        bookRepository.save(metadata);

                        System.out.println(">>> SAVED TO DB: " + title);
                        return metadata;
                    } else {
                        System.err.println("API returned success but JSON was missing key: " + key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Remove from pending set regardless of success/failure
                pendingRequests.remove(isbn);
            }
            return null;
        });
    }
}