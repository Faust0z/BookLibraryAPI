package com.faust0z.BookLibraryAPI.controller;

import com.faust0z.BookLibraryAPI.dto.CreateLoanDTO;
import com.faust0z.BookLibraryAPI.dto.LoanDTO;
import com.faust0z.BookLibraryAPI.entity.UserEntity;
import com.faust0z.BookLibraryAPI.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public ResponseEntity<List<LoanDTO>> getLoans(@RequestParam(name = "userId", required = false) UUID userId) {
        if (userId != null) {
            return ResponseEntity.ok(loanService.getLoansByUserId(userId));
        }
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/me")
    public ResponseEntity<List<LoanDTO>> getMyLoans(@AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(loanService.getLoansByUserId(currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<LoanDTO> createLoan(@Valid @RequestBody CreateLoanDTO createLoanDTO) {
        LoanDTO createdLoan = loanService.createLoan(createLoanDTO);
        return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
    }

    @PatchMapping("/{loanId}/return")
    public ResponseEntity<LoanDTO> returnLoan(@PathVariable("loanId") UUID loanId) {
        LoanDTO returnedLoan = loanService.returnLoan(loanId);
        return ResponseEntity.ok(returnedLoan);
    }
}