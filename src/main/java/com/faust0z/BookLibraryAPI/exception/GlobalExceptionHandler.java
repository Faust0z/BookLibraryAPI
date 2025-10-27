package com.faust0z.BookLibraryAPI.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> buildResponse(Exception e, HttpStatus status, HttpServletRequest request) {
        return buildResponse(e, e.getClass().getSimpleName(), e.getMessage(), status, request);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(Exception e, String error, String message, HttpStatus status,
                                                              HttpServletRequest request) {
        return new ResponseEntity<>(createErrorBody(error, message, status, request), status);
    }

    private Map<String, Object> createErrorBody(String error, String message, HttpStatus status, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", error);
        body.put("message", message != null ? message : "An error occurred");
        body.put("status", status.value());
        body.put("path", request.getRequestURI());
        body.put("timestamp", Instant.now().toString());
        return body;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        return buildResponse(e, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({BookUnavailableException.class, LoanLimitExceededException.class})
    public ResponseEntity<Map<String, Object>> handleBusinessBadRequest(RuntimeException e, HttpServletRequest request) {
        return buildResponse(e, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException e, HttpServletRequest request) {

        String cleanMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        Map<String, Object> body = createErrorBody(
                "ValidationException",
                cleanMessage,
                HttpStatus.BAD_REQUEST,
                request
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception e, HttpServletRequest request) {
        return buildResponse(e, "InternalServerError", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}