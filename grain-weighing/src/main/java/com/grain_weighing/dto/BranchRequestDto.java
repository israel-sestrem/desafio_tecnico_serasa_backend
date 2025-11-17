package com.grain_weighing.dto;

public record BranchRequestDto(
        String code,
        String name,
        String city,
        String state
) {}
