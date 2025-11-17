package com.grain_weighing.dto;

import java.util.UUID;

public record ScaleRequestDto(
        String externalId,
        String description,
        UUID branchId,
        String apiToken
) {}
