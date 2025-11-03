package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.BookDTO;
import com.faust0z.BookLibraryAPI.dto.CreateBookDTO;
import com.faust0z.BookLibraryAPI.dto.UpdateBookDTO;
import com.faust0z.BookLibraryAPI.entity.BookEntity;
import com.faust0z.BookLibraryAPI.exception.ResourceNotFoundException;
import com.faust0z.BookLibraryAPI.repository.BookRepository;
import org.modelmapper.ModelMapper;
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

    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookDTO createBook(CreateBookDTO dto) {
        BookEntity book = modelMapper.map(dto, BookEntity.class);

        BookEntity savedBook = bookRepository.save(book);
        return convertToDto(savedBook);
    }

    @Transactional
    public BookDTO updateBook(UUID id, UpdateBookDTO dto) {
        BookEntity existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        modelMapper.map(dto, existingBook);

        BookEntity updatedBook = bookRepository.save(existingBook);
        return convertToDto(updatedBook);
    }
}