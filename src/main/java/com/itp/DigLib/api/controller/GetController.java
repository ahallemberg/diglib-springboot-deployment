package com.itp.DigLib.api.controller;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itp.DigLib.api.model.Book;
import com.itp.DigLib.api.model.PagedContent;
import com.itp.DigLib.api.service.BookContentService;
import com.itp.DigLib.db.BookRepository;

@Controller
@RequestMapping("/books")
public class GetController {

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
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        if (title != null) {
            return bookRepo.findByTitleContainingIgnoreCase(title, pageRequest);
        } else if (author != null) {
            return bookRepo.findByAuthorContainingIgnoreCase(author, pageRequest);
        } else if (genre != null) {
            return bookRepo.findByGenreContainingIgnoreCase(genre, pageRequest);
        } else {
            return bookRepo.findAll(pageRequest);
        }
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
}
