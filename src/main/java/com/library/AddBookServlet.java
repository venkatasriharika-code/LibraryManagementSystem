package com.library;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/addBook")
public class AddBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String title = trim(request.getParameter("title"));
        String author = trim(request.getParameter("author"));
        String copiesText = trim(request.getParameter("availableCopies"));

        if (title.isEmpty() || author.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/index.html?notice=invalid-input#add-book");
            return;
        }

        final int availableCopies;
        try {
            availableCopies = Integer.parseInt(copiesText);
            if (availableCopies < 0) throw new NumberFormatException("negative");
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/index.html?notice=invalid-input#add-book");
            return;
        }

        Book book = new Book(0, title, author, availableCopies);
        book.setCategory(trim(request.getParameter("category")));
        try {
            BookCoverLookup.Cover cover = BookCoverLookup.find(title, author);
            if (cover == null) {
                response.sendRedirect(request.getContextPath() + "/index.html?notice=cover-not-found#add-book");
                return;
            }
            book.setCoverImage(cover.bytes);
            book.setCoverMimeType(cover.mimeType);
        } catch (IOException ex) {
            response.sendRedirect(request.getContextPath() + "/index.html?notice=cover-not-found#add-book");
            return;
        }
        boolean success = new BookDAO().addBook(book);
        if (!success) {
            response.sendRedirect(request.getContextPath() + "/index.html?notice=add-failed#add-book");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/index.html?added=1");
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

}
