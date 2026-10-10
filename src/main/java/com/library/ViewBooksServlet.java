package com.library;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/viewBooks")
public class ViewBooksServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 48;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        BookDAO dao = new BookDAO();
        String category = clean(request.getParameter("category"));
        String sort = clean(request.getParameter("sort"));
        if (!("author".equals(sort) || "newest".equals(sort))) sort = "title";
        int total = dao.countBooks(category);
        int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        int page = parsePage(request.getParameter("page"), pages);
        List<Book> books = dao.getBooksPage((page - 1) * PAGE_SIZE, PAGE_SIZE, category, sort);
        List<String> categories = dao.getCategories();

        response.setContentType("text/html;charset=UTF-8");
        String context = request.getContextPath();
        StringBuilder html = new StringBuilder(LibraryHtml.start(context, "My Books"));
        html.append("<section class='collection-hero collection-hero-compact'><div><p class='eyebrow'>THE LIBRARY</p>")
            .append("<h1>My Books <span class='book-total-badge'>").append(total).append("</span></h1>")
            .append("<p class='section-subtitle'>Your collection, arranged for easy browsing.</p></div>")
            .append("<a class='btn btn-primary' href='").append(LibraryHtml.escape(context))
            .append("/index.html#add-book'>Add a book</a></section>");

        html.append("<div class='catalog-controls'><form class='genre-filter' action='")
            .append(LibraryHtml.escape(context)).append("/viewBooks' method='get'>")
            .append("<label for='genreFilter'>Genre</label><select id='genreFilter' name='category' onchange='this.form.submit()'>")
            .append("<option value=''").append(category.isEmpty() ? " selected" : "").append(">All genres</option>");
        for (String value : categories) {
            html.append("<option value='").append(LibraryHtml.escape(value)).append("'")
                .append(value.equalsIgnoreCase(category) ? " selected" : "").append(">")
                .append(LibraryHtml.escape(value)).append("</option>");
        }
        html.append("</select><input type='hidden' name='sort' value='").append(LibraryHtml.escape(sort)).append("'></form>")
            .append("<div class='sort-options'><span>Sort by</span>");
        appendSort(html, context, category, sort, "title", "Title");
        appendSort(html, context, category, sort, "author", "Author");
        appendSort(html, context, category, sort, "newest", "Recently added");
        html.append("</div></div>");
        if (books.isEmpty()) {
            html.append("<div class='empty-state'><span>Library</span><h2>No books in this genre yet</h2>")
                .append("<p>Choose another genre or add a title to your collection.</p><a class='btn btn-primary' href='")
                .append(LibraryHtml.escape(context)).append("/index.html#add-book'>Add a book</a></div>");
        } else {
            int first = (page - 1) * PAGE_SIZE + 1;
            int last = Math.min(page * PAGE_SIZE, total);
            html.append("<div class='result-caption'>Showing ").append(first).append("&ndash;").append(last)
                .append(" of ").append(total).append(" books</div>");
            if ("title".equals(sort)) appendAlphabeticBooks(html, books, context);
            else html.append(LibraryHtml.bookGrid(books, context));
            if (pages > 1) appendPagination(html, context, page, pages, category, sort);
        }
        html.append(LibraryHtml.end(context));
        response.getWriter().write(html.toString());
    }

    private static void appendAlphabeticBooks(StringBuilder html, List<Book> books, String context) {
        java.util.Map<Character, List<Book>> groups = new java.util.TreeMap<>();
        for (Book book : books) {
            String title = book.getTitle() == null ? "" : book.getTitle().trim();
            String indexTitle = title.replaceFirst("(?i)^(the|a|an)\\s+", "");
            char letter = indexTitle.isEmpty() ? '#' : Character.isLetter(indexTitle.charAt(0)) ? Character.toUpperCase(indexTitle.charAt(0)) : '#';
            groups.computeIfAbsent(letter, key -> new java.util.ArrayList<>()).add(book);
        }
        List<Book> symbols = groups.remove('#');
        if (symbols != null) {
            java.util.Map<Character, List<Book>> ordered = new java.util.LinkedHashMap<>(groups);
            ordered.put('#', symbols);
            groups = ordered;
        }
        html.append("<nav class='alphabet-index' aria-label='Jump to title letter'>");
        for (Character letter : groups.keySet()) {
            html.append("<a href='#letter-").append(letter).append("'>").append(letter).append("</a>");
        }
        html.append("<a class='alphabet-all' href='#book-list'>All</a></nav><div id='book-list'>");
        for (java.util.Map.Entry<Character, List<Book>> group : groups.entrySet()) {
            html.append("<section class='alphabet-section' id='letter-").append(group.getKey()).append("'>")
                .append("<h2>").append(group.getKey()).append("</h2>")
                .append(LibraryHtml.bookGrid(group.getValue(), context)).append("</section>");
        }
        html.append("</div>");
    }
    private static void appendSort(StringBuilder html, String context, String category,
                                   String current, String value, String label) {
        html.append("<a class='sort-link ").append(current.equals(value) ? "active" : "")
            .append("' href='").append(listUrl(context, category, value, 1)).append("'>")
            .append(label).append("</a>");
    }

    private static void appendPagination(StringBuilder html, String context, int page, int pages,
                                         String category, String sort) {
        html.append("<nav class='pagination' aria-label='Book pages'>");
        if (page > 1) html.append("<a href='").append(listUrl(context, category, sort, page - 1)).append("' aria-label='Previous page'>&larr;</a>");
        int start = Math.max(1, page - 2), end = Math.min(pages, page + 2);
        for (int i = start; i <= end; i++) {
            html.append("<a class='").append(i == page ? "active" : "").append("' href='")
                .append(listUrl(context, category, sort, i)).append("'>").append(i).append("</a>");
        }
        if (page < pages) html.append("<a href='").append(listUrl(context, category, sort, page + 1)).append("' aria-label='Next page'>&rarr;</a>");
        html.append("</nav>");
    }

    private static String listUrl(String context, String category, String sort, int page) {
        return LibraryHtml.escape(context) + "/viewBooks?page=" + page + "&category="
                + encode(category) + "&sort=" + encode(sort);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static int parsePage(String value, int pages) {
        try { return Math.max(1, Math.min(pages, Integer.parseInt(value))); }
        catch (RuntimeException ex) { return 1; }
    }

    private static String clean(String value) { return value == null ? "" : value.trim(); }
}
