package com.grain_weighing.controllers;

import com.grain_weighing.services.GrainTypeService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrainTypeControllerMockConfig {

    @Bean
    public GrainTypeService grainTypeServiceMock() {
        return Mockito.mock(GrainTypeService.class);
    }
}
