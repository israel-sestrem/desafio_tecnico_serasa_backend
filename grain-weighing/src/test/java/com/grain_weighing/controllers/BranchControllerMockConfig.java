package com.grain_weighing.controllers;

import com.grain_weighing.services.BranchService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BranchControllerMockConfig {

    @Bean
    public BranchService branchServiceMock() {
        return Mockito.mock(BranchService.class);
    }
}
