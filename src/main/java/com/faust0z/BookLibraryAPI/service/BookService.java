package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.AdminBookDTO;
import com.faust0z.BookLibraryAPI.dto.BookDTO;
import com.faust0z.BookLibraryAPI.dto.CreateBookDTO;
import com.faust0z.BookLibraryAPI.dto.UpdateBookDTO;
import com.faust0z.BookLibraryAPI.entity.BookEntity;
import com.faust0z.BookLibraryAPI.exception.ResourceNotFoundException;
import com.faust0z.BookLibraryAPI.repository.BookRepository;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    public BookService(BookRepository bookRepository, ModelMapper modelMapper) {
        this.bookRepository = bookRepository;
        this.modelMapper = modelMapper;
    }

    private BookDTO convertToDto(BookEntity book) {
        return modelMapper.map(book, BookDTO.class);
    }

    private AdminBookDTO convertToAdminDto(BookEntity book) {
        return modelMapper.map(book, AdminBookDTO.class);
    }

    @Cacheable(value = "books", key = "'list:all'")
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "books", key = "'details:' + #bookId")
    public BookDTO getBookbyId(UUID bookId) {
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        return convertToDto(book);
    }


    @CacheEvict(value = "books", key = "'list:all'")
    @Transactional
    public AdminBookDTO createBook(CreateBookDTO dto) {
        BookEntity book = modelMapper.map(dto, BookEntity.class);

        BookEntity savedBook = bookRepository.save(book);
        return convertToAdminDto(savedBook);
    }

    @Caching(evict = {
            @CacheEvict(value = "books", key = "'details:' + #bookId"),
            @CacheEvict(value = "books", key = "'list:all'")
    })
    @Transactional
    public AdminBookDTO updateBook(UUID bookId, UpdateBookDTO dto) {
        BookEntity existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        modelMapper.map(dto, existingBook);

        BookEntity updatedBook = bookRepository.save(existingBook);
        return convertToAdminDto(updatedBook);
    }
}