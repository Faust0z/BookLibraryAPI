package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.CreateLoanDTO;
import com.faust0z.BookLibraryAPI.dto.LoanDTO;
import com.faust0z.BookLibraryAPI.entity.BookEntity;
import com.faust0z.BookLibraryAPI.entity.LoanEntity;
import com.faust0z.BookLibraryAPI.entity.UserEntity;
import com.faust0z.BookLibraryAPI.exception.LoanLimitExceededException;
import com.faust0z.BookLibraryAPI.exception.ResourceNotFoundException;
import com.faust0z.BookLibraryAPI.exception.ResourceUnavailableException;
import com.faust0z.BookLibraryAPI.mapper.LoanMapper;
import com.faust0z.BookLibraryAPI.repository.BookRepository;
import com.faust0z.BookLibraryAPI.repository.LoanRepository;
import com.faust0z.BookLibraryAPI.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private LoanService loanService;


    @Test
    void createLoan_WhenValid_ShouldSuccessAndDecrementCopies() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        CreateLoanDTO dto = new CreateLoanDTO();
        dto.setUserId(userId);
        dto.setBookId(bookId);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        BookEntity book = new BookEntity();
        book.setId(bookId);
        book.setCopies(5);
        book.setName("Test Book");

        LoanDTO expectedDto = new LoanDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(loanRepository.countByUserIdAndReturnDateIsNull(userId)).thenReturn(0);
        when(loanRepository.save(any(LoanEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanMapper.toDto(any(LoanEntity.class))).thenReturn(expectedDto);

        LoanDTO result = loanService.createLoan(dto);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(book.getCopies()).isEqualTo(4); // Verify logic: 5 - 1
        verify(bookRepository).save(book);
        verify(loanRepository).save(any(LoanEntity.class));
    }

    @Test
    void createLoan_WhenBookHasNoCopies_ShouldThrowException() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        CreateLoanDTO dto = new CreateLoanDTO();
        dto.setUserId(userId);
        dto.setBookId(bookId);

        BookEntity book = new BookEntity();
        book.setCopies(0);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new UserEntity()));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(ResourceUnavailableException.class)
                .hasMessageContaining("Book is unavailable");
    }

    @Test
    void createLoan_WhenUserReachedLimit_ShouldThrowException() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        CreateLoanDTO dto = new CreateLoanDTO();
        dto.setUserId(userId);
        dto.setBookId(bookId);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        BookEntity book = new BookEntity();
        book.setCopies(10);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(loanRepository.countByUserIdAndReturnDateIsNull(userId)).thenReturn(3);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(LoanLimitExceededException.class)
                .hasMessageContaining("maximum loan limit");
    }


    @Test
    void returnLoan_WhenValid_ShouldSetReturnDateAndIncrementCopies() {
        UUID loanId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        BookEntity book = new BookEntity();
        book.setId(bookId);

        LoanEntity loan = new LoanEntity();
        loan.setId(loanId);
        loan.setBook(book);
        loan.setReturnDate(null);

        LoanDTO expectedDto = new LoanDTO();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);
        when(loanMapper.toDto(loan)).thenReturn(expectedDto);

        LoanDTO result = loanService.returnLoan(loanId);

        assertThat(result).isNotNull();
        assertThat(loan.getReturnDate()).isToday();
        verify(bookRepository).incrementCopies(bookId);
    }

    @Test
    void returnLoan_WhenAlreadyReturned_ShouldThrowException() {
        UUID loanId = UUID.randomUUID();
        LoanEntity loan = new LoanEntity();
        loan.setReturnDate(LocalDate.now().minusDays(1));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnLoan(loanId))
                .isInstanceOf(ResourceUnavailableException.class)
                .hasMessageContaining("already been returned");
    }


    @Test
    void getLoanById_WhenNotFound_ShouldThrowException() {
        UUID loanId = UUID.randomUUID();
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.getLoanbyId(loanId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}