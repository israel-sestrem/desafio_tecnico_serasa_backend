package com.grain_weighing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record WeighingInsertionRequestDto(
        @JsonProperty("id")
        String scaleExternalId,

        @JsonProperty("plate")
        String licensePlate,

        @JsonProperty("weight")
        BigDecimal weight
) {}
