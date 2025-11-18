package com.grain_weighing.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grain_weighing.dto.WeighingRequestDto;
import com.grain_weighing.entities.*;
import com.grain_weighing.services.WeighingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WeighingController.class)
@Import(WeighingControllerMockConfig.class)
class WeighingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WeighingService weighingServiceMock;

    private WeighingRequestDto createValidRequest() {
        return new WeighingRequestDto(
                "scale-123",
                "ABC1234",
                new BigDecimal("123.45")
        );
    }

    private WeighingEntity createExampleEntity() {
        return WeighingEntity.builder()
                .id(UUID.randomUUID())
                .scale(ScaleEntity.builder()
                        .id(UUID.randomUUID())
                        .externalId("scale-123")
                        .branch(BranchEntity.builder()
                                .id(UUID.randomUUID())
                                .name("Branch Test")
                                .build())
                        .build())
                .grossWeightKg(new BigDecimal("500"))
                .tareWeightKg(new BigDecimal("100"))
                .netWeightKg(new BigDecimal("400"))
                .weighingTimestamp(LocalDateTime.now())
                .truck(TruckEntity.builder()
                        .id(UUID.randomUUID())
                        .licensePlate("ABC1234")
                        .active(true)
                        .build())
                .grainType(GrainTypeEntity.builder()
                        .id(UUID.randomUUID())
                        .name("Soybean")
                        .build())
                .build();
    }

    @Test
    @DisplayName("201 CREATED → quando o peso estabiliza e o weighing é persistido")
    void insertCreatedWhenStable() throws Exception {

        var request = createValidRequest();
        var entity = createExampleEntity();

        Mockito.when(weighingServiceMock.insertRawWeighing(any(), anyString()))
                .thenReturn(Optional.of(entity));

        mockMvc.perform(post("/api/weighings/insert")
                        .header("X-Scale-Token", "token-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.netWeightKg").value(400.0));
    }

    @Test
    @DisplayName("202 ACCEPTED → quando o peso ainda não estabilizou")
    void insertAcceptedWhenPending() throws Exception {

        Mockito.when(weighingServiceMock.insertRawWeighing(any(), anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/weighings/insert")
                        .header("X-Scale-Token", "token-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING_STABILIZATION"))
                .andExpect(jsonPath("$.message").value("Weight not stable yet"));
    }

    @Test
    @DisplayName("400 BAD_REQUEST → quando o header X-Scale-Token não é informado")
    void insertMissingHeaderReturnsBadRequest() throws Exception {

        mockMvc.perform(post("/api/weighings/insert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(weighingServiceMock);
    }

    @Test
    @DisplayName("400 BAD_REQUEST → quando o body é inválido (erro de validação)")
    void insertInvalidBodyReturnsBadRequest() throws Exception {

        var invalidBody = """
                {
                  "id": "",
                  "weight": null
                }
                """;

        mockMvc.perform(post("/api/weighings/insert")
                        .header("X-Scale-Token", "token-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }
}
