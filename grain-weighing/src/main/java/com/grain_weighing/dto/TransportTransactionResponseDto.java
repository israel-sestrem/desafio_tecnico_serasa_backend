package com.grain_weighing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransportTransactionResponseDto(
        UUID id,
        UUID truckId,
        UUID branchId,
        UUID grainTypeId,
        LocalDateTime startTimestamp,
        LocalDateTime endTimestamp,
        BigDecimal appliedMargin,
        BigDecimal purchasePricePerTon,
        BigDecimal salePricePerTon,
        BigDecimal totalNetWeightKg,
        BigDecimal totalLoadCost,
        BigDecimal totalEstimatedRevenue,
        BigDecimal estimatedProfit
) {}
