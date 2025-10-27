package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.BookDTO;
import com.faust0z.BookLibraryAPI.dto.CreateBookDTO;
import com.faust0z.BookLibraryAPI.model.Book;
import com.faust0z.BookLibraryAPI.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Retrieves all books from the database.
     *
     * @return A list of all books as BookDTOs.
     */
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new book and saves it to the database.
     *
     * @param bookDTO The DTO containing the new book's data.
     * @return The created book's data as a DTO.
     */
    public BookDTO createBook(CreateBookDTO bookDTO) {
        Book book = new Book();
        book.setName(bookDTO.getName());
        book.setAuthor(bookDTO.getAuthor());
        book.setPublicationDate(bookDTO.getPublicationDate());
        book.setCopies(bookDTO.getCopies());

        Book savedBook = bookRepository.save(book);
        return convertToDto(savedBook);
    }

    /**
     * Helper method to convert a Book entity to a BookDTO.
     */
    private BookDTO convertToDto(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setName(book.getName());
        dto.setAuthor(book.getAuthor());
        dto.setPublicationDate(book.getPublicationDate());
        dto.setCopies(book.getCopies());
        return dto;
    }
}