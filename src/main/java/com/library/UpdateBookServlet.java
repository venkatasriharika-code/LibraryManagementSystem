package com.library;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/updateBook")
public class UpdateBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        int bookId;
        int availableCopies;
        try {
            bookId = Integer.parseInt(request.getParameter("bookId"));
            availableCopies = Integer.parseInt(request.getParameter("availableCopies"));
            if (availableCopies < 0) throw new NumberFormatException("negative copies");
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/viewBooks?notice=invalid-input");
            return;
        }

        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String category = request.getParameter("category");
        if (title == null || title.trim().isEmpty() || author == null || author.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/bookDetails?bookId=" + bookId
                    + "&notice=invalid-input");
            return;
        }

        Book book = new Book(bookId, title.trim(), author.trim(), availableCopies);
        book.setCategory(category == null ? "" : category.trim());
        boolean success = new BookDAO().updateBook(book);
        response.sendRedirect(request.getContextPath() + "/bookDetails?bookId=" + bookId
                + "&notice=" + (success ? "updated" : "update-failed"));
    }
}
