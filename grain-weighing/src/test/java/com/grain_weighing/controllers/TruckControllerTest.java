package com.grain_weighing.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grain_weighing.dto.TruckRequestDto;
import com.grain_weighing.entities.TruckEntity;
import com.grain_weighing.services.TruckService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TruckController.class)
@Import(TruckControllerMockConfig.class)
class TruckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TruckService truckServiceMock;

    private TruckRequestDto createValidRequest() {
        return new TruckRequestDto(
                "ABC1D23",
                new BigDecimal("12000"),
                "Volvo FH",
                true
        );
    }

    private TruckEntity createTruck(UUID id,
                                    String licensePlate,
                                    BigDecimal tareWeightKg,
                                    String model,
                                    boolean active) {
        return TruckEntity.builder()
                .id(id)
                .licensePlate(licensePlate)
                .tareWeightKg(tareWeightKg)
                .model(model)
                .active(active)
                .build();
    }

    @Test
    @DisplayName("POST /api/trucks → 201 Created com Location e body correto")
    void createTruckReturnsCreated() throws Exception {
        TruckRequestDto request = createValidRequest();
        UUID id = UUID.randomUUID();

        TruckEntity saved = createTruck(
                id,
                request.licensePlate(),
                request.tareWeightKg(),
                request.model(),
                request.active()
        );

        Mockito.when(truckServiceMock.create(any(TruckRequestDto.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/trucks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/trucks/" + id))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.licensePlate").value("ABC1D23"))
                .andExpect(jsonPath("$.tareWeightKg").value(12000.0))
                .andExpect(jsonPath("$.model").value("Volvo FH"))
                .andExpect(jsonPath("$.active").value(true));

        Mockito.verify(truckServiceMock).create(any(TruckRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/trucks → 400 Bad Request quando body é inválido (Bean Validation)")
    void createTruckInvalidBodyReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                  "licensePlate": "",
                  "tareWeightKg": -10,
                  "model": null,
                  "active": null
                }
                """;

        mockMvc.perform(post("/api/trucks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/trucks/{id} → 200 OK com body atualizado")
    void updateTruckReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();

        TruckRequestDto request = new TruckRequestDto(
                "DEF4G56",
                new BigDecimal("13000.50"),
                "Scania R500",
                false
        );

        TruckEntity updated = createTruck(
                id,
                request.licensePlate(),
                request.tareWeightKg(),
                request.model(),
                request.active()
        );

        Mockito.when(truckServiceMock.update(eq(id), any(TruckRequestDto.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/trucks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.licensePlate").value("DEF4G56"))
                .andExpect(jsonPath("$.tareWeightKg").value(13000.50))
                .andExpect(jsonPath("$.model").value("Scania R500"))
                .andExpect(jsonPath("$.active").value(false));

        Mockito.verify(truckServiceMock).update(eq(id), any(TruckRequestDto.class));
    }

    @Test
    @DisplayName("GET /api/trucks → 200 OK retornando lista de trucks")
    void listTrucksReturnsOk() throws Exception {
        TruckEntity t1 = createTruck(
                UUID.randomUUID(),
                "ABC1D23",
                new BigDecimal("12000"),
                "Volvo FH",
                true
        );
        TruckEntity t2 = createTruck(
                UUID.randomUUID(),
                "DEF4G56",
                new BigDecimal("13000.50"),
                "Scania R500",
                false
        );

        Mockito.when(truckServiceMock.findAll())
                .thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/trucks")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].licensePlate").value("ABC1D23"))
                .andExpect(jsonPath("$[0].model").value("Volvo FH"))
                .andExpect(jsonPath("$[1].licensePlate").value("DEF4G56"))
                .andExpect(jsonPath("$[1].model").value("Scania R500"));

        Mockito.verify(truckServiceMock).findAll();
    }

    @Test
    @DisplayName("GET /api/trucks/{id} → 200 OK quando encontrado")
    void getTruckByIdReturnsOkWhenFound() throws Exception {
        UUID id = UUID.randomUUID();

        TruckEntity truck = createTruck(
                id,
                "ABC1D23",
                new BigDecimal("12000"),
                "Volvo FH",
                true
        );

        Mockito.when(truckServiceMock.findById(id))
                .thenReturn(Optional.of(truck));

        mockMvc.perform(get("/api/trucks/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.licensePlate").value("ABC1D23"))
                .andExpect(jsonPath("$.tareWeightKg").value(12000.0))
                .andExpect(jsonPath("$.model").value("Volvo FH"))
                .andExpect(jsonPath("$.active").value(true));

        Mockito.verify(truckServiceMock).findById(id);
    }

    @Test
    @DisplayName("GET /api/trucks/{id} → 404 Not Found quando não encontrado")
    void getTruckByIdReturnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(truckServiceMock.findById(id))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trucks/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(truckServiceMock).findById(id);
    }
}
