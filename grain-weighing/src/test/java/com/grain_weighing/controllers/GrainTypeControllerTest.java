package com.grain_weighing.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grain_weighing.dto.GrainTypeRequestDto;
import com.grain_weighing.entities.GrainTypeEntity;
import com.grain_weighing.services.GrainTypeService;
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

@WebMvcTest(controllers = GrainTypeController.class)
@Import(GrainTypeControllerMockConfig.class)
class GrainTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GrainTypeService grainTypeServiceMock;

    private GrainTypeRequestDto createValidRequest() {
        return new GrainTypeRequestDto(
                "Soybean",
                new BigDecimal("1000.50"),
                new BigDecimal("0.05"),
                new BigDecimal("0.15")
        );
    }

    private GrainTypeEntity createGrainType(UUID id,
                                            String name,
                                            BigDecimal price,
                                            BigDecimal minMargin,
                                            BigDecimal maxMargin) {
        return GrainTypeEntity.builder()
                .id(id)
                .name(name)
                .purchasePricePerTon(price)
                .minMargin(minMargin)
                .maxMargin(maxMargin)
                .build();
    }

    @Test
    @DisplayName("POST /api/grain-types → 201 Created com Location e body correto")
    void createGrainTypeReturnsCreated() throws Exception {
        GrainTypeRequestDto request = createValidRequest();
        UUID id = UUID.randomUUID();

        GrainTypeEntity saved = createGrainType(
                id,
                request.name(),
                request.purchasePricePerTon(),
                request.minMargin(),
                request.maxMargin()
        );

        Mockito.when(grainTypeServiceMock.create(any(GrainTypeRequestDto.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/grain-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/grain-types/" + id))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Soybean"))
                .andExpect(jsonPath("$.purchasePricePerTon").value(1000.50))
                .andExpect(jsonPath("$.minMargin").value(0.05))
                .andExpect(jsonPath("$.maxMargin").value(0.15));

        Mockito.verify(grainTypeServiceMock).create(any(GrainTypeRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/grain-types → 400 Bad Request quando body é inválido (Bean Validation)")
    void createGrainTypeInvalidBodyReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "purchasePricePerTon": null,
                  "minMargin": null,
                  "maxMargin": null
                }
                """;

        mockMvc.perform(post("/api/grain-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/grain-types/{id} → 200 OK com body atualizado")
    void updateGrainTypeReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        GrainTypeRequestDto request = new GrainTypeRequestDto(
                "Corn",
                new BigDecimal("800.00"),
                new BigDecimal("0.06"),
                new BigDecimal("0.18")
        );

        GrainTypeEntity updated = createGrainType(
                id,
                request.name(),
                request.purchasePricePerTon(),
                request.minMargin(),
                request.maxMargin()
        );

        Mockito.when(grainTypeServiceMock.update(eq(id), any(GrainTypeRequestDto.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/grain-types/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Corn"))
                .andExpect(jsonPath("$.purchasePricePerTon").value(800.00))
                .andExpect(jsonPath("$.minMargin").value(0.06))
                .andExpect(jsonPath("$.maxMargin").value(0.18));

        Mockito.verify(grainTypeServiceMock).update(eq(id), any(GrainTypeRequestDto.class));
    }

    @Test
    @DisplayName("GET /api/grain-types → 200 OK retornando lista de grain types")
    void listGrainTypesReturnsOk() throws Exception {
        GrainTypeEntity gt1 = createGrainType(
                UUID.randomUUID(),
                "Soybean",
                new BigDecimal("1000.50"),
                new BigDecimal("0.05"),
                new BigDecimal("0.15")
        );
        GrainTypeEntity gt2 = createGrainType(
                UUID.randomUUID(),
                "Corn",
                new BigDecimal("800.00"),
                new BigDecimal("0.06"),
                new BigDecimal("0.18")
        );

        Mockito.when(grainTypeServiceMock.findAll())
                .thenReturn(List.of(gt1, gt2));

        mockMvc.perform(get("/api/grain-types")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Soybean"))
                .andExpect(jsonPath("$[1].name").value("Corn"));

        Mockito.verify(grainTypeServiceMock).findAll();
    }

    @Test
    @DisplayName("GET /api/grain-types/{id} → 200 OK quando encontrado")
    void getGrainTypeByIdReturnsOkWhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        GrainTypeEntity gt = createGrainType(
                id,
                "Soybean",
                new BigDecimal("1000.50"),
                new BigDecimal("0.05"),
                new BigDecimal("0.15")
        );

        Mockito.when(grainTypeServiceMock.findById(id))
                .thenReturn(Optional.of(gt));

        mockMvc.perform(get("/api/grain-types/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Soybean"))
                .andExpect(jsonPath("$.purchasePricePerTon").value(1000.50))
                .andExpect(jsonPath("$.minMargin").value(0.05))
                .andExpect(jsonPath("$.maxMargin").value(0.15));

        Mockito.verify(grainTypeServiceMock).findById(id);
    }

    @Test
    @DisplayName("GET /api/grain-types/{id} → 404 Not Found quando não encontrado")
    void getGrainTypeByIdReturnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(grainTypeServiceMock.findById(id))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/grain-types/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(grainTypeServiceMock).findById(id);
    }
}
