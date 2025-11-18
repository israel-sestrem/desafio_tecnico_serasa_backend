package com.grain_weighing.dto;

import com.grain_weighing.entities.BranchEntity;

import java.util.UUID;

public record BranchResponseDto(
        UUID id,
        String code,
        String name,
        String city,
        String state
) {

    public static BranchResponseDto from(BranchEntity branch) {
        return new BranchResponseDto(
                branch.getId(),
                branch.getCode(),
                branch.getName(),
                branch.getCity(),
                branch.getState()
        );
    }

}
