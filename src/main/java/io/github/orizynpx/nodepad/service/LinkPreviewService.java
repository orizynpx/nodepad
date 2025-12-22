package io.github.orizynpx.nodepad.service;

import io.github.orizynpx.nodepad.dao.LinkRepository;
import io.github.orizynpx.nodepad.model.LinkMetadata;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkPreviewService {
    private final LinkRepository linkRepo;
    private final OkHttpClient client = new OkHttpClient();

    // Regex for Open Graph tags
    private static final Pattern TITLE_TAG = Pattern.compile("<meta property=\"og:title\" content=\"([^\"]*)\"");
    private static final Pattern DESC_TAG = Pattern.compile("<meta property=\"og:description\" content=\"([^\"]*)\"");
    private static final Pattern IMG_TAG = Pattern.compile("<meta property=\"og:image\" content=\"([^\"]*)\"");

    public LinkPreviewService(LinkRepository linkRepo) {
        this.linkRepo = linkRepo;
    }

    public CompletableFuture<LinkMetadata> fetchPreview(String url) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. Check Cache
            var cached = linkRepo.findByUrl(url);
            if (cached.isPresent()) return cached.get();

            // 2. Network Request
            try {
                Request request = new Request.Builder().url(url).build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) return null;

                    String html = response.body().string();

                    String title = extract(html, TITLE_TAG, "No Title");
                    String desc = extract(html, DESC_TAG, "No Description");
                    String img = extract(html, IMG_TAG, "");

                    LinkMetadata meta = new LinkMetadata(url, title, desc, img);
                    linkRepo.save(meta); // 3. Cache it
                    return meta;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new LinkMetadata(url, "Error loading preview", e.getMessage(), "");
            }
        });
    }

    private String extract(String html, Pattern pattern, String defaultValue) {
        Matcher m = pattern.matcher(html);
        return m.find() ? m.group(1) : defaultValue;
    }
}