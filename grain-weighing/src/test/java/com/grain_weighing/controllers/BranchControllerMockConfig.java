package com.grain_weighing.controllers;

import com.grain_weighing.services.BranchService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class BranchControllerMockConfig {

    @Bean
    public BranchService branchServiceMock() {
        return Mockito.mock(BranchService.class);
    }
}
