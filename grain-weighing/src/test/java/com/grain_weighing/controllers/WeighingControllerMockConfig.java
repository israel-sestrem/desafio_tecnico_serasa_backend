package com.grain_weighing.controllers;

import com.grain_weighing.services.WeighingService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class WeighingControllerMockConfig {

    @Bean
    public WeighingService weighingServiceMock() {
        return Mockito.mock(WeighingService.class);
    }
}
