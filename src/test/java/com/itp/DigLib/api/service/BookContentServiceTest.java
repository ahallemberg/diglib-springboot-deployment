package com.itp.DigLib.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.itp.DigLib.api.model.PagedContent;

public class BookContentServiceTest {
    
    private BookContentService bookContentService;
    @TempDir
    Path tempDir;
    private static final String TEST_CONTENT = """
                                               First page content.
                                               Second page content.
                                               Third page content.
                                               Fourth page content.
                                               """;

    @BeforeEach
    void setUp() throws IOException {
        bookContentService = new BookContentService(tempDir.toString(), 20); // Set page size to 20 chars
        // Create a test file with known content
        Files.write(tempDir.resolve("test.txt"), TEST_CONTENT.getBytes());
    }

    @Test
    void testStoreFile() throws IOException {
        String newContent = "This is new test content";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "newtest.txt",
            "text/plain", 
            newContent.getBytes()
        );

        String storedPath = bookContentService.storeFile(file, "newtest.txt");
        
        assertTrue(Files.exists(Path.of(storedPath)));
        assertEquals(newContent, Files.readString(Path.of(storedPath)));
    }

    @Test
    void testReadBookContentFirstPage() throws IOException {
        PagedContent pagedContent = bookContentService.readBookContent("test.txt", 0, 20);
        
        assertEquals("First page content.\n", pagedContent.getContent());
        assertEquals(0, pagedContent.getPageNumber());
        assertEquals(20, pagedContent.getPageSize());
        assertEquals(TEST_CONTENT.length(), pagedContent.getTotalSize());
        assertTrue(pagedContent.getTotalPages() > 1);
    }

    @Test
    void testReadBookContentMiddlePage() throws IOException {
        PagedContent pagedContent = bookContentService.readBookContent("test.txt", 1, 20);
        
        assertEquals("Second page content.", pagedContent.getContent());
        assertEquals(1, pagedContent.getPageNumber());
        assertEquals(20, pagedContent.getPageSize());
    }

    @Test
    void testReadBookContentLastPage() throws IOException {
        // Calculate last page number
        int lastPage = (TEST_CONTENT.length() - 1) / 20;
        PagedContent pagedContent = bookContentService.readBookContent("test.txt", lastPage, 20);
        
        assertTrue(pagedContent.getContent().length() <= 20);
        assertEquals(lastPage, pagedContent.getPageNumber());
        assertEquals(TEST_CONTENT.length(), pagedContent.getTotalSize());
    }

    @Test
    void testReadBookContentCustomPageSize() throws IOException {
        PagedContent pagedContent = bookContentService.readBookContent("test.txt", 0, 10);
        
        assertEquals(10, pagedContent.getContent().length());
        assertEquals(0, pagedContent.getPageNumber());
        assertEquals(10, pagedContent.getPageSize());
        assertEquals(TEST_CONTENT.length(), pagedContent.getTotalSize());
    }

    @Test
    void testReadBookContentInvalidPageNumber() {
        assertThrows(IllegalArgumentException.class, () -> {
            bookContentService.readBookContent("test.txt", -1, 20);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            bookContentService.readBookContent("test.txt", 1000, 20);
        });
    }

    @Test
    void testDeleteExistingBookContent() throws IOException {
        assertTrue(bookContentService.deleteBookContent("test.txt"));
        assertFalse(Files.exists(tempDir.resolve("test.txt")));
    }

    @Test
    void testDeleteNonExistentBookContent() {
        assertFalse(bookContentService.deleteBookContent("nonexistent.txt"));
    }

    @Test
    void testReadNonExistentFile() {
        assertThrows(IOException.class, () -> {
            bookContentService.readBookContent("nonexistent.txt", 0, 20);
        });
    }
}