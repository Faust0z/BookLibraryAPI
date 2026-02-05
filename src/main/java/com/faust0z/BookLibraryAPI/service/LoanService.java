package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.AdminLoanDTO;
import com.faust0z.BookLibraryAPI.dto.CreateLoanDTO;
import com.faust0z.BookLibraryAPI.dto.LoanDTO;
import com.faust0z.BookLibraryAPI.entity.BookEntity;
import com.faust0z.BookLibraryAPI.entity.LoanEntity;
import com.faust0z.BookLibraryAPI.entity.UserEntity;
import com.faust0z.BookLibraryAPI.exception.LoanLimitExceededException;
import com.faust0z.BookLibraryAPI.exception.ResourceNotFoundException;
import com.faust0z.BookLibraryAPI.exception.ResourceUnavailableException;
import com.faust0z.BookLibraryAPI.repository.BookRepository;
import com.faust0z.BookLibraryAPI.repository.LoanRepository;
import com.faust0z.BookLibraryAPI.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    private final ModelMapper modelMapper;
    private static final int MAX_ACTIVE_LOANS = 3;
    private static final int LOAN_LIMIT_WEEKS = 2;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    private LoanDTO convertToDto(LoanEntity loan) {
        return modelMapper.map(loan, LoanDTO.class);
    }

    private AdminLoanDTO convertToAdminDto(LoanEntity loan) {
        return modelMapper.map(loan, AdminLoanDTO.class);
    }

    @Cacheable(value = "loans", key = "'list:all'")
    public List<AdminLoanDTO> getAllLoans() {
        List<LoanEntity> loans = loanRepository.findAllWithUserAndBook();

        return loans.stream()
                .map(this::convertToAdminDto)
                .collect(Collectors.toList());

    }

    @Cacheable(value = "loans", key = "'details:' + #loanId")
    public AdminLoanDTO getLoanbyId(UUID loanId) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

        return convertToAdminDto(loan);
    }

    @Cacheable(value = "loans", key = "'details:' + #userId")
    public List<AdminLoanDTO> getLoansByUserId(UUID userId) {
        List<LoanEntity> loans = loanRepository.findByUserIdWithUserAndBook(userId);

        return loans.stream()
                .map(this::convertToAdminDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "user_loans", key = "#userId")
    public List<LoanDTO> getMyLoans(UUID userId) {
        List<LoanEntity> loans = loanRepository.findByUserIdWithUserAndBook(userId);

        return loans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    @Caching(evict = {
            @CacheEvict(value = "loans", key = "'list:all'"),
            @CacheEvict(value = "loans", key = "'details:' + #dto.userId"),
            @CacheEvict(value = "books", key = "'list:all'"),
            @CacheEvict(value = "books", key = "'details:' + #result.bookId")
    })
    @Transactional
    public LoanDTO createLoan(CreateLoanDTO dto) {

        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        BookEntity book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + dto.getBookId()));

        // --- Business Rule #1 ---
        if (book.getCopies() <= 0) {
            throw new ResourceUnavailableException("Book is unavailable: " + book.getName());
        }

        // --- Business Rule #2 ---
        int activeLoans = loanRepository.countByUserIdAndReturnDateIsNull(user.getId());
        if (activeLoans >= MAX_ACTIVE_LOANS) {
            throw new LoanLimitExceededException("User has reached the maximum loan limit of " + MAX_ACTIVE_LOANS);
        }

        book.setCopies(book.getCopies() - 1);
        bookRepository.save(book);

        LoanEntity loan = new LoanEntity();
        loan.setUser(user);
        loan.setBook(book);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusWeeks(LOAN_LIMIT_WEEKS));
        loan.setReturnDate(null);

        LoanEntity savedLoan = loanRepository.save(loan);
        return convertToDto(savedLoan);
    }

    @Caching(evict = {
            @CacheEvict(value = "loans", key = "'details:' + #loanId"),
            @CacheEvict(value = "loans", key = "'list:all'"),
            @CacheEvict(value = "loans", key = "'details:' + #result.userId"),
            @CacheEvict(value = "books", key = "'list:all'"),
            @CacheEvict(value = "books", key = "'details:' + #result.bookId")
    })
    @Transactional
    public LoanDTO returnLoan(UUID loanId) {

        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));
        if (loan.getReturnDate() != null) {
            throw new ResourceUnavailableException("This loan has already been returned on " + loan.getReturnDate());
        }
        loan.setReturnDate(LocalDate.now());
        LoanEntity savedLoan = loanRepository.save(loan);

        bookRepository.incrementCopies(savedLoan.getBook().getId());

        return convertToDto(savedLoan);
    }
}