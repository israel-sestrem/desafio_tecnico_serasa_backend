package com.grain_weighing.controllers;

import com.grain_weighing.services.TruckService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TruckControllerMockConfig {

    @Bean
    public TruckService truckServiceMock() {
        return Mockito.mock(TruckService.class);
    }
}
