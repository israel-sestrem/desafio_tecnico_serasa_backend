package com.grain_weighing.dto;

import java.util.UUID;

public record BranchResponseDto(
        UUID id,
        String code,
        String name,
        String city,
        String state
) {}
