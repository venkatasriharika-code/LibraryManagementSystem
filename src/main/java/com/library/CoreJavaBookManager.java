package com.library;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Standalone Core Java demonstration for the Library Book Management assignment.
 * It keeps its sample books in memory and demonstrates search and issue rules
 * independently of the Servlet/JDBC application.
 */
public class CoreJavaBookManager {
    private final List<Book> books = new ArrayList<>();

    public CoreJavaBookManager() {
        addSampleBook(1, "The Hobbit", "J. R. R. Tolkien", 3);
        addSampleBook(2, "Pride and Prejudice", "Jane Austen", 2);
        addSampleBook(3, "1984", "George Orwell", 0);
        addSampleBook(4, "The Great Gatsby", "F. Scott Fitzgerald", 4);
        addSampleBook(5, "To Kill a Mockingbird", "Harper Lee", 2);
        addSampleBook(6, "Dune", "Frank Herbert", 1);
        addSampleBook(7, "The Alchemist", "Paulo Coelho", 5);
        addSampleBook(8, "Clean Code", "Robert C. Martin", 3);
        addSampleBook(9, "A Brief History of Time", "Stephen Hawking", 2);
        addSampleBook(10, "The Little Prince", "Antoine de Saint-Exupery", 1);
    }

    private void addSampleBook(int id, String title, String author, int copies) {
        books.add(new Book(id, title, author, copies));
    }

    public Book searchBook(int bookId) {
        for (Book book : books) {
            if (book.getBookId() == bookId) return book;
        }
        return null;
    }

    public boolean issueBook(int bookId) {
        Book book = searchBook(bookId);
        if (book == null || book.getAvailableCopies() < 1) return false;
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return true;
    }

    public Book addBook(String title, String author, int copies) {
        int nextId = 1;
        for (Book book : books) nextId = Math.max(nextId, book.getBookId() + 1);
        Book book = new Book(nextId, title.trim(), author.trim(), copies);
        books.add(book);
        return book;
    }

    private void printBooks() {
        System.out.println("\nID | Title | Author | Available copies");
        for (Book book : books) {
            System.out.printf("%d | %s | %s | %d%n", book.getBookId(), book.getTitle(),
                    book.getAuthor(), book.getAvailableCopies());
        }
    }

    public void run() {
        try (Scanner input = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                System.out.println("\nLibrary Book Management");
                System.out.println("1. Display books");
                System.out.println("2. Search by book ID");
                System.out.println("3. Issue a book");
                System.out.println("4. Add a book");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                String choice = input.nextLine().trim();
                switch (choice) {
                    case "1":
                        printBooks();
                        break;
                    case "2":
                        System.out.print("Enter book ID: ");
                        printSearch(input.nextLine());
                        break;
                    case "3":
                        System.out.print("Enter book ID to issue: ");
                        issueFromInput(input.nextLine());
                        break;
                    case "4":
                        addFromInput(input);
                        break;
                    case "0":
                        running = false;
                        System.out.println("Program ended.");
                        break;
                    default:
                        System.out.println("Invalid option. Choose 0 through 4.");
                }
            }
        }
    }

    private void printSearch(String idText) {
        try {
            Book book = searchBook(Integer.parseInt(idText.trim()));
            if (book == null) {
                System.out.println("Book not found.");
            } else {
                System.out.printf("Found: %s by %s; %d copies available.%n",
                        book.getTitle(), book.getAuthor(), book.getAvailableCopies());
            }
        } catch (NumberFormatException ex) {
            System.out.println("Enter a valid numeric book ID.");
        }
    }

    private void issueFromInput(String idText) {
        try {
            int id = Integer.parseInt(idText.trim());
            Book book = searchBook(id);
            if (book == null) System.out.println("Book not found.");
            else if (issueBook(id)) System.out.println("Issued: " + book.getTitle()
                    + ". Copies remaining: " + book.getAvailableCopies());
            else System.out.println("Cannot issue " + book.getTitle() + ": no copies are available.");
        } catch (NumberFormatException ex) {
            System.out.println("Enter a valid numeric book ID.");
        }
    }

    private void addFromInput(Scanner input) {
        System.out.print("Title: ");
        String title = input.nextLine().trim();
        System.out.print("Author: ");
        String author = input.nextLine().trim();
        System.out.print("Available copies: ");
        String copiesText = input.nextLine().trim();
        try {
            int copies = Integer.parseInt(copiesText);
            if (title.isEmpty() || author.isEmpty() || copies < 0) {
                System.out.println("Enter a title, author, and a non-negative copy count.");
                return;
            }
            Book book = addBook(title, author, copies);
            System.out.println("Added book with ID " + book.getBookId() + ".");
        } catch (NumberFormatException ex) {
            System.out.println("Available copies must be a non-negative whole number.");
        }
    }

    public static void main(String[] args) {
        new CoreJavaBookManager().run();
    }
}