package com.grain_weighing.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TruckRequestDto(
        @NotBlank
        @Size(max = 10)
        String licensePlate,

        @NotNull
        @Positive
        @Digits(integer = 15, fraction = 2)
        BigDecimal tareWeightKg,

        @NotNull
        @Size(max = 80)
        String model,

        @NotNull
        Boolean active
) {}
