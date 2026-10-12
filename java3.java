package com.example.library.repository;

import com.example.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository
        extends JpaRepository<Book, Long> {

    List<Book>
    findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
        String title,
        String author
    );

    boolean existsByIsbnIgnoreCase(String isbn);

    boolean existsByIsbnIgnoreCaseAndIdNot(
        String isbn,
        Long id
    );
}