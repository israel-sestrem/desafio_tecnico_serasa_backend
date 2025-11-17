package com.grain_weighing.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GrainTypeResponseDto(
        UUID id,
        String name,
        BigDecimal purchasePricePerTon,
        BigDecimal minMargin,
        BigDecimal maxMargin
) {}
