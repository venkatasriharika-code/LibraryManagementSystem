package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // ADD BOOK, including an optional image stored in MySQL.
    public boolean addBook(Book book) {
        if (book.getCoverImage() == null || book.getCoverImage().length == 0
                || book.getCoverMimeType() == null) return false;
        String sql = "INSERT INTO books "
                + "(title, author, category, available_copies, cover_image, cover_mime_type) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory());
            ps.setInt(4, book.getAvailableCopies());
            ps.setBytes(5, book.getCoverImage());
            ps.setString(6, book.getCoverMimeType());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Book searchBook(int bookId) {
        String sql = "SELECT book_id, title, author, category, available_copies, "
                + "cover_image IS NOT NULL AS has_cover FROM books WHERE book_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return readBook(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean issueBook(int bookId) {
        String sql = "UPDATE books SET available_copies = available_copies - 1 "
                + "WHERE book_id = ? AND available_copies > 0";
        return executeBookIdUpdate(sql, bookId);
    }

    public boolean returnBook(int bookId) {
        String sql = "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?";
        return executeBookIdUpdate(sql, bookId);
    }

    private boolean executeBookIdUpdate(String sql, int bookId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, category = ?, available_copies = ? WHERE book_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory());
            ps.setInt(4, book.getAvailableCopies());
            ps.setInt(5, book.getBookId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        return executeBookIdUpdate(sql, bookId);
    }

    public boolean updateBookCover(int bookId, byte[] image, String mimeType) {
        String sql = "UPDATE books SET cover_image = ?, cover_mime_type = ? WHERE book_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBytes(1, image);
            ps.setString(2, mimeType);
            ps.setInt(3, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeBookCover(int bookId) {
        String sql = "UPDATE books SET cover_image = NULL, cover_mime_type = NULL WHERE book_id = ?";
        return executeBookIdUpdate(sql, bookId);
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author, category, available_copies, "
                + "cover_image IS NOT NULL AS has_cover FROM books ORDER BY title";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) books.add(readBook(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public List<Book> getBooksPage(int offset, int limit, String category, String sort) {
        List<Book> books = new ArrayList<>();
        String orderBy;
        if ("author".equals(sort)) orderBy = "author, title";
        else if ("newest".equals(sort)) orderBy = "book_id DESC";
        else orderBy = "title, book_id";
        String categoryFilter = category == null ? "" : category.trim();
        String where = categoryFilter.isEmpty() ? "" : "WHERE category = ? ";
        String sql = "SELECT book_id, title, author, category, available_copies, "
                + "cover_image IS NOT NULL AS has_cover FROM books "
                + where + "ORDER BY " + orderBy + " LIMIT ? OFFSET ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            int parameter = 1;
            if (!categoryFilter.isEmpty()) ps.setString(parameter++, categoryFilter);
            ps.setInt(parameter++, limit);
            ps.setInt(parameter, offset);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) books.add(readBook(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return books;
    }

    public int countBooks(String category) {
        String categoryFilter = category == null ? "" : category.trim();
        String sql = "SELECT COUNT(*) FROM books" + (categoryFilter.isEmpty() ? "" : " WHERE category = ?");
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (!categoryFilter.isEmpty()) ps.setString(1, categoryFilter);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT TRIM(category) AS category FROM books "
                + "WHERE category IS NOT NULL AND TRIM(category) <> '' ORDER BY category";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) categories.add(rs.getString("category"));
        } catch (SQLException e) { e.printStackTrace(); }
        return categories;
    }

    public List<Book> searchBooksPage(String query, int offset, int limit) {
        List<Book> books = new ArrayList<>();
        String trimmed = query == null ? "" : query.trim();
        boolean numericId = trimmed.matches("\\d+");
        String predicate = numericId ? "book_id = ?" : "(title LIKE ? OR author LIKE ? OR category LIKE ?)";
        String sql = "SELECT book_id, title, author, category, available_copies, "
                + "cover_image IS NOT NULL AS has_cover FROM books WHERE " + predicate
                + " ORDER BY title, book_id LIMIT ? OFFSET ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            int next = 1;
            if (numericId) ps.setInt(next++, Integer.parseInt(trimmed));
            else {
                String match = "%" + trimmed + "%";
                ps.setString(next++, match); ps.setString(next++, match); ps.setString(next++, match);
            }
            ps.setInt(next++, limit); ps.setInt(next, offset);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) books.add(readBook(rs)); }
        } catch (SQLException | NumberFormatException e) { e.printStackTrace(); }
        return books;
    }

    public int countSearchBooks(String query) {
        String trimmed = query == null ? "" : query.trim();
        boolean numericId = trimmed.matches("\\d+");
        String predicate = numericId ? "book_id = ?" : "(title LIKE ? OR author LIKE ? OR category LIKE ?)";
        String sql = "SELECT COUNT(*) FROM books WHERE " + predicate;
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (numericId) ps.setInt(1, Integer.parseInt(trimmed));
            else {
                String match = "%" + trimmed + "%";
                ps.setString(1, match); ps.setString(2, match); ps.setString(3, match);
            }
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException | NumberFormatException e) { e.printStackTrace(); return 0; }
    }

    public List<Book> searchBooks(String query) {
        List<Book> books = new ArrayList<>();
        String trimmed = query.trim();
        boolean numericId = trimmed.matches("\\d+");
        String sql = "SELECT book_id, title, author, category, available_copies, "
                + "cover_image IS NOT NULL AS has_cover FROM books WHERE "
                + (numericId ? "book_id = ?" : "title LIKE ? OR author LIKE ?") + " ORDER BY title";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (numericId) {
                ps.setInt(1, Integer.parseInt(trimmed));
            } else {
                String match = "%" + trimmed + "%";
                ps.setString(1, match);
                ps.setString(2, match);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) books.add(readBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public Book getBookCover(int bookId) {
        String sql = "SELECT book_id, title, author, category, available_copies, cover_image, cover_mime_type "
                + "FROM books WHERE book_id = ? AND cover_image IS NOT NULL";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Book(rs.getInt("book_id"), rs.getString("title"),
                            rs.getString("author"), rs.getInt("available_copies"),
                            rs.getBytes("cover_image"), rs.getString("cover_mime_type"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Book readBook(ResultSet rs) throws SQLException {
        Book book = new Book(rs.getInt("book_id"), rs.getString("title"),
                rs.getString("author"), rs.getInt("available_copies"));
        book.setCategory(rs.getString("category"));
        book.setHasCover(rs.getBoolean("has_cover"));
        return book;
    }
}
