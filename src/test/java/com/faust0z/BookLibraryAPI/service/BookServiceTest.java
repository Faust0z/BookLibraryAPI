package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.AdminBookDTO;
import com.faust0z.BookLibraryAPI.dto.BookDTO;
import com.faust0z.BookLibraryAPI.dto.CreateBookDTO;
import com.faust0z.BookLibraryAPI.dto.UpdateBookDTO;
import com.faust0z.BookLibraryAPI.entity.BookEntity;
import com.faust0z.BookLibraryAPI.exception.ResourceNotFoundException;
import com.faust0z.BookLibraryAPI.mapper.BookMapper;
import com.faust0z.BookLibraryAPI.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    @Test
    void getAllBooks_ShouldReturnListOfDtos() {
        BookEntity entity1 = new BookEntity();
        BookEntity entity2 = new BookEntity();
        List<BookEntity> entityList = List.of(entity1, entity2);

        BookDTO dto1 = new BookDTO();
        BookDTO dto2 = new BookDTO();
        List<BookDTO> dtoList = List.of(dto1, dto2);

        when(bookRepository.findAll()).thenReturn(entityList);
        when(bookMapper.toDtoList(entityList)).thenReturn(dtoList);

        List<BookDTO> result = bookService.getAllBooks();

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactly(dto1, dto2);
    }

    @Test
    void getBookById_WhenBookExists_ShouldReturnDto() {
        UUID bookId = UUID.randomUUID();
        BookEntity entity = new BookEntity();
        entity.setId(bookId);

        BookDTO expectedDto = new BookDTO();
        expectedDto.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(entity));
        when(bookMapper.toDto(entity)).thenReturn(expectedDto);

        BookDTO result = bookService.getBookbyId(bookId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookId);
    }

    @Test
    void getBookById_WhenBookDoesNotExist_ShouldThrowException() {
        when(bookRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookbyId(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    void createBook_ShouldSaveAndReturnAdminDto() {
        CreateBookDTO createDto = new CreateBookDTO();
        createDto.setName("Clean Code");

        BookEntity entity = new BookEntity();
        entity.setName("Clean Code");

        BookEntity savedEntity = new BookEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setName("Clean Code");

        AdminBookDTO adminDto = new AdminBookDTO();
        adminDto.setId(savedEntity.getId());
        adminDto.setName("Clean Code");

        when(bookMapper.toEntity(createDto)).thenReturn(entity);
        when(bookRepository.save(entity)).thenReturn(savedEntity);
        when(bookMapper.toAdminDto(savedEntity)).thenReturn(adminDto);

        AdminBookDTO result = bookService.createBook(createDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedEntity.getId());
        assertThat(result.getName()).isEqualTo("Clean Code");

        verify(bookRepository).save(entity);
    }

    @Test
    void updateBook_WhenBookExists_ShouldUpdateAndReturnAdminDto() {
        UUID bookId = UUID.randomUUID();
        UpdateBookDTO updateDto = new UpdateBookDTO();
        updateDto.setName("Clean Architecture");

        BookEntity entity = new BookEntity();
        entity.setId(bookId);
        entity.setName("Old Title");

        AdminBookDTO expectedDto = new AdminBookDTO();
        expectedDto.setId(bookId);
        expectedDto.setName("Clean Architecture");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(entity));
        when(bookRepository.save(entity)).thenReturn(entity);
        when(bookMapper.toAdminDto(entity)).thenReturn(expectedDto);

        AdminBookDTO result = bookService.updateBook(bookId, updateDto);

        verify(bookMapper).updateBookFromDto(updateDto, entity);

        verify(bookRepository).save(entity);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Clean Architecture");
    }

    @Test
    void updateBook_WhenBookDoesNotExist_ShouldThrowException() {
        when(bookRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(UUID.randomUUID(), new UpdateBookDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}