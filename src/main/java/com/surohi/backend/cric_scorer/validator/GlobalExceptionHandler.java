package com.surohi.backend.cric_scorer.validator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationResponse> handleValidationException(ValidationException ex) {
        return ResponseEntity.badRequest().body(ValidationResponse.builder()
                .message(ex.getMessage())
                .errors(ex.getErrors())
                .build());
    }
}

