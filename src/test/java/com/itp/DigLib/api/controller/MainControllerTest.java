package com.itp.DigLib.api.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.itp.DigLib.api.model.Book;
import com.itp.DigLib.api.model.PagedContent;
import com.itp.DigLib.api.service.BookContentService;
import com.itp.DigLib.db.BookRepository;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Optional;

public class MainControllerTest {

    @Mock
    private BookRepository bookRepo;

    @Mock
    private BookContentService bookContentService;

    @InjectMocks
    private MainController mainController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddNewBook() throws Exception {
        Book book = new Book();
        book.setTitle("Test Book");
        when(bookRepo.save(any(Book.class))).thenReturn(book);

        MockMultipartFile content = new MockMultipartFile(
            "content",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );

        ResponseEntity<String> response = mainController.addNewBook(
            "Test Book",
            "Test Author",
            "Fiction",
            "1234567890123",
            2020,
            content
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Book added successfully", response.getBody());
        verify(bookRepo).save(any(Book.class));
        verify(bookContentService).storeFile(any(), anyString());
    }

    @Test
    void testGetAllBooks() {
        Book book1 = new Book();
        book1.setTitle("Book 1");
        Book book2 = new Book();
        book2.setTitle("Book 2");

        Page<Book> page = new PageImpl<>(Arrays.asList(book1, book2));
        when(bookRepo.findAll(any(PageRequest.class))).thenReturn(page);

        Page<Book> result = mainController.getAllBooks(0, 10, "title", "asc", null, null, null);

        assertEquals(2, result.getContent().size());
        verify(bookRepo).findAll(any(PageRequest.class));
    }

    @Test
    void testGetBook() {
        Book book = new Book();
        book.setTitle("Test Book");
        when(bookRepo.findById(1)).thenReturn(Optional.of(book));

        ResponseEntity<Book> response = mainController.getBook(1);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Test Book", response.getBody().getTitle());
    }

    @Test
    void testGetBookContent() throws Exception {
        Book book = new Book();
        book.setTitle("Test Book");
        when(bookRepo.findById(1)).thenReturn(Optional.of(book));

        PagedContent pagedContent = new PagedContent("test content", 0, 1, 12, 12);
        when(bookContentService.readBookContent(anyString(), anyInt(), any())).thenReturn(pagedContent);

        ResponseEntity<PagedContent> response = mainController.getBookContent(1, 0, null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("test content", response.getBody().getContent());
    }

    @Test
    void testDeleteBook() {
        Book book = new Book();
        book.setTitle("Test Book");
        when(bookRepo.findById(1)).thenReturn(Optional.of(book));

        ResponseEntity<String> response = mainController.deleteBook(1);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Book deleted successfully", response.getBody());
        verify(bookRepo).deleteById(1);
        verify(bookContentService).deleteBookContent(anyString());
    }
}