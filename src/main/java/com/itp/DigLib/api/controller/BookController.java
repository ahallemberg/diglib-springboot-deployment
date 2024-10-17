package com.itp.DigLib.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itp.DigLib.api.model.Book;
import com.itp.DigLib.service.BookService;

@RestController
public class BookController {

    private BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    
    @GetMapping
    public Book getBook(@RequestParam int id) {
        return bookService.getBook(id);
        
        }
}