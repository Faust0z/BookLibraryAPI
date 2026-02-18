package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.AdminBookDTO;
import com.faust0z.BookLibraryAPI.dto.BookDTO;
import com.faust0z.BookLibraryAPI.dto.CreateBookDTO;
import com.faust0z.BookLibraryAPI.dto.UpdateBookDTO;
import com.faust0z.BookLibraryAPI.entity.BookEntity;
import com.faust0z.BookLibraryAPI.exception.ResourceNotFoundException;
import com.faust0z.BookLibraryAPI.mapper.BookMapper;
import com.faust0z.BookLibraryAPI.repository.BookRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Cacheable(value = "books", key = "'list:all'")
    public List<BookDTO> getAllBooks() {
        return bookMapper.toDtoList(bookRepository.findAll());
    }

    @Cacheable(value = "books", key = "'details:' + #bookId")
    public BookDTO getBookbyId(UUID bookId) {
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        return bookMapper.toDto(book);
    }


    @CacheEvict(value = "books", key = "'list:all'")
    @Transactional
    public AdminBookDTO createBook(CreateBookDTO dto) {
        BookEntity book = bookMapper.toEntity(dto);

        BookEntity savedBook = bookRepository.save(book);
        return bookMapper.toAdminDto(savedBook);
    }

    @Caching(evict = {
            @CacheEvict(value = "books", key = "'details:' + #bookId"),
            @CacheEvict(value = "books", key = "'list:all'")
    })
    @Transactional
    public AdminBookDTO updateBook(UUID bookId, UpdateBookDTO dto) {
        BookEntity existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        bookMapper.updateBookFromDto(dto, existingBook);

        BookEntity updatedBook = bookRepository.save(existingBook);
        return bookMapper.toAdminDto(updatedBook);
    }
}