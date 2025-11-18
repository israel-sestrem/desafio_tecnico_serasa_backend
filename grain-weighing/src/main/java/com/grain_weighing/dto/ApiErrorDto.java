package com.grain_weighing.dto;

import java.util.List;

public record ApiErrorDto(
        int status,
        String error,
        String message,
        List<FieldErrorDto> fieldErrors
) {}
