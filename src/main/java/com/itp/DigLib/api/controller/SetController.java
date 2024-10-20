package com.itp.DigLib.api.controller;

import java.io.IOException;
import java.util.Optional;

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
            
            return ResponseEntity.ok("Book added successfully");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to store book content: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Illegal values" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public @ResponseBody ResponseEntity<String> deleteBook(@PathVariable int id) {
        Optional<Book> bookOpt = bookRepo.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            bookContentService.deleteBookContent(book.getFileName());
            bookRepo.deleteById(id);
            return ResponseEntity.ok("Book deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
}
