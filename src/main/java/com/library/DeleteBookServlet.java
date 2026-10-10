package com.library;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/deleteBook")
public class DeleteBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int bookId = Integer.parseInt(request.getParameter("bookId"));
            boolean success = new BookDAO().deleteBook(bookId);
            response.sendRedirect(request.getContextPath() + "/viewBooks?notice="
                    + (success ? "deleted" : "delete-failed"));
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/viewBooks?notice=invalid-id");
        }
    }
}
