package com.grain_weighing.controllers;

import com.grain_weighing.services.ScaleService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ScaleControllerMockConfig {

    @Bean
    public ScaleService scaleServiceMock() {
        return Mockito.mock(ScaleService.class);
    }
}
