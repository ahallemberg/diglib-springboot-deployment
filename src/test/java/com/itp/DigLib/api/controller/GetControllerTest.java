package com.itp.DigLib.api.controller;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.itp.DigLib.api.model.Book;
import com.itp.DigLib.api.model.PagedContent;
import com.itp.DigLib.api.service.BookContentService;
import com.itp.DigLib.db.BookRepository;

public class GetControllerTest {

    @Mock
    private BookRepository bookRepo;

    @Mock
    private BookContentService bookContentService;

    @InjectMocks
    private GetController getController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllBooks() {
        Book book1 = new Book();
        book1.setTitle("Book 1");
        Book book2 = new Book();
        book2.setTitle("Book 2");

        Page<Book> page = new PageImpl<>(Arrays.asList(book1, book2));
        when(bookRepo.findAll(any(PageRequest.class))).thenReturn(page);

        Page<Book> result = getController.getAllBooks(0, 10, "title", "asc", null, null, null);

        assertEquals(2, result.getContent().size());
        verify(bookRepo).findAll(any(PageRequest.class));
    }

    @Test
    void testGetBook() {
        Book book = new Book();
        book.setTitle("Test Book");
        when(bookRepo.findById(1)).thenReturn(Optional.of(book));

        ResponseEntity<Book> response = getController.getBook(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Book", response.getBody().getTitle());
    }

    @Test
    void testGetBookContent() throws Exception {
        Book book = new Book();
        book.setTitle("Test Book");
        when(bookRepo.findById(1)).thenReturn(Optional.of(book));

        PagedContent pagedContent = new PagedContent("test content", 0, 1, 12, 12);
        when(bookContentService.readBookContent(anyString(), anyInt(), any())).thenReturn(pagedContent);

        ResponseEntity<PagedContent> response = getController.getBookContent(1, 0, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test content", response.getBody().getContent());
    }
}