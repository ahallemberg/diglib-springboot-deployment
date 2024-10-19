package com.itp.DigLib.api.controller;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.itp.DigLib.api.model.Book;
import com.itp.DigLib.api.model.PagedContent;
import com.itp.DigLib.api.service.BookContentService;
import com.itp.DigLib.db.BookRepository;

@Controller
@RequestMapping("/api/books")
public class MainController {
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
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        if (title != null) {
            return bookRepo.findByTitleContainingIgnoreCase(title, pageRequest);
        } else if (author != null) {
            return bookRepo.findByAuthorContainingIgnoreCase(author, pageRequest);
        } else if (genre != null) {
            return bookRepo.findByGenreContainingIgnoreCase(genre, pageRequest);
        }
        
        return bookRepo.findAll(pageRequest);
    }

    @GetMapping("/{id}")
    public @ResponseBody ResponseEntity<Book> getBook(@PathVariable int id) {
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
        try {
            Optional<Book> bookOpt = bookRepo.findById(id);
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                PagedContent content = bookContentService.readBookContent(book.getFileName(), page, pageSize);
                return ResponseEntity.ok(content);
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
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