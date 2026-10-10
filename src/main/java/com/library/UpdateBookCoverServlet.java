package com.library;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/updateBookCover")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class UpdateBookCoverServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final int bookId;
        try {
            bookId = Integer.parseInt(request.getParameter("bookId"));
        } catch (RuntimeException ex) {
            response.sendRedirect(request.getContextPath() + "/viewBooks?notice=invalid-id");
            return;
        }

        BookDAO dao = new BookDAO();
        if ("true".equals(request.getParameter("removeCover"))) {
            boolean removed = dao.removeBookCover(bookId);
            response.sendRedirect(request.getContextPath() + "/bookDetails?bookId=" + bookId
                    + "&notice=" + (removed ? "cover-removed" : "cover-failed"));
            return;
        }

        Part part = request.getPart("coverImage");
        if (part == null || part.getSize() == 0 || part.getSize() > MAX_BYTES) {
            response.sendRedirect(request.getContextPath() + "/bookDetails?bookId=" + bookId
                    + "&notice=invalid-cover");
            return;
        }
        byte[] image = part.getInputStream().readAllBytes();
        String mime = detectImageType(image);
        if (mime == null) {
            response.sendRedirect(request.getContextPath() + "/bookDetails?bookId=" + bookId
                    + "&notice=invalid-cover");
            return;
        }

        boolean saved = dao.updateBookCover(bookId, image, mime);
        response.sendRedirect(request.getContextPath() + "/bookDetails?bookId=" + bookId
                + "&notice=" + (saved ? "cover-updated" : "cover-failed"));
    }

    private static String detectImageType(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xff) == 0xff && (data[1] & 0xff) == 0xd8
                && (data[2] & 0xff) == 0xff) return "image/jpeg";
        if (data.length >= 8 && (data[0] & 0xff) == 0x89 && data[1] == 'P' && data[2] == 'N'
                && data[3] == 'G' && data[4] == 0x0d && data[5] == 0x0a
                && data[6] == 0x1a && data[7] == 0x0a) return "image/png";
        if (data.length >= 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F'
                && data[3] == 'F' && data[8] == 'W' && data[9] == 'E'
                && data[10] == 'B' && data[11] == 'P') return "image/webp";
        return null;
    }
}
