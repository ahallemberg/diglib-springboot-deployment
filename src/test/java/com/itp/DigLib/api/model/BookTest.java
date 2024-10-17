package com.itp.DigLib.api.model;

import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;


public class BookTest {
    
    private final Book book = new Book("Test Title", "Test Author", "Test Genre", "1894635901825", 2021);
    
    @Test
    public void testParams() {
        Assertions.assertEquals("Test Title", book.getTitle());
        Assertions.assertEquals("Test Author", book.getAuthor());
        Assertions.assertEquals("Test Genre", book.getGenre());
        Assertions.assertEquals("1894635901825", book.getIsbn());
        Assertions.assertEquals(2021, book.getYear());
        Assertions.assertEquals(Book.toCamelCase(book.getTitle()) + ".txt", book.getFileName().toString());
    }

    @Test
    public void testIllegalParams() {
        // Test empty fields
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Book("", "Test Author", "Test Genre", "1894635901825", 2021);
        });
    
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Book("Title", "", "Test Genre", "1894635901825", 2021);
        });
    
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Book("Title", "Test Author", "", "1894635901825", 2021);
        });
    
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Book("Title", "Test Author", "Test Genre", "", 2021);
        });
    
        // Test invalid ISBN format
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Book("Title", "Test Author", "Test Genre", "123456789", 2021); // too short
        });
    
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Book("Title", "Test Author", "Test Genre", "12345678901234", 2021); // too long
        });
    
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Book("Title", "Test Author", "Test Genre", "12abc67890123", 2021); // non-numeric characters
        });
    
        // Test invalid year
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Book("Title", "Test Author", "Test Genre", "1894635901825", 2030); // year > 2025
        });
    
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Book("Title", "Test Author", "Test Genre", "1894635901825", -4); // year < 0
        });
    }

    @Test 
    public void testMetadata() {
        Book book = new Book("TestTitle", "TestAuthor", "TestGenre", "1894635901825", 2021);
        String metadataFormat ="Title: " + book.getTitle() +
         "\nAuthor: " + book.getAuthor() +
          "\nGenre: " + book.getGenre() +
           "\nISBN: " + book.getIsbn() +
            "\nYear: " + book.getYear();
        assertEquals(metadataFormat, book.getMetadata());
    }

    @Test
    public void testToCamelCase() {
        //invalid input
        String nullString = null;
        String emptyString = "";
        assertEquals(nullString, Book.toCamelCase(nullString));
        assertEquals(emptyString, Book.toCamelCase(emptyString));

        assertEquals("TestTitle", Book.toCamelCase("Test title"));
        assertEquals("TestTitle", Book.toCamelCase("test Title"));

        
    }

    @Test
    public void testGetFilePath() {
        Book book = new Book("Test Title", "TestAuthor", "TestGenre", "1894635901825", 2021);
        assertEquals(Paths.get("books", "TestTitle.txt").toString(), book.getFilePath("books"));
    }
}
