package com.grain_weighing.controllers;

import com.grain_weighing.services.ScaleService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScaleControllerMockConfig {

    @Bean
    public ScaleService scaleServiceMock() {
        return Mockito.mock(ScaleService.class);
    }
}
