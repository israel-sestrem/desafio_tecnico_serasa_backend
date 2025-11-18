package com.grain_weighing.dto;

import com.grain_weighing.entities.TruckEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record TruckResponseDto(
        UUID id,
        String licensePlate,
        BigDecimal tareWeightKg,
        String model,
        boolean active
) {

    public static TruckResponseDto from(TruckEntity truck) {
        return new TruckResponseDto(
                truck.getId(),
                truck.getLicensePlate(),
                truck.getTareWeightKg(),
                truck.getModel(),
                truck.isActive()
        );
    }

}
