package com.grain_weighing.dto;

import java.math.BigDecimal;

public record TruckRequestDto(
        String licensePlate,
        BigDecimal tareWeightKg,
        String model
) {}
