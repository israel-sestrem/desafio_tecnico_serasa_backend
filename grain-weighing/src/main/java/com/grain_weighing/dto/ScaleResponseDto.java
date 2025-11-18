package com.grain_weighing.dto;

import com.grain_weighing.entities.ScaleEntity;

import java.util.UUID;

public record ScaleResponseDto(
        UUID id,
        String externalId,
        String description,
        UUID branchId,
        boolean active
) {

    public static ScaleResponseDto from(ScaleEntity scale) {
        return new ScaleResponseDto(
                scale.getId(),
                scale.getExternalId(),
                scale.getDescription(),
                scale.getBranch().getId(),
                scale.isActive()
        );
    }

}
