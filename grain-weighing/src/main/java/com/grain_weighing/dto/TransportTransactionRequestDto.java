package com.grain_weighing.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransportTransactionRequestDto(
        UUID truckId,
        UUID branchId,
        UUID grainTypeId,
        BigDecimal appliedMargin
) {}
