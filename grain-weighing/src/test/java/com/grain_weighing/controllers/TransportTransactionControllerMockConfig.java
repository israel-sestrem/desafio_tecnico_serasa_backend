package com.grain_weighing.controllers;

import com.grain_weighing.services.TransportTransactionService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TransportTransactionControllerMockConfig {

    @Bean
    public TransportTransactionService transportTransactionServiceMock() {
        return Mockito.mock(TransportTransactionService.class);
    }
}
