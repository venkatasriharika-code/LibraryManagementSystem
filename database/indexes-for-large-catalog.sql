-- Optional performance indexes for larger LibraryHub catalogs.
-- Run this once in the MySQL database that contains the `books` table.
-- If an index with one of these names already exists, skip that CREATE INDEX line.

CREATE INDEX idx_books_category_title ON books (category, title);
CREATE INDEX idx_books_category_author_title ON books (category, author, title);
CREATE INDEX idx_books_title ON books (title);
CREATE INDEX idx_books_author_title ON books (author, title);