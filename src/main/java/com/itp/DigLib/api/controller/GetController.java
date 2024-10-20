package com.itp.DigLib.api.controller;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.itp.DigLib.api.model.Book;
import com.itp.DigLib.api.model.PagedContent;
import com.itp.DigLib.api.service.BookContentService;
import com.itp.DigLib.db.BookRepository;

@RestController
@RequestMapping("/books")
public class GetController {
    private static final Logger logger = LoggerFactory.getLogger(GetController.class);

    @Autowired
    private BookRepository bookRepo;

    @Autowired
    private BookContentService bookContentService;

    @GetMapping
    public @ResponseBody Page<Book> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre
    ) {
        logger.info("Fetching all books with page: {}, size: {}, sortBy: {}, sortDir: {}, title: {}, author: {}, genre: {}",
        page, size, sortBy, sortDir, title, author, genre);

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Book> result;
        if (title != null) {
            result = bookRepo.findByTitleContainingIgnoreCase(title, pageRequest);
        } else if (author != null) {
            result = bookRepo.findByAuthorContainingIgnoreCase(author, pageRequest);
        } else if (genre != null) {
            result = bookRepo.findByGenreContainingIgnoreCase(genre, pageRequest);
        } else {
            result = bookRepo.findAll(pageRequest);
        } 
        logger.info("Fetched {} books", result.getTotalElements());
        return result;
    }

    @GetMapping("/{id}")
    public @ResponseBody ResponseEntity<Book> getBook(@PathVariable int id) {
        logger.info("Fetching book with ID: {}", id);
        return bookRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/content")
    public @ResponseBody ResponseEntity<PagedContent> getBookContent(
            @PathVariable int id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer pageSize
    ) {
        logger.info("Fetching content for book with ID: {}, page: {}, pageSize: {}", id, page, pageSize);
        try {
            Optional<Book> bookOpt = bookRepo.findById(id);
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                PagedContent content = bookContentService.readBookContent(book.getFileName(), page, pageSize);
                return ResponseEntity.ok(content);
            } else {
                logger.error("Book with ID: {} not found", id);
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            logger.error("Failed to read book content for book with ID: {}. Error: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
