package com.grain_weighing.controllers;

import com.grain_weighing.services.ReportService;
import com.grain_weighing.services.WeighingService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportControllerMockConfig {

    @Bean
    public WeighingService weighingServiceMock() {
        return Mockito.mock(WeighingService.class);
    }

    @Bean
    public ReportService reportServiceMock() {
        return Mockito.mock(ReportService.class);
    }
}
