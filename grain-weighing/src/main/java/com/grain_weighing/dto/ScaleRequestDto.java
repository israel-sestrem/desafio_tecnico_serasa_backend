package com.grain_weighing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ScaleRequestDto(

        @NotNull
        UUID branchId,

        @NotBlank
        @Size(max = 50)
        String externalId,

        @Size(max = 100)
        String description,

        @NotBlank
        @Size(max = 120)
        String apiToken,

        @NotNull
        Boolean active
) {}
