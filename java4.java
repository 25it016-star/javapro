package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.repository.BookRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> getAllBooks(String search) {
        if (search == null || search.isBlank()) {
            return repository.findAll();
        }

        return repository
            .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                search, search
            );
    }

    public Book getBook(Long id) {
        return repository.findById(id)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book not found"
                )
            );
    }

    @Transactional
    public Book addBook(Book book) {

        if (repository.existsByIsbnIgnoreCase(
                book.getIsbn().trim())) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A book with this ISBN already exists"
            );
        }

        book.setId(null);
        book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());
        book.setIsbn(book.getIsbn().trim());

        book.setAvailableCopies(book.getTotalCopies());

        return repository.save(book);
    }

    @Transactional
    public Book updateBook(Long id, Book updated) {

        Book existing = getBook(id);

        if (repository.existsByIsbnIgnoreCaseAndIdNot(
                updated.getIsbn().trim(), id)) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A book with this ISBN already exists"
            );
        }

        int borrowedCopies =
            existing.getTotalCopies()
            - existing.getAvailableCopies();

        if (updated.getTotalCopies() < borrowedCopies) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Total copies cannot be less than borrowed copies"
            );
        }

        existing.setTitle(updated.getTitle().trim());
        existing.setAuthor(updated.getAuthor().trim());
        existing.setIsbn(updated.getIsbn().trim());

        existing.setTotalCopies(updated.getTotalCopies());

        existing.setAvailableCopies(
            updated.getTotalCopies() - borrowedCopies
        );

        return repository.save(existing);
    }

    @Transactional
    public void deleteBook(Long id) {

        Book book = getBook(id);

        if (!book.getAvailableCopies()
                .equals(book.getTotalCopies())) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Return all borrowed copies before deleting"
            );
        }

        repository.delete(book);
    }

    @Transactional
    public Book borrowBook(Long id) {

        Book book = getBook(id);

        if (book.getAvailableCopies() <= 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No copies are currently available"
            );
        }

        book.setAvailableCopies(
            book.getAvailableCopies() - 1
        );

        return repository.save(book);
    }

    @Transactional
    public Book returnBook(Long id) {

        Book book = getBook(id);

        if (book.getAvailableCopies()
                >= book.getTotalCopies()) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "All copies are already in the library"
            );
        }

        book.setAvailableCopies(
            book.getAvailableCopies() + 1
        );

        return repository.save(book);
    }
}