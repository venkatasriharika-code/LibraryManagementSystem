package com.library;

import java.util.List;

final class LibraryHtml {
    private LibraryHtml() { }

    static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    static String start(String context, String title) {
        String base = escape(context);
        return "<!doctype html><html lang='en'><head><meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
            + "<title>" + escape(title) + " | LibraryHub</title>"
            + "<link rel='preconnect' href='https://fonts.googleapis.com'>"
            + "<link rel='preconnect' href='https://fonts.gstatic.com' crossorigin>"
            + "<link href='https://fonts.googleapis.com/css2?family=Fraunces:wght@500;700;900&family=Manrope:wght@400;500;600;700&display=swap' rel='stylesheet'>"
            + "<link rel='stylesheet' href='" + base + "/style.css?v=20261010b'>"
            + "</head><body class='library-page'><header class='library-topbar'>"
            + "<a class='logo library-logo' href='" + base + "/index.html'><span class='logo-mark'>Books</span> LibraryHub</a>"
            + "<form class='library-search' action='" + base + "/searchBook' method='get'>"
            + "<span aria-hidden='true'>Search</span><input name='q' type='search' placeholder='Search by title, author, or ID' aria-label='Search books' required>"
            + "<button type='submit'>Search</button></form>"
            + "<nav class='library-nav'><a href='" + base + "/viewBooks'>My Books</a>"
            + "<a href='" + base + "/index.html#add-book'>Add a Book</a></nav></header><main class='library-main'>";
    }

    static String end(String context) {
        return "</main><footer class='library-footer'>LibraryHub | Library Management System</footer>"
            + "<div class='toast' id='toast' role='status' aria-live='polite'></div>"
            + "<script src='" + escape(context) + "/main.js' defer></script></body></html>";
    }

    static String bookGrid(List<Book> books, String context) {
        StringBuilder html = new StringBuilder("<div class='book-grid'>");
        for (Book book : books) {
            String title = escape(book.getTitle());
            String author = escape(book.getAuthor());
            String category = escape(book.getCategory());
            html.append("<article class='book-card' data-title='").append(title)
                .append("' data-author='").append(author).append("' data-category='").append(category).append("'>");
            html.append("<a class='book-card-link' href='").append(escape(context))
                .append("/bookDetails?bookId=").append(book.getBookId()).append("'>");
            if (book.hasCover()) {
                html.append("<img class='book-cover' loading='lazy' src='")
                    .append(escape(context)).append("/bookCover?bookId=")
                    .append(book.getBookId()).append("' alt='Cover of ").append(title).append("'>");
            } else {
                html.append("<div class='book-cover book-cover-unavailable' aria-label='No verified cover image'>Cover unavailable</div>");
            }
            html.append("<span class='book-card-title'>").append(title).append("</span>")
                .append("<span class='book-card-author'>").append(author).append("</span>");
            html.append("</a><div class='book-card-meta'><span class='availability ")
                .append(book.getAvailableCopies() > 0 ? "available'>Available" : "unavailable'>Checked out")
                .append("</span><span>").append(book.getAvailableCopies()).append(" copies</span></div>")
                .append("<div class='book-card-actions'><a class='text-action' href='")
                .append(escape(context)).append("/bookDetails?bookId=").append(book.getBookId())
                .append("'>Details</a><form method='post' action='").append(escape(context))
                .append("/deleteBook' data-confirm-delete><input type='hidden' name='bookId' value='")
                .append(book.getBookId()).append("'><button class='text-action delete-action' type='submit'>Delete</button></form></div></article>");
        }
        html.append("</div>");
        return html.toString();
    }
}
