package com.grain_weighing.exceptions;

import com.grain_weighing.dto.ApiErrorDto;
import com.grain_weighing.dto.FieldErrorDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "error", "BAD_REQUEST",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(409).body(
                Map.of(
                        "error", "INVALID_STATE",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleValidationException(MethodArgumentNotValidException ex) {

        List<FieldErrorDto> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDto(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()
                ))
                .toList();

        ApiErrorDto body = new ApiErrorDto(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                "One or more fields are invalid",
                fieldErrors
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDto> handleConstraintViolation(ConstraintViolationException ex) {

        List<FieldErrorDto> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(cv -> new FieldErrorDto(
                        cv.getPropertyPath().toString(),
                        cv.getMessage(),
                        cv.getInvalidValue()
                ))
                .toList();

        ApiErrorDto body = new ApiErrorDto(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                "One or more parameters are invalid",
                fieldErrors
        );

        return ResponseEntity.badRequest().body(body);
    }
}
