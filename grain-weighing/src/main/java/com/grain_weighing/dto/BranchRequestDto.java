package com.grain_weighing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BranchRequestDto(
        @NotBlank
        @Size(max = 20)
        String code,

        @NotBlank
        @Size(max = 120)
        String name,

        @NotBlank
        @Size(max = 80)
        String city,

        @NotBlank
        @Size(max = 2)
        String state
) {}
