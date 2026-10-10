package com.library;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/searchBook")
public class SearchBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 24;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String query = request.getParameter("q");
        String oldBookId = request.getParameter("bookId");
        BookDAO dao = new BookDAO();
        List<Book> books;
        String label;
        int total;
        int page = parsePage(request.getParameter("page"));

        if (oldBookId != null && !oldBookId.trim().isEmpty()) {
            try {
                Book book = dao.searchBook(Integer.parseInt(oldBookId.trim()));
                books = book == null ? Collections.emptyList() : Collections.singletonList(book);
                total = books.size();
                label = "ID " + oldBookId.trim();
            } catch (NumberFormatException ex) {
                books = Collections.emptyList(); total = 0; label = oldBookId.trim();
            }
        } else {
            query = query == null ? "" : query.trim();
            total = query.isEmpty() ? 0 : dao.countSearchBooks(query);
            int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            page = Math.min(page, pages);
            books = query.isEmpty() ? Collections.emptyList()
                    : dao.searchBooksPage(query, (page - 1) * PAGE_SIZE, PAGE_SIZE);
            label = query;
        }

        int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        response.setContentType("text/html;charset=UTF-8");
        String context = request.getContextPath();
        StringBuilder html = new StringBuilder(LibraryHtml.start(context, "Search Books"));
        html.append("<section class='collection-hero'><div><p class='eyebrow'>SEARCH RESULTS</p><h1>Matching <em>")
            .append(LibraryHtml.escape(label)).append("</em></h1><p class='section-subtitle'>Search titles, authors, genres, or a book ID.</p></div>")
            .append("<a class='text-action' href='").append(LibraryHtml.escape(context)).append("/viewBooks'>Browse all books</a></section>")
            .append("<div class='catalog-toolbar'><div><strong>").append(total).append(total == 1 ? " result" : " results")
            .append("</strong></div></div>");
        if (books.isEmpty()) {
            html.append("<div class='empty-state'><span>Search</span><h2>No books found</h2><p>Try another title, author, genre, or ID.</p>")
                .append("<a class='btn btn-primary' href='").append(LibraryHtml.escape(context)).append("/viewBooks'>Browse the collection</a></div>");
        } else {
            html.append(LibraryHtml.bookGrid(books, context));
            if (pages > 1) {
                html.append("<nav class='pagination' aria-label='Search result pages'>");
                for (int i = 1; i <= pages; i++) {
                    html.append("<a class='").append(i == page ? "active" : "").append("' href='")
                        .append(LibraryHtml.escape(context)).append("/searchBook?q=")
                        .append(URLEncoder.encode(label, StandardCharsets.UTF_8)).append("&page=").append(i)
                        .append("'>").append(i).append("</a>");
                }
                html.append("</nav>");
            }
        }
        html.append(LibraryHtml.end(context));
        response.getWriter().write(html.toString());
    }

    private int parsePage(String value) {
        try { return Math.max(1, Integer.parseInt(value)); }
        catch (RuntimeException ex) { return 1; }
    }
}
