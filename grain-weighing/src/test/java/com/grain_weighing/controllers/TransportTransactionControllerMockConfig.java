package com.grain_weighing.controllers;

import com.grain_weighing.services.TransportTransactionService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransportTransactionControllerMockConfig {

    @Bean
    public TransportTransactionService transportTransactionServiceMock() {
        return Mockito.mock(TransportTransactionService.class);
    }
}
