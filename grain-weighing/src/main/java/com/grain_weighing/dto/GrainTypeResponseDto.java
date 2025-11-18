package com.grain_weighing.dto;

import com.grain_weighing.entities.GrainTypeEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record GrainTypeResponseDto(
        UUID id,
        String name,
        BigDecimal purchasePricePerTon,
        BigDecimal minMargin,
        BigDecimal maxMargin
) {

    public static GrainTypeResponseDto from(GrainTypeEntity grainType) {
        return new GrainTypeResponseDto(
                grainType.getId(),
                grainType.getName(),
                grainType.getPurchasePricePerTon(),
                grainType.getMinMargin(),
                grainType.getMaxMargin()
        );
    }

}
