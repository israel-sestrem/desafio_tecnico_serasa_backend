package com.grain_weighing.controllers;

import com.grain_weighing.services.TruckService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TruckControllerMockConfig {

    @Bean
    public TruckService truckServiceMock() {
        return Mockito.mock(TruckService.class);
    }
}
