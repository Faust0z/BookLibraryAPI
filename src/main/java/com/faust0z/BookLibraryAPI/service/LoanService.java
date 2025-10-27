package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.CreateLoanDTO;
import com.faust0z.BookLibraryAPI.dto.LoanDTO;
import com.faust0z.BookLibraryAPI.exception.BookUnavailableException;
import com.faust0z.BookLibraryAPI.exception.LoanLimitExceededException;
import com.faust0z.BookLibraryAPI.exception.ResourceNotFoundException;
import com.faust0z.BookLibraryAPI.model.Book;
import com.faust0z.BookLibraryAPI.model.Loan;
import com.faust0z.BookLibraryAPI.model.User;
import com.faust0z.BookLibraryAPI.repository.BookRepository;
import com.faust0z.BookLibraryAPI.repository.LoanRepository;
import com.faust0z.BookLibraryAPI.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private static final int MAX_ACTIVE_LOANS = 3;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new loan, applying all business rules.
     */
    @Transactional // Ensures this is all-or-nothing
    public LoanDTO createLoan(CreateLoanDTO createLoanDTO) {
        // Find the user and book, or throw 404
        User user = userRepository.findById(createLoanDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + createLoanDTO.getUserId()));

        Book book = bookRepository.findById(createLoanDTO.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + createLoanDTO.getBookId()));

        // --- Business Rule #1 ---
        if (book.getCopies() <= 0) {
            throw new BookUnavailableException("Book is unavailable: " + book.getName());
        }

        // --- Business Rule #2 ---
        int activeLoans = loanRepository.countByUserIdAndReturnDateIsNull(user.getId());
        if (activeLoans >= MAX_ACTIVE_LOANS) {
            throw new LoanLimitExceededException("User has reached the maximum loan limit of " + MAX_ACTIVE_LOANS);
        }

        // All rules passed: create the loan
        book.setCopies(book.getCopies() - 1);
        bookRepository.save(book);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBook(book);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusWeeks(2)); // 2-week loan period
        loan.setReturnDate(null);

        Loan savedLoan = loanRepository.save(loan);
        return convertToDto(savedLoan);
    }

    /**
     * Marks an existing loan as returned.
     */
    @Transactional
    public LoanDTO returnLoan(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

        if (loan.getReturnDate() != null) {
            throw new BookUnavailableException("This loan has already been returned on " + loan.getReturnDate());
        }

        // Mark as returned
        loan.setReturnDate(LocalDate.now());
        Loan savedLoan = loanRepository.save(loan);

        // Increment book copies
        Book book = loan.getBook();
        book.setCopies(book.getCopies() + 1);
        bookRepository.save(book);

        return convertToDto(savedLoan);
    }

    /**
     * Gets all loans, optionally filtered by user.
     */
    public List<LoanDTO> getLoans(UUID userId) {
        List<Loan> loans;
        if (userId != null) {
            // Find by user
            loans = loanRepository.findByUserId(userId);
        } else {
            // Get all loans
            loans = loanRepository.findAll();
        }
        return loans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to convert a Loan entity to a LoanDTO.
     */
    private LoanDTO convertToDto(Loan loan) {
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setLoanDate(loan.getLoanDate());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnDate(loan.getReturnDate());
        dto.setUserId(loan.getUser().getId());
        dto.setBookId(loan.getBook().getId());
        dto.setBookName(loan.getBook().getName()); // Add some useful context
        return dto;
    }
}