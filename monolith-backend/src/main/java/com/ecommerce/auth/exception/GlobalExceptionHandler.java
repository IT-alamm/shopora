package com.ecommerce.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> badRequest(BadRequestException e, HttpServletRequest req) {
        return err(HttpStatus.BAD_REQUEST, e.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> unauthorized(UnauthorizedException e, HttpServletRequest req) {
        return err(HttpStatus.UNAUTHORIZED, e.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException e, HttpServletRequest req) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst().orElse("Validation failed");
        return err(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    private ResponseEntity<ApiError> err(HttpStatus s, String m, String p) {
        return ResponseEntity.status(s).body(new ApiError(Instant.now(), s.value(), m, p));
    }

    public record ApiError(Instant timestamp, int status, String message, String path) {}
}
