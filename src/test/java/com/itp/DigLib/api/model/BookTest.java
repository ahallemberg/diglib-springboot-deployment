package com.itp.DigLib.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BookTest {
    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book();
    }

    @Test
    void testValidBookCreation() {
        book.setTitle("The Great Gatsby");
        book.setAuthor("F. Scott Fitzgerald");
        book.setYear(1925);
        book.setGenre("Fiction");
        book.setIsbn("1234567890123");

        assertEquals("The Great Gatsby", book.getTitle());
        assertEquals("F. Scott Fitzgerald", book.getAuthor());
        assertEquals(1925, book.getYear());
        assertEquals("Fiction", book.getGenre());
        assertEquals("1234567890123", book.getIsbn());
        assertEquals("0_TheGreatGatsby.txt", book.getFileName());
    }

    @Test
    void testInvalidYear() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            book.setYear(2026);
        });
        assertTrue(exception.getMessage().contains("Year must be between 0 and 2025"));

        exception = assertThrows(IllegalArgumentException.class, () -> {
            book.setYear(-1);
        });
        assertTrue(exception.getMessage().contains("Year must be between 0 and 2025"));
    }

    @Test
    void testInvalidIsbn() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            book.setIsbn("123"); // Too short
        });
        assertTrue(exception.getMessage().contains("ISBN must be 13 digits"));

        exception = assertThrows(IllegalArgumentException.class, () -> {
            book.setIsbn("123abc4567890"); // Contains letters
        });
        assertTrue(exception.getMessage().contains("ISBN must be 13 digits"));
    }

    @Test
    void testEmptyTitle() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            book.setTitle("");
        });
        assertTrue(exception.getMessage().contains("Title cannot be empty"));

        exception = assertThrows(IllegalArgumentException.class, () -> {
            book.setTitle(null);
        });
        assertTrue(exception.getMessage().contains("Title cannot be empty"));
    }

    @Test
    void testEmptyAuthor() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            book.setAuthor("");
        });
        assertTrue(exception.getMessage().contains("Author cannot be empty"));

        exception = assertThrows(IllegalArgumentException.class, () -> {
            book.setAuthor(null);
        });
        assertTrue(exception.getMessage().contains("Author cannot be empty"));
    }

    @Test
    void testToCamelCase() {
        assertEquals("TheGreatGatsby", Book.toCamelCase("the great gatsby"));
        assertEquals("HelloWorld", Book.toCamelCase("hello world"));
        assertEquals("", Book.toCamelCase(""));
        assertNull(Book.toCamelCase(null));
    }

    @Test
    void testGetMetadata() {
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setYear(2020);
        book.setGenre("Test Genre");
        book.setIsbn("1234567890123");

        String expectedMetadata = 
"""
Title: Test Book
Author: Test Author
Genre: Test Genre
ISBN: 1234567890123
Year: 2020""";
        assertEquals(expectedMetadata, book.getMetadata());
    }
}