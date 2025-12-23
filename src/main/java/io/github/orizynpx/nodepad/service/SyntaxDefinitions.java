package io.github.orizynpx.nodepad.service;

import java.util.regex.Pattern;

public class SyntaxDefinitions {

    // --- REGEX PATTERNS ---
    // Explanation:
    // @id      -> Matches the tag name
    // \\(      -> Matches literal open parenthesis
    // ([^)]+)  -> Capturing Group 1: Matches anything EXCEPT a closing parenthesis
    // \\)      -> Matches literal closing parenthesis

    public static final Pattern ID = Pattern.compile("@id\\(([^)]+)\\)");

    // Supports both @req(...) and @requires(...)
    public static final Pattern REQ = Pattern.compile("@(?:req|requires)\\(([^)]+)\\)");

    public static final Pattern ISBN = Pattern.compile("@isbn\\(([^)]+)\\)");

    public static final Pattern URL = Pattern.compile("@url\\(([^)]+)\\)");

    // Simple tag (no parentheses)
    public static final String DONE_TAG = "@done";


    // --- VALIDATION LOGIC ---

    /**
     * Checks if an ISBN string looks valid (length 10 or 13, digits/dashes only).
     * Used by IsbnProcessor to prevent bad API calls.
     */
    public static boolean isValidIsbn(String isbn) {
        if (isbn == null) return false;
        // Remove dashes for length check
        String clean = isbn.replaceAll("-", "").trim();
        // Basic check: Must be 10 or 13 digits long
        return (clean.length() == 10 || clean.length() == 13) && clean.matches("\\d+");
    }

    /**
     * Basic URL validation to prevent crashes on malformed links.
     */
    public static boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }
}