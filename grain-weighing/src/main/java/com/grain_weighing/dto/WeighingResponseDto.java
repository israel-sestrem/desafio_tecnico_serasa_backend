package com.grain_weighing.dto;

import com.grain_weighing.entities.WeighingEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WeighingResponseDto(
        UUID id,
        String licensePlate,
        BigDecimal grossWeightKg,
        BigDecimal tareWeightKg,
        BigDecimal netWeightKg,
        LocalDateTime weighingTimestamp,
        UUID scaleId,
        UUID branchId,
        String grainTypeName,
        BigDecimal loadCost
) {

    public static WeighingResponseDto from(WeighingEntity w) {
        return new WeighingResponseDto(
                w.getId(),
                w.getLicensePlate(),
                w.getGrossWeightKg(),
                w.getTareWeightKg(),
                w.getNetWeightKg(),
                w.getWeighingTimestamp(),
                w.getScale().getId(),
                w.getScale().getBranch().getId(),
                w.getGrainType().getName(),
                w.getLoadCost()
        );
    }
}
