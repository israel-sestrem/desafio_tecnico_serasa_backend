package com.grain_weighing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "grain-weighing.stabilization")
@Data
public class StabilizationProperties {
    private int requiredStableReadings;
    private BigDecimal maxDiffBetweenReadingsKg;
}

