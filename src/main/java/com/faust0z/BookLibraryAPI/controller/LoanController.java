package com.faust0z.BookLibraryAPI.controller;

import com.faust0z.BookLibraryAPI.dto.CreateLoanDTO;
import com.faust0z.BookLibraryAPI.dto.LoanDTO;
import com.faust0z.BookLibraryAPI.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<LoanDTO> createLoan(@Valid @RequestBody CreateLoanDTO createLoanDTO) {
        LoanDTO createdLoan = loanService.createLoan(createLoanDTO);
        return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<LoanDTO> returnLoan(@PathVariable("id") UUID loanId) {
        LoanDTO returnedLoan = loanService.returnLoan(loanId);
        return ResponseEntity.ok(returnedLoan);
    }

    @GetMapping
    public ResponseEntity<List<LoanDTO>> getLoans(@RequestParam(name = "userId", required = false) UUID userId) {
        // This handles both GET /loans and GET /loans?userId=...
        List<LoanDTO> loans = loanService.getLoans(userId);
        return ResponseEntity.ok(loans);
    }
}