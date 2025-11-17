package com.grain_weighing.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TruckResponseDto(
        UUID id,
        String licensePlate,
        BigDecimal tareWeightKg,
        String model,
        boolean active
) {}
