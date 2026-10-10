package com.library;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Finds an Open Library cover by a book's title and author and stores it in MySQL. */
@WebServlet("/findBookCover")
public class FindBookCoverServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int bookId;
        try {
            bookId = Integer.parseInt(request.getParameter("bookId"));
        } catch (RuntimeException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A valid book ID is required.");
            return;
        }

        BookDAO dao = new BookDAO();
        Book book = dao.searchBook(bookId);
        if (book == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found.");
            return;
        }

        try {
            String imageUrl = null;
            try {
                imageUrl = findGoogleBooksCover(book.getTitle(), book.getAuthor());
            } catch (IOException ignored) {
                // Continue to the secondary catalog if Google Books is unavailable or rate-limited.
            }
            if (imageUrl == null) {
                try {
                    imageUrl = findOpenLibraryCover(book.getTitle(), book.getAuthor());
                } catch (IOException ignored) {
                    // No online catalog is reachable; return to the book page with the upload option.
                }
            }
            if (imageUrl == null) {
                response.sendRedirect(request.getContextPath() + "/bookDetails?bookId=" + bookId + "&cover=notfound");
                return;
            }

            HttpURLConnection imageConnection = open(imageUrl);
            try {
                if (imageConnection.getResponseCode() != HttpServletResponse.SC_OK) {
                    response.sendRedirect(request.getContextPath() + "/bookDetails?bookId=" + bookId + "&cover=notfound");
                    return;
                }
                String mime = imageConnection.getContentType();
                if (mime != null && mime.indexOf(';') >= 0) mime = mime.substring(0, mime.indexOf(';')).trim();
                if (!("image/jpeg".equalsIgnoreCase(mime) || "image/png".equalsIgnoreCase(mime)
                        || "image/webp".equalsIgnoreCase(mime))) {
                    response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Cover service returned an unsupported image.");
                    return;
                }
                byte[] bytes = readLimited(imageConnection, MAX_IMAGE_BYTES);
                if (bytes.length == 0 || !dao.updateBookCover(bookId, bytes, mime.toLowerCase())) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not save the cover in MySQL.");
                    return;
                }
            } finally {
                imageConnection.disconnect();
            }
            response.sendRedirect(request.getContextPath() + "/bookDetails?bookId=" + bookId + "&cover=saved");
        } catch (IOException ex) {
            throw new ServletException("Could not retrieve a cover from Open Library.", ex);
        }
    }

    private String findGoogleBooksCover(String title, String author) throws IOException {
        String query = "https://www.googleapis.com/books/v1/volumes?q=intitle%3A"
                + URLEncoder.encode(title, StandardCharsets.UTF_8)
                + "%20inauthor%3A" + URLEncoder.encode(author, StandardCharsets.UTF_8)
                + "&maxResults=5&fields=items(volumeInfo(imageLinks))";
        String json = getText(query);
        Matcher matcher = Pattern.compile("\\\"thumbnail\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
        if (!matcher.find()) return null;
        String imageUrl = matcher.group(1).replace("\\/", "/")
                .replace("\\u003d", "=").replace("\\u0026", "&");
        if (imageUrl.startsWith("http://")) imageUrl = "https://" + imageUrl.substring(7);
        return imageUrl.startsWith("https://books.google.com/")
                || imageUrl.startsWith("https://books.googleusercontent.com/") ? imageUrl : null;
    }

    private String findOpenLibraryCover(String title, String author) throws IOException {
        String query = "https://openlibrary.org/search.json?title="
                + URLEncoder.encode(title, StandardCharsets.UTF_8)
                + "&author=" + URLEncoder.encode(author, StandardCharsets.UTF_8)
                + "&fields=cover_i&limit=5";
        String json = getText(query);
        Matcher matcher = Pattern.compile("\\\"cover_i\\\"\\s*:\\s*(\\d+)").matcher(json);
        return matcher.find() ? "https://covers.openlibrary.org/b/id/" + matcher.group(1) + "-L.jpg?default=false" : null;
    }

    private HttpURLConnection open(String address) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "LibraryHub/1.0 (educational library project)");
        return connection;
    }

    private String getText(String address) throws IOException {
        HttpURLConnection connection = open(address);
        try {
            if (connection.getResponseCode() != HttpServletResponse.SC_OK) {
                throw new IOException("Cover search returned HTTP " + connection.getResponseCode());
            }
            return new String(readLimited(connection, 1024 * 1024), StandardCharsets.UTF_8);
        } finally {
            connection.disconnect();
        }
    }

    private byte[] readLimited(HttpURLConnection connection, int limit) throws IOException {
        try (java.io.InputStream input = connection.getInputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (output.size() + count > limit) throw new IOException("Cover response exceeds size limit.");
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }
}
