package com.grain_weighing.dto;

import java.math.BigDecimal;

public record GrainTypeRequestDto(
        String name,
        BigDecimal purchasePricePerTon,
        BigDecimal minMargin,
        BigDecimal maxMargin
) {}
