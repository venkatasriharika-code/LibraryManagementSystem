package com.library;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Looks up a real cover by title and author without requiring a user image upload. */
final class BookCoverLookup {
    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    private BookCoverLookup() { }

    static Cover find(String title, String author) throws IOException {
        String imageUrl = null;
        try { imageUrl = googleBooks(title, author); } catch (IOException ignored) { }
        if (imageUrl == null) {
            try { imageUrl = openLibrary(title, author); } catch (IOException ignored) { }
        }
        if (imageUrl == null) return null;

        HttpURLConnection connection = open(imageUrl);
        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            String mime = connection.getContentType();
            if (mime != null && mime.indexOf(';') >= 0) mime = mime.substring(0, mime.indexOf(';')).trim();
            if (!("image/jpeg".equalsIgnoreCase(mime) || "image/png".equalsIgnoreCase(mime)
                    || "image/webp".equalsIgnoreCase(mime))) return null;
            byte[] bytes = readLimited(connection, MAX_IMAGE_BYTES);
            return bytes.length == 0 ? null : new Cover(bytes, mime.toLowerCase());
        } finally { connection.disconnect(); }
    }

    private static String googleBooks(String title, String author) throws IOException {
        String query = "https://www.googleapis.com/books/v1/volumes?q=intitle%3A"
                + URLEncoder.encode(title, StandardCharsets.UTF_8)
                + "%20inauthor%3A" + URLEncoder.encode(author, StandardCharsets.UTF_8)
                + "&maxResults=5&fields=items(volumeInfo(imageLinks))";
        Matcher matcher = Pattern.compile("\\\"thumbnail\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
                .matcher(getText(query));
        if (!matcher.find()) return null;
        String result = matcher.group(1).replace("\\/", "/").replace("\\u003d", "=").replace("\\u0026", "&");
        if (result.startsWith("http://")) result = "https://" + result.substring(7);
        return result.startsWith("https://books.google.com/")
                || result.startsWith("https://books.googleusercontent.com/") ? result : null;
    }

    private static String openLibrary(String title, String author) throws IOException {
        String query = "https://openlibrary.org/search.json?title="
                + URLEncoder.encode(title, StandardCharsets.UTF_8)
                + "&author=" + URLEncoder.encode(author, StandardCharsets.UTF_8)
                + "&fields=cover_i&limit=5";
        Matcher matcher = Pattern.compile("\\\"cover_i\\\"\\s*:\\s*(\\d+)").matcher(getText(query));
        return matcher.find() ? "https://covers.openlibrary.org/b/id/" + matcher.group(1) + "-L.jpg?default=false" : null;
    }

    private static String getText(String address) throws IOException {
        HttpURLConnection connection = open(address);
        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) throw new IOException("Cover source unavailable");
            return new String(readLimited(connection, 1024 * 1024), StandardCharsets.UTF_8);
        } finally { connection.disconnect(); }
    }

    private static HttpURLConnection open(String address) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "LibraryHub/1.0 (educational library project)");
        return connection;
    }

    private static byte[] readLimited(HttpURLConnection connection, int limit) throws IOException {
        try (java.io.InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (output.size() + count > limit) throw new IOException("Image response is too large");
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }

    static final class Cover {
        final byte[] bytes;
        final String mimeType;
        Cover(byte[] bytes, String mimeType) { this.bytes = bytes; this.mimeType = mimeType; }
    }
}
