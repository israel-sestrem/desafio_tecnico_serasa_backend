package com.grain_weighing.exceptions;

import com.grain_weighing.dto.ApiErrorDto;
import com.grain_weighing.dto.FieldErrorDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleIllegalArgument() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().get("error"));
        assertEquals("Invalid argument", response.getBody().get("message"));
    }

    @Test
    void testHandleIllegalState() {
        IllegalStateException exception = new IllegalStateException("Invalid state");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleIllegalState(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("INVALID_STATE", response.getBody().get("error"));
        assertEquals("Invalid state", response.getBody().get("message"));
    }

    @Test
    void testHandleValidationException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "Invalid value");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiErrorDto> response = globalExceptionHandler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorDto body = response.getBody();
        assertNotNull(body);
        assertEquals("Validation error", body.error());
        assertEquals("One or more fields are invalid", body.message());
        assertEquals(1, body.fieldErrors().size());
        FieldErrorDto errorDto = body.fieldErrors().get(0);
        assertEquals("fieldName", errorDto.field());
        assertEquals("Invalid value", errorDto.message());
    }

    @Test
    void testHandleConstraintViolation() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("fieldName");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Invalid value");
        when(violation.getInvalidValue()).thenReturn("Rejected value");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ApiErrorDto> response = globalExceptionHandler.handleConstraintViolation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorDto body = response.getBody();
        assertNotNull(body);
        assertEquals("Validation error", body.error());
        assertEquals("One or more parameters are invalid", body.message());
        assertEquals(1, body.fieldErrors().size());
        FieldErrorDto errorDto = body.fieldErrors().get(0);
        assertEquals("fieldName", errorDto.field());
        assertEquals("Invalid value", errorDto.message());
        assertEquals("Rejected value", errorDto.rejectedValue());
    }
}