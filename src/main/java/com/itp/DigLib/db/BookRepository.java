package com.itp.DigLib.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.itp.DigLib.api.model.Book;

public interface BookRepository extends PagingAndSortingRepository<Book, Integer>, CrudRepository<Book, Integer> {
    @SuppressWarnings("null")
    @Override
    Page<Book> findAll(Pageable pageable);
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
    Page<Book> findByGenreContainingIgnoreCase(String genre, Pageable pageable);
}