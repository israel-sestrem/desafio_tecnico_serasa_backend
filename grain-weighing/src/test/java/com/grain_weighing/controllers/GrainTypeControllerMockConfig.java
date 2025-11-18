package com.grain_weighing.controllers;

import com.grain_weighing.services.GrainTypeService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class GrainTypeControllerMockConfig {

    @Bean
    public GrainTypeService grainTypeServiceMock() {
        return Mockito.mock(GrainTypeService.class);
    }
}
