package com.faust0z.BookLibraryAPI.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> buildResponse(Exception e, HttpStatus status, HttpServletRequest request) {
        return buildResponse(e, e.getClass().getSimpleName(), e.getMessage(), status, request);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(Exception e, String error, String message, HttpStatus status,
                                                              HttpServletRequest request) {

        if (status.is5xxServerError()) {
            log.error("Server Error [{}]: {} - Path: {}", status.value(), message, request.getRequestURI(), e);
        } else if (status.is4xxClientError()) {
            log.warn("Client Error [{}]: {} - Path: {}", status.value(), message, request.getRequestURI());
        } else {
            log.info("Response Status [{}]: {} - Path: {}", status.value(), message, request.getRequestURI());
        }

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e, HttpServletRequest request) {
        return buildResponse(e, "InternalServerError", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(RuntimeException e, HttpServletRequest request) {
        return buildResponse(e, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({ResourceUnavailableException.class, LoanLimitExceededException.class, IncorrectPasswordException.class,
            SamePasswordException.class, AlreadyLoanedBookException.class, ExcelProcessingException.class})
    public ResponseEntity<Map<String, Object>> handleBusinessBadRequest(RuntimeException e, HttpServletRequest request) {
        return buildResponse(e, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<Map<String, Object>> handleConflictExceptions(RuntimeException e, HttpServletRequest request) {
        return buildResponse(e, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        return buildResponse(e, "AuthenticationFailure", "Incorrect email or password", HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        return buildResponse(e, "AccessDenied", "You do not have the required role to perform this action.", HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException e,
                                                                          HttpServletRequest request) {
        String cleanMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return buildResponse(e, "ValidationException", cleanMessage, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(Exception e, HttpServletRequest request) {
        return buildResponse(e, "Malformed JSON", "The request body is invalid or missing required formats.",
                HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(Exception e, HttpServletRequest request) {
        return buildResponse(e, "MethodNotAllowed", e.getMessage(),
                HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    @ExceptionHandler({org.springframework.web.context.request.async.AsyncRequestNotUsableException.class,
            org.apache.catalina.connector.ClientAbortException.class})
    public void handleClientDisconnect(Exception e, HttpServletRequest request) {
        log.debug("Client closed connection prematurely for path: {} - Message: {}",
                request.getRequestURI(), e.getMessage());
    }
}