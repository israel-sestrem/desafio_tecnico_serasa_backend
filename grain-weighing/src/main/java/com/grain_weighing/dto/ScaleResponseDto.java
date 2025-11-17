package com.grain_weighing.dto;

import java.util.UUID;

public record ScaleResponseDto(
        UUID id,
        String externalId,
        String description,
        UUID branchId,
        boolean active
) {}
