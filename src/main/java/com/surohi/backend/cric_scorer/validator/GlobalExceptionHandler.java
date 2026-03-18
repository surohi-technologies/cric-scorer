package com.surohi.backend.cric_scorer.validator;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationResponse> handleValidationException(ValidationException ex) {
        return ResponseEntity.badRequest().body(ValidationResponse.builder()
                .message(ex.getMessage())
                .errors(ex.getErrors())
                .build());
    }

    /**
     * Handles bean validation errors from @Valid request bodies (DTO annotations).
     * This keeps frontend error rendering consistent with our custom ValidationException shape.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ValidationError> errors = new ArrayList<>();

        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            String msg = fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage();
            errors.add(new ValidationError(fe.getField(), msg));
        }

        return ResponseEntity.badRequest().body(ValidationResponse.builder()
                .message("Validation failed")
                .errors(errors)
                .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<ValidationError> errors = new ArrayList<>();
        ex.getConstraintViolations().forEach(v ->
                errors.add(new ValidationError(String.valueOf(v.getPropertyPath()), v.getMessage()))
        );
        return ResponseEntity.badRequest().body(ValidationResponse.builder()
                .message("Validation failed")
                .errors(errors)
                .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ValidationResponse.builder()
                .message("Malformed JSON request body")
                .errors(List.of(new ValidationError("request", "Malformed JSON request body")))
                .build());
    }

    /**
     * Common unique-constraint and FK issues.
     * We return 409 so the UI can show "already exists" style messages.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ValidationResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = "Conflict: data violates database constraint";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ValidationResponse.builder()
                .message(msg)
                .errors(List.of(new ValidationError("database", msg)))
                .build());
    }
}
