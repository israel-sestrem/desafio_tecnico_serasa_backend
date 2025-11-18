package com.grain_weighing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record WeighingRequestDto(
        @JsonProperty("id")
        @NotBlank
        String scaleExternalId,

        @JsonProperty("plate")
        @NotBlank
        @Size(max = 10)
        String licensePlate,

        @JsonProperty("weight")
        @NotNull
        @Digits(integer = 15, fraction = 3)
        BigDecimal weight
) {}
