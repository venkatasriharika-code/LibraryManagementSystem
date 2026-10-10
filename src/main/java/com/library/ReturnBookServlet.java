package com.library;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/returnBook")
public class ReturnBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int bookId = Integer.parseInt(request.getParameter("bookId"));
            boolean success = new BookDAO().returnBook(bookId);
            response.sendRedirect(request.getContextPath() + "/bookDetails?bookId=" + bookId
                    + "&notice=" + (success ? "returned" : "return-failed"));
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/viewBooks?notice=invalid-id");
        }
    }
}
