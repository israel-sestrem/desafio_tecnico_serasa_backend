package com.grain_weighing.dto;

import com.grain_weighing.entities.TransportTransactionEntity;

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
) {

    public static TransportTransactionResponseDto from(TransportTransactionEntity tt){
        return new TransportTransactionResponseDto(
                tt.getId(),
                tt.getTruck().getId(),
                tt.getBranch().getId(),
                tt.getGrainType().getId(),
                tt.getStartTimestamp(),
                tt.getEndTimestamp(),
                tt.getAppliedMargin(),
                tt.getPurchasePricePerTon(),
                tt.getSalePricePerTon(),
                tt.getTotalNetWeightKg(),
                tt.getTotalLoadCost(),
                tt.getTotalEstimatedRevenue(),
                tt.getEstimatedProfit()
        );
    }

}
