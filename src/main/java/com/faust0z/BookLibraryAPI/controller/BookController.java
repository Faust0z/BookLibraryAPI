package com.faust0z.BookLibraryAPI.controller;

import com.faust0z.BookLibraryAPI.dto.AdminBookDTO;
import com.faust0z.BookLibraryAPI.dto.BookDTO;
import com.faust0z.BookLibraryAPI.dto.CreateBookDTO;
import com.faust0z.BookLibraryAPI.dto.UpdateBookDTO;
import com.faust0z.BookLibraryAPI.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/books")
@Tag(name = "Books", description = "Library's books inventory management")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "Get all books")
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Get a single book by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found (Invalid ID)")
    })
    @GetMapping("/{bookId}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable UUID bookId) {
        BookDTO book = bookService.getBookbyId(bookId);
        return ResponseEntity.ok(book);
    }

    @Operation(summary = "Post a new book. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. User does not have ADMIN privileges."),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., future date, negative copies)")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AdminBookDTO> createBook(@Valid @RequestBody CreateBookDTO bookDTO) {
        AdminBookDTO createdBook = bookService.createBook(bookDTO);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    @Operation(summary = "Patch an existing book. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., future date, negative copies)"),
            @ApiResponse(responseCode = "403", description = "Forbidden. User does not have ADMIN privileges."),
            @ApiResponse(responseCode = "404", description = "Book not found (Invalid ID)")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{bookId}")
    public ResponseEntity<AdminBookDTO> updateBook(@Valid @PathVariable("bookId") UUID bookId, @Valid @RequestBody UpdateBookDTO bookDTO) {
        AdminBookDTO updatedBook = bookService.updateBook(bookId, bookDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @Operation(summary = "Export all books to Excel. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Books exported successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. User does not have ADMIN privileges.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportBooks() {
        byte[] excelContent = bookService.exportBooksToExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books_inventory.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }

    @Operation(summary = "Import books from Excel. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Books imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid Excel file or data"),
            @ApiResponse(responseCode = "403", description = "Forbidden. User does not have ADMIN privileges.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importBooks(@RequestParam("file") MultipartFile file) throws IOException {
        int count = bookService.importBooksFromExcel(file.getInputStream());
        return ResponseEntity.ok("Successfully imported " + count + " books.");
    }
}