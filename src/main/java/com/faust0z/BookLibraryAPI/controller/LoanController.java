package com.faust0z.BookLibraryAPI.controller;

import com.faust0z.BookLibraryAPI.dto.CreateLoanDTO;
import com.faust0z.BookLibraryAPI.dto.LoanDTO;
import com.faust0z.BookLibraryAPI.entity.UserEntity;
import com.faust0z.BookLibraryAPI.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/loans")
@Tag(name = "Loans", description = "Library's loans management")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @Operation(summary = "Gets loans (all loans or filter by user ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User's loans found successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    public ResponseEntity<List<LoanDTO>> getLoans(@RequestParam(name = "userId", required = false) UUID userId) {
        if (userId != null) {
            return ResponseEntity.ok(loanService.getLoansByUserId(userId));
        }
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @Operation(summary = "Get a single loan by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan found successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    @GetMapping("/{loanId}")
    public ResponseEntity<LoanDTO> getLoanById(@PathVariable UUID loanId) {
        LoanDTO loan = loanService.getLoanbyId(loanId);
        return ResponseEntity.ok(loan);
    }

    @Operation(
            summary = "Get the loans of the current logged user",
            description = "This endpoint gets the data by using the JWT, so there are no parameters required"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loans found successfully"),
    })
    @GetMapping("/me")
    public ResponseEntity<List<LoanDTO>> getMyLoans(@Parameter(hidden = true) @AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(loanService.getLoansByUserId(currentUser.getId()));
    }

    @Operation(
            summary = "Post a new Loan",
            description = "Each user has a limit of 3 loans by default. Asking for more than 3 will result in an error"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Loan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or Loan limit reached for the user"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    @PostMapping
    public ResponseEntity<LoanDTO> createLoan(@Valid @RequestBody CreateLoanDTO createLoanDTO) {
        LoanDTO createdLoan = loanService.createLoan(createLoanDTO);
        return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
    }

    @Operation(summary = "Return a borrowed Loan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan returned successfully"),
            @ApiResponse(responseCode = "400", description = "The Loan has already been returned"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    @PatchMapping("/{loanId}/return")
    public ResponseEntity<LoanDTO> returnLoan(@PathVariable("loanId") UUID loanId) {
        LoanDTO returnedLoan = loanService.returnLoan(loanId);
        return ResponseEntity.ok(returnedLoan);
    }
}