-- Library Book Management assignment queries
-- Database sample data is already present in library_db (the running catalog shows 105 books).
USE library_db;

-- Display sample data and the total number of books.
SELECT COUNT(*) AS total_books FROM books;
SELECT book_id, title, author, available_copies FROM books ORDER BY book_id LIMIT 10;

-- Add a book. Bind all values with PreparedStatement in the Java application.
-- The application only inserts books after it has found and stored a real cover image.
INSERT INTO books (title, author, category, available_copies, cover_image, cover_mime_type)
VALUES (?, ?, ?, ?, ?, ?);

-- Search by ID.
SELECT book_id, title, author, available_copies
FROM books
WHERE book_id = ?;

-- Update a book.
UPDATE books
SET title = ?, author = ?, available_copies = ?
WHERE book_id = ?;

-- Delete a book.
DELETE FROM books
WHERE book_id = ?;

-- Display books with no available copies.
SELECT book_id, title, author, available_copies
FROM books
WHERE available_copies = 0
ORDER BY title;