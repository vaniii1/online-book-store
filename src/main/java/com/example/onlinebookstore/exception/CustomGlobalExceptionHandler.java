package com.example.onlinebookstore.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(this::getErrorMessage)
                .toList();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler({
            EntityNotFoundException.class
    })
    public ResponseEntity<Object> handleNotFoundException(RuntimeException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, List.of(e.getMessage()));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            ConstraintViolationException.class,
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<Object> handleBadRequests(RuntimeException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, List.of(e.getMessage()));
    }

    @ExceptionHandler({
            AccessDeniedException.class
    })
    public ResponseEntity<Object> handleForbiddenExceptions(RuntimeException e) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, List.of(e.getMessage()));
    }

    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, List<String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);
        body.put("errors", errors);
        return ResponseEntity.status(status).body(body);
    }

    private String getErrorMessage(ObjectError objectError) {
        if (objectError instanceof FieldError fieldError) {
            return fieldError.getField() + " " + fieldError.getDefaultMessage();
        }
        return objectError.getDefaultMessage();
    }
}

