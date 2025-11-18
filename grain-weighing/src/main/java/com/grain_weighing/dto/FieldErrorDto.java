package com.grain_weighing.dto;

public record FieldErrorDto(
        String field,
        String message,
        Object rejectedValue
) {}
