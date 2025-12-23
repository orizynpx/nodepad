package io.github.orizynpx.nodepad.app;

import java.awt.Desktop;
import java.net.URI;

public class BrowserUtil {

    public static void open(String url) {
        if (url == null || url.isEmpty()) return;

        try {
            // Standard Java Desktop API
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Fallback for some Linux distros or weird environments
                System.err.println("Desktop API not supported. Cannot open: " + url);
                // Optional: You could try Runtime.exec("xdg-open " + url) here for Linux
            }
        } catch (Exception e) {
            System.err.println("Failed to open URL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}