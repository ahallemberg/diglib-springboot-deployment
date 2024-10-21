package com.itp.DigLib.api.controller;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itp.DigLib.api.model.Book;
import com.itp.DigLib.api.service.BookContentService;
import com.itp.DigLib.db.BookRepository;


@RestController
@RequestMapping("/books")
public class SetController {
    private static final Logger logger = LoggerFactory.getLogger(SetController.class);

    @Autowired
    private BookRepository bookRepo;

    @Autowired
    private BookContentService bookContentService;

    @PostMapping
    public @ResponseBody ResponseEntity<String> addNewBook(
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String genre,
            @RequestParam String isbn,
            @RequestParam int year,
            @RequestParam MultipartFile content
    ) {
        try {
            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            book.setGenre(genre);
            book.setIsbn(isbn);
            book.setYear(year);
            
            book = bookRepo.save(book);
            bookContentService.storeFile(content, book.getFileName());

            
            logger.info("Added new book: {}", title);
            return ResponseEntity.ok("ID:" + book.getID());
        } catch (IOException e) {
            logger.error("Failed to store book content for book: {}. Error: {}", title, e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to store book content: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Illegal values provided for book: {}. Error: {}", title, e.getMessage());
            logger.error("values provided for: Title: {}. author: {}. genre: {}. isbn {}. year{} ", title, author, genre, isbn, year);
            return ResponseEntity.badRequest().body("Illegal values " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public @ResponseBody ResponseEntity<String> deleteBook(@PathVariable int id) {
        Optional<Book> bookOpt = bookRepo.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            bookContentService.deleteBookContent(book.getFileName());
            bookRepo.deleteById(id);
            logger.info("Deleted book with ID: {}", id);
            return ResponseEntity.ok("Book deleted successfully");
        } else{
            logger.error("Book with ID: {} not found", id);
            return ResponseEntity.notFound().build();
        }
        
    }
}
