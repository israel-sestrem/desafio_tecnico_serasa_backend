package com.grain_weighing.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransportTransactionRequestDto(
        @NotNull
        UUID truckId,

        @NotNull
        UUID branchId,

        @NotNull
        UUID grainTypeId,

        @Digits(integer = 5, fraction = 4)
        BigDecimal appliedMargin
) {}
