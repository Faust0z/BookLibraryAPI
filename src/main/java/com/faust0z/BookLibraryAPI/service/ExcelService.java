package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.entity.BookEntity;
import com.faust0z.BookLibraryAPI.exception.ExcelProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

@Slf4j
@Service
public class ExcelService {

    private static final String SHEET_NAME = "Library Inventory";
    private final DataFormatter dataFormatter = new DataFormatter();

    private record ExcelColumn(
            String header,
            Function<BookEntity, Object> extractor,
            BiConsumer<BookEntity, Cell> importer
    ) {
    }

    private final List<ExcelColumn> COLUMNS = List.of(
            new ExcelColumn("Title", BookEntity::getName, (book, cell) -> book.setName(getCellValueAsString(cell))),
            new ExcelColumn("Author", BookEntity::getAuthor, (book, cell) -> book.setAuthor(getCellValueAsString(cell))),
            new ExcelColumn("Publication Date", BookEntity::getPublicationDate, (book, cell) -> book.setPublicationDate(getCellValueAsDate(cell))),
            new ExcelColumn("Available Copies", BookEntity::getCopies, (book, cell) -> book.setCopies((int) getCellValueAsNumber(cell)))
    );

    public byte[] exportBooksToExcel(List<BookEntity> books) {
        log.info("Initiating Excel export for {} books", books.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET_NAME);
            createHeaderRow(workbook, sheet);

            log.debug("Populating Excel rows...");
            IntStream.range(0, books.size()).forEach(i -> {
                Row row = sheet.createRow(i + 1);
                BookEntity book = books.get(i);

                for (int colIdx = 0; colIdx < COLUMNS.size(); colIdx++) {
                    Cell cell = row.createCell(colIdx);
                    Object value = COLUMNS.get(colIdx).extractor().apply(book);
                    setCellValue(cell, value);
                }
                if (i % 100 == 0 && i > 0) log.debug("Processed {}/{} rows", i, books.size());
            });

            log.debug("Auto-sizing columns for better readability");
            IntStream.range(0, COLUMNS.size()).forEach(sheet::autoSizeColumn);

            workbook.write(out);
            log.info("Excel export completed successfully");
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Critical error during Excel export: {}", e.getMessage(), e);
            throw new ExcelProcessingException("Failed to generate Excel file");
        }
    }

    public List<BookEntity> importBooksFromExcel(InputStream is) {
        log.info("Starting Excel import process");
        List<BookEntity> books = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            if (!rows.hasNext()) {
                log.warn("Import attempted on an empty Excel file");
                throw new ExcelProcessingException("The Excel file is empty!");
            }

            Row headerRow = rows.next();
            Map<String, Integer> headerMap = mapHeaders(headerRow);
            validateHeaders(headerMap);

            log.debug("Iterating through data rows...");
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                if (isRowEmpty(currentRow)) {
                    log.debug("Skipping empty row at index {}", currentRow.getRowNum());
                    continue;
                }

                int rowNum = currentRow.getRowNum() + 1;
                BookEntity book = new BookEntity();
                for (ExcelColumn column : COLUMNS) {
                    Integer colIdx = headerMap.get(column.header());
                    if (colIdx != null) {
                        column.importer().accept(book, currentRow.getCell(colIdx));
                    }
                }

                validateBook(book, rowNum);
                books.add(book);
                log.debug("Successfully parsed row {}: {}", rowNum, book.getName());
            }

            log.info("Successfully imported {} books from Excel", books.size());
            return books;

        } catch (Exception e) {
            log.error("Failed to parse Excel file: {}", e.getMessage(), e);
            throw new ExcelProcessingException("Import failed: " + e.getMessage());
        }
    }

    private void createHeaderRow(Workbook workbook, Sheet sheet) {
        log.debug("Creating header row with style");
        Row headerRow = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < COLUMNS.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(COLUMNS.get(i).header());
            cell.setCellStyle(headerStyle);
        }
    }

    private void setCellValue(Cell cell, Object value) {
        switch (value) {
            case Number n -> cell.setCellValue(n.doubleValue());
            case LocalDate d -> cell.setCellValue(d.toString());
            case String s -> cell.setCellValue(s);
            case null -> {
            }
            default -> cell.setCellValue(value.toString());
        }
    }

    private Map<String, Integer> mapHeaders(Row headerRow) {
        log.debug("Mapping Excel headers to column indices");
        Map<String, Integer> headerMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String val = dataFormatter.formatCellValue(cell).trim();
            if (!val.isEmpty()) {
                headerMap.put(val, cell.getColumnIndex());
                log.debug("Found header '{}' at index {}", val, cell.getColumnIndex());
            }
        }
        return headerMap;
    }

    private void validateHeaders(Map<String, Integer> headerMap) {
        COLUMNS.stream()
                .map(ExcelColumn::header)
                .filter(header -> !headerMap.containsKey(header))
                .findFirst()
                .ifPresent(missing -> {
                    log.error("Missing required header: {}", missing);
                    throw new ExcelProcessingException("Missing required column: " + missing);
                });
    }

    private void validateBook(BookEntity book, int rowNum) {
        if (book.getName() == null || book.getName().isBlank())
            throw new ExcelProcessingException("Title is required at row " + rowNum);
        if (book.getAuthor() == null || book.getAuthor().isBlank())
            throw new ExcelProcessingException("Author is required at row " + rowNum);
        if (book.getCopies() != null && book.getCopies() < 0)
            throw new ExcelProcessingException("Copies cannot be negative at row " + rowNum);
        if (book.getPublicationDate() == null)
            throw new ExcelProcessingException("Valid Publication Date is required at row " + rowNum);
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        return IntStream.range(row.getFirstCellNum(), row.getLastCellNum())
                .mapToObj(row::getCell)
                .allMatch(cell -> cell == null || cell.getCellType() == CellType.BLANK);
    }

    private String getCellValueAsString(Cell cell) {
        return dataFormatter.formatCellValue(cell).trim();
    }

    private double getCellValueAsNumber(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return 0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();

        String val = getCellValueAsString(cell);
        try {
            return val.isEmpty() ? 0 : Double.parseDouble(val);
        } catch (NumberFormatException e) {
            log.debug("Could not parse numeric value from '{}' at {}", val, cell.getAddress());
            return 0;
        }
    }

    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        String dateStr = getCellValueAsString(cell);
        try {
            return dateStr.isEmpty() ? null : LocalDate.parse(dateStr);
        } catch (Exception e) {
            log.debug("Failed to parse date from string '{}' at {}", dateStr, cell.getAddress());
            return null;
        }
    }
}
