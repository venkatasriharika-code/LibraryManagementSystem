package com.library;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/bookDetails")
public class BookDetailsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final int bookId;
        try {
            bookId = Integer.parseInt(request.getParameter("bookId"));
        } catch (RuntimeException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A valid book ID is required.");
            return;
        }

        Book book = new BookDAO().searchBook(bookId);
        if (book == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found.");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        String context = request.getContextPath();
        String title = LibraryHtml.escape(book.getTitle());
        String author = LibraryHtml.escape(book.getAuthor());
        String category = LibraryHtml.escape(book.getCategory());
        StringBuilder page = new StringBuilder(LibraryHtml.start(context, title));
        page.append("<a class='back-link' href='").append(LibraryHtml.escape(context)).append("/viewBooks'>&larr; My Books</a>")
            .append("<section class='book-detail'>");
        if (book.hasCover()) {
            page.append("<img class='detail-cover' src='").append(LibraryHtml.escape(context))
                .append("/bookCover?bookId=").append(book.getBookId()).append("' alt='Cover of ").append(title).append("'>");
        } else {
            page.append("<div class='detail-cover no-cover'><span>No cover image yet</span></div>");
        }
        page.append("<div class='detail-copy'><p class='eyebrow'>MY BOOKS | BOOK #").append(book.getBookId())
            .append("</p><h1>").append(title).append("</h1><p class='detail-author'>").append(author).append("</p>");
        if (!category.isEmpty()) page.append("<span class='genre-chip detail-genre'>").append(category).append("</span>");
        page.append("<div class='detail-facts'>");
        page.append("<div><span>Available copies</span><strong>")
            .append(book.getAvailableCopies()).append("</strong></div><div><span>Status</span><strong>")
            .append(book.getAvailableCopies() > 0 ? "Available" : "Checked out")
            .append("</strong></div></div><div class='detail-actions'>");
        if (book.getAvailableCopies() > 0) {
            page.append(actionForm(context, "issueBook", book.getBookId(), "Issue a copy", "btn btn-primary"));
        }
        page.append(actionForm(context, "returnBook", book.getBookId(), "Return a copy", "btn btn-secondary"))
            .append("</div>");
        if (!book.hasCover()) {
            page.append("<form class='online-cover-form' method='post' action='").append(LibraryHtml.escape(context))
                .append("/findBookCover'><input type='hidden' name='bookId' value='").append(book.getBookId())
                .append("'><button class='btn btn-primary' type='submit'>Find real cover online</button>")
                .append("<small>Looks up the title and author, then saves the image in MySQL.</small></form>");
        }
        page.append("<details class='edit-details'><summary>Edit book details</summary>")
            .append("<form class='edit-book-form' method='post' action='").append(LibraryHtml.escape(context)).append("/updateBook'>")
            .append("<input type='hidden' name='bookId' value='").append(book.getBookId()).append("'>")
            .append("<label>Title<input name='title' value='").append(title).append("' required></label>")
            .append("<label>Author<input name='author' value='").append(author).append("' required></label>")
            .append("<label>Genre / category<input name='category' value='").append(category).append("' list='book-genres'></label>")
            .append("<datalist id='book-genres'><option>Fiction</option><option>Fantasy</option><option>Mystery</option><option>Romance</option><option>Science Fiction</option><option>History</option><option>Biography</option><option>Science</option><option>Technology</option><option>Computer Science</option><option>Mathematics</option><option>Children's</option></datalist>")
            .append("<label>Available copies<input type='number' name='availableCopies' min='0' value='").append(book.getAvailableCopies()).append("' required></label>")
            .append("<button class='btn btn-primary' type='submit'>Save changes</button></form></details>")
            .append("<form class='detail-delete' method='post' action='").append(LibraryHtml.escape(context))
            .append("/deleteBook' data-confirm-delete><input type='hidden' name='bookId' value='")
            .append(book.getBookId()).append("'><button type='submit' class='delete-button'>Delete this book</button></form></div></section>")
            .append(LibraryHtml.end(context));
        response.getWriter().write(page.toString());
    }

    private String actionForm(String context, String endpoint, int bookId, String label, String cssClass) {
        return "<form method='post' action='" + LibraryHtml.escape(context) + "/" + endpoint
            + "'><input type='hidden' name='bookId' value='" + bookId + "'><button class='" + cssClass
            + "' type='submit'>" + LibraryHtml.escape(label) + "</button></form>";
    }
}
