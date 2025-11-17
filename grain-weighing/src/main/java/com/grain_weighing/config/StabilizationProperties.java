package com.grain_weighing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "grain-weighing.stabilization")
public class StabilizationProperties {
    private int requiredStableReadings;
    private BigDecimal maxDiffBetweenReadingsKg;
}

