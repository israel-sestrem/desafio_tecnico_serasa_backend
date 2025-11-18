package com.grain_weighing.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grain_weighing.dto.ScaleRequestDto;
import com.grain_weighing.entities.BranchEntity;
import com.grain_weighing.entities.ScaleEntity;
import com.grain_weighing.services.ScaleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ScaleController.class)
@Import(ScaleControllerMockConfig.class)
class ScaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScaleService scaleServiceMock;

    private ScaleRequestDto createValidRequest() {
        return new ScaleRequestDto(
                UUID.randomUUID(),
                "EXT-001",
                "Main scale",
                "api-token-123",
                true
        );
    }

    private BranchEntity createBranch(UUID id) {
        return BranchEntity.builder()
                .id(id)
                .code("BR001")
                .name("Branch")
                .city("Blumenau")
                .state("SC")
                .build();
    }

    private ScaleEntity createScale(UUID id, BranchEntity branch, String externalId, String description, boolean active) {
        return ScaleEntity.builder()
                .id(id)
                .branch(branch)
                .externalId(externalId)
                .description(description)
                .apiToken("api-token-123")
                .active(active)
                .build();
    }

    @Test
    @DisplayName("POST /api/scales → 201 Created com Location e body correto")
    void createScaleReturnsCreated() throws Exception {
        ScaleRequestDto request = createValidRequest();
        UUID scaleId = UUID.randomUUID();
        UUID branchId = request.branchId();

        BranchEntity branch = createBranch(branchId);
        ScaleEntity saved = createScale(
                scaleId,
                branch,
                request.externalId(),
                request.description(),
                true
        );

        Mockito.when(scaleServiceMock.create(any(ScaleRequestDto.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/scales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/scales/" + scaleId))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(scaleId.toString()))
                .andExpect(jsonPath("$.externalId").value("EXT-001"))
                .andExpect(jsonPath("$.description").value("Main scale"))
                .andExpect(jsonPath("$.branchId").value(branchId.toString()))
                .andExpect(jsonPath("$.active").value(true));

        Mockito.verify(scaleServiceMock).create(any(ScaleRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/scales → 400 Bad Request quando body é inválido (Bean Validation)")
    void createScaleInvalidBodyReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                  "branchId": null,
                  "externalId": "",
                  "description": "x",
                  "apiToken": "",
                  "active": null
                }
                """;

        mockMvc.perform(post("/api/scales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/scales/{id} → 200 OK com body atualizado")
    void updateScaleReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        ScaleRequestDto request = new ScaleRequestDto(
                branchId,
                "EXT-002",
                "Updated scale",
                "new-api-token",
                false
        );

        BranchEntity branch = createBranch(branchId);
        ScaleEntity updated = createScale(
                id,
                branch,
                request.externalId(),
                request.description(),
                request.active()
        );

        Mockito.when(scaleServiceMock.update(eq(id), any(ScaleRequestDto.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/scales/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.externalId").value("EXT-002"))
                .andExpect(jsonPath("$.description").value("Updated scale"))
                .andExpect(jsonPath("$.branchId").value(branchId.toString()))
                .andExpect(jsonPath("$.active").value(false));

        Mockito.verify(scaleServiceMock).update(eq(id), any(ScaleRequestDto.class));
    }

    @Test
    @DisplayName("GET /api/scales → 200 OK retornando lista de scales")
    void listScalesReturnsOk() throws Exception {
        BranchEntity b1 = createBranch(UUID.randomUUID());
        BranchEntity b2 = createBranch(UUID.randomUUID());

        ScaleEntity s1 = createScale(UUID.randomUUID(), b1, "EXT-001", "Scale 1", true);
        ScaleEntity s2 = createScale(UUID.randomUUID(), b2, "EXT-002", "Scale 2", false);

        Mockito.when(scaleServiceMock.findAll())
                .thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/scales")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].externalId").value("EXT-001"))
                .andExpect(jsonPath("$[0].description").value("Scale 1"))
                .andExpect(jsonPath("$[1].externalId").value("EXT-002"))
                .andExpect(jsonPath("$[1].description").value("Scale 2"));

        Mockito.verify(scaleServiceMock).findAll();
    }

    @Test
    @DisplayName("GET /api/scales/{id} → 200 OK quando encontrado")
    void getScaleByIdReturnsOkWhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        BranchEntity branch = createBranch(branchId);
        ScaleEntity scale = createScale(id, branch, "EXT-001", "Main scale", true);

        Mockito.when(scaleServiceMock.findById(id))
                .thenReturn(Optional.of(scale));

        mockMvc.perform(get("/api/scales/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.externalId").value("EXT-001"))
                .andExpect(jsonPath("$.description").value("Main scale"))
                .andExpect(jsonPath("$.branchId").value(branchId.toString()))
                .andExpect(jsonPath("$.active").value(true));

        Mockito.verify(scaleServiceMock).findById(id);
    }

    @Test
    @DisplayName("GET /api/scales/{id} → 404 Not Found quando não encontrado")
    void getScaleByIdReturnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(scaleServiceMock.findById(id))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/scales/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(scaleServiceMock).findById(id);
    }
}
