package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.entity.BookEntity;
import com.faust0z.BookLibraryAPI.exception.ExcelProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelServiceTest {

    private ExcelService excelService;

    @BeforeEach
    void setUp() {
        excelService = new ExcelService();
    }

    @Test
    void exportBooksToExcel_ShouldReturnByteArray() {
        BookEntity book = new BookEntity();
        book.setName("Test Book");
        book.setAuthor("Test Author");
        book.setCopies(10);
        book.setPublicationDate(LocalDate.of(2023, 1, 1));

        byte[] result = excelService.exportBooksToExcel(List.of(book));

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void importBooksFromExcel_ShouldReturnListOfBooks() {
        BookEntity book = new BookEntity();
        book.setName("Imported Book");
        book.setAuthor("Imported Author");
        book.setCopies(5);
        book.setPublicationDate(LocalDate.of(2022, 5, 20));

        byte[] excelData = excelService.exportBooksToExcel(List.of(book));
        ByteArrayInputStream is = new ByteArrayInputStream(excelData);

        List<BookEntity> result = excelService.importBooksFromExcel(is);

        assertEquals(1, result.size());
        assertEquals("Imported Book", result.get(0).getName());
        assertEquals("Imported Author", result.get(0).getAuthor());
        assertEquals(5, result.get(0).getCopies());
        assertEquals(LocalDate.of(2022, 5, 20), result.get(0).getPublicationDate());
    }

    @Test
    void importBooksFromExcel_MissingHeader_ShouldThrowException() {
        byte[] excelData = excelService.exportBooksToExcel(List.of());
        ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);

        assertThrows(ExcelProcessingException.class, () -> excelService.importBooksFromExcel(is));
    }

    @Test
    void validateBook_InvalidData_ShouldThrowException() {
        BookEntity invalidBook = new BookEntity();
        invalidBook.setName("");
        invalidBook.setAuthor("Author");

        byte[] excelData = excelService.exportBooksToExcel(List.of(invalidBook));
        ByteArrayInputStream is = new ByteArrayInputStream(excelData);

        assertThrows(ExcelProcessingException.class, () -> excelService.importBooksFromExcel(is));
    }
}
