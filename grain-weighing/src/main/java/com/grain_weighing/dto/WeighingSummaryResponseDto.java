package com.grain_weighing.dto;

import java.math.BigDecimal;

public record WeighingSummaryResponseDto(
        BigDecimal totalNetWeightKg,
        BigDecimal totalLoadCost,
        BigDecimal estimatedRevenue,
        BigDecimal estimatedProfit
) {}
