package com.grain_weighing.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grain_weighing.dto.BranchRequestDto;
import com.grain_weighing.entities.BranchEntity;
import com.grain_weighing.services.BranchService;
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

@WebMvcTest(controllers = BranchController.class)
@Import(BranchControllerMockConfig.class)
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BranchService branchServiceMock;

    private BranchRequestDto createValidRequest() {
        return new BranchRequestDto(
                "BR001",
                "Main Branch",
                "Blumenau",
                "SC"
        );
    }

    private BranchEntity createBranch(UUID id, String code, String name, String city, String state) {
        return BranchEntity.builder()
                .id(id)
                .code(code)
                .name(name)
                .city(city)
                .state(state)
                .build();
    }

    @Test
    @DisplayName("POST /api/branches → 201 Created com Location e body correto")
    void createBranchReturnsCreated() throws Exception {
        BranchRequestDto request = createValidRequest();
        UUID id = UUID.randomUUID();
        BranchEntity saved = createBranch(id, request.code(), request.name(), request.city(), request.state());

        Mockito.when(branchServiceMock.create(any(BranchRequestDto.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/branches/" + id))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("BR001"))
                .andExpect(jsonPath("$.name").value("Main Branch"))
                .andExpect(jsonPath("$.city").value("Blumenau"))
                .andExpect(jsonPath("$.state").value("SC"));

        Mockito.verify(branchServiceMock).create(any(BranchRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/branches → 400 Bad Request quando body é inválido")
    void createBranchInvalidBodyReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                  "code": "",
                  "name": "",
                  "city": "",
                  "state": ""
                }
                """;

        mockMvc.perform(post("/api/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/branches/{id} → 200 OK com body atualizado")
    void updateBranchReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        BranchRequestDto request = new BranchRequestDto(
                "BR002",
                "Updated Branch",
                "Itajai",
                "SC"
        );

        BranchEntity updated = createBranch(id, request.code(), request.name(), request.city(), request.state());

        Mockito.when(branchServiceMock.update(eq(id), any(BranchRequestDto.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/branches/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("BR002"))
                .andExpect(jsonPath("$.name").value("Updated Branch"))
                .andExpect(jsonPath("$.city").value("Itajai"))
                .andExpect(jsonPath("$.state").value("SC"));

        Mockito.verify(branchServiceMock).update(eq(id), any(BranchRequestDto.class));
    }

    @Test
    @DisplayName("GET /api/branches → 200 OK retornando lista de branches")
    void listBranchesReturnsOk() throws Exception {
        BranchEntity b1 = createBranch(UUID.randomUUID(), "BR001", "Main Branch", "Blumenau", "SC");
        BranchEntity b2 = createBranch(UUID.randomUUID(), "BR002", "Secondary Branch", "Itajai", "SC");

        Mockito.when(branchServiceMock.findAll())
                .thenReturn(List.of(b1, b2));

        mockMvc.perform(get("/api/branches")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("BR001"))
                .andExpect(jsonPath("$[0].name").value("Main Branch"))
                .andExpect(jsonPath("$[1].code").value("BR002"))
                .andExpect(jsonPath("$[1].name").value("Secondary Branch"));

        Mockito.verify(branchServiceMock).findAll();
    }

    @Test
    @DisplayName("GET /api/branches/{id} → 200 OK quando encontrado")
    void getBranchByIdReturnsOkWhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        BranchEntity branch = createBranch(id, "BR001", "Main Branch", "Blumenau", "SC");

        Mockito.when(branchServiceMock.findById(id))
                .thenReturn(Optional.of(branch));

        mockMvc.perform(get("/api/branches/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("BR001"))
                .andExpect(jsonPath("$.name").value("Main Branch"))
                .andExpect(jsonPath("$.city").value("Blumenau"))
                .andExpect(jsonPath("$.state").value("SC"));

        Mockito.verify(branchServiceMock).findById(id);
    }

    @Test
    @DisplayName("GET /api/branches/{id} → 404 Not Found quando não encontrado")
    void getBranchByIdReturnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(branchServiceMock.findById(id))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/branches/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(branchServiceMock).findById(id);
    }
}
