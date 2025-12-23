package io.github.orizynpx.nodepad.view;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {
    // Static storage, used to keep images alive as long as the app runs
    private static final Map<String, Image> cache = new HashMap<>();

    public static Image get(String url) {
        if (url == null || url.isEmpty()) return null;

        // If we already have it, return it instantly
        if (cache.containsKey(url)) {
            return cache.get(url);
        }

        // Otherwise, download it in background and save it
        Image img = new Image(url, true);
        cache.put(url, img);
        return img;
    }
}