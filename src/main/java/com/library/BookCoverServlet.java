package com.library;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/bookCover")
public class BookCoverServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final int bookId;
        try {
            bookId = Integer.parseInt(request.getParameter("bookId"));
        } catch (RuntimeException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Book book = new BookDAO().getBookCover(bookId);
        if (book == null || book.getCoverImage() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String mime = book.getCoverMimeType();
        if (!("image/jpeg".equals(mime) || "image/png".equals(mime) || "image/webp".equals(mime))) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }
        response.setContentType(mime);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLength(book.getCoverImage().length);
        response.getOutputStream().write(book.getCoverImage());
    }
}
