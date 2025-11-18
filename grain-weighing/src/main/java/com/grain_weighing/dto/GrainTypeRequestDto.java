package com.grain_weighing.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record GrainTypeRequestDto(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false)
        @Digits(integer = 15, fraction = 2)
        BigDecimal purchasePricePerTon,

        @NotNull
        @DecimalMin(value = "0.05")
        @DecimalMax(value = "0.20")
        BigDecimal minMargin,

        @NotNull
        @DecimalMin(value = "0.05")
        @DecimalMax(value = "0.20")
        BigDecimal maxMargin
) {}
