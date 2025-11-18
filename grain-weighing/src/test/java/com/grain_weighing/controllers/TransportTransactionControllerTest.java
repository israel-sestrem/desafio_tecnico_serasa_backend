package com.grain_weighing.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grain_weighing.dto.TransportTransactionRequestDto;
import com.grain_weighing.entities.BranchEntity;
import com.grain_weighing.entities.GrainTypeEntity;
import com.grain_weighing.entities.TransportTransactionEntity;
import com.grain_weighing.entities.TruckEntity;
import com.grain_weighing.services.TransportTransactionService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransportTransactionController.class)
@Import(TransportTransactionControllerMockConfig.class)
class TransportTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransportTransactionService transportTransactionServiceMock;

    private TransportTransactionRequestDto createValidRequest(UUID truckId,
                                                              UUID branchId,
                                                              UUID grainTypeId) {
        return new TransportTransactionRequestDto(
                truckId,
                branchId,
                grainTypeId,
                new BigDecimal("0.10")
        );
    }

    private TruckEntity createTruck(UUID id) {
        return TruckEntity.builder()
                .id(id)
                .licensePlate("ABC1D23")
                .tareWeightKg(new BigDecimal("12000"))
                .model("Volvo FH")
                .active(true)
                .build();
    }

    private BranchEntity createBranch(UUID id) {
        return BranchEntity.builder()
                .id(id)
                .code("BR001")
                .name("Main Branch")
                .city("Blumenau")
                .state("SC")
                .build();
    }

    private GrainTypeEntity createGrainType(UUID id) {
        return GrainTypeEntity.builder()
                .id(id)
                .name("Soybean")
                .purchasePricePerTon(new BigDecimal("1000.00"))
                .minMargin(new BigDecimal("0.05"))
                .maxMargin(new BigDecimal("0.20"))
                .build();
    }

    private TransportTransactionEntity createOpenTransaction(UUID id,
                                                             TruckEntity truck,
                                                             BranchEntity branch,
                                                             GrainTypeEntity grainType,
                                                             LocalDateTime start) {
        return TransportTransactionEntity.builder()
                .id(id)
                .truck(truck)
                .branch(branch)
                .grainType(grainType)
                .startTimestamp(start)
                .endTimestamp(null)
                .appliedMargin(new BigDecimal("0.10"))
                .purchasePricePerTon(new BigDecimal("1000.00"))
                .salePricePerTon(new BigDecimal("1100.00"))
                .totalNetWeightKg(null)
                .totalLoadCost(null)
                .totalEstimatedRevenue(null)
                .estimatedProfit(null)
                .build();
    }

    private TransportTransactionEntity createClosedTransaction(UUID id,
                                                               TruckEntity truck,
                                                               BranchEntity branch,
                                                               GrainTypeEntity grainType,
                                                               LocalDateTime start,
                                                               LocalDateTime end) {
        return TransportTransactionEntity.builder()
                .id(id)
                .truck(truck)
                .branch(branch)
                .grainType(grainType)
                .startTimestamp(start)
                .endTimestamp(end)
                .appliedMargin(new BigDecimal("0.10"))
                .purchasePricePerTon(new BigDecimal("1000.00"))
                .salePricePerTon(new BigDecimal("1100.00"))
                .totalNetWeightKg(new BigDecimal("350.000"))
                .totalLoadCost(new BigDecimal("186.00"))
                .totalEstimatedRevenue(new BigDecimal("196.00"))
                .estimatedProfit(new BigDecimal("10.00"))
                .build();
    }

    @Test
    @DisplayName("POST /api/transport-transactions/open → 201 Created com Location e body correto")
    void openTransportTransactionReturnsCreated() throws Exception {
        UUID ttId = UUID.randomUUID();
        UUID truckId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID grainId = UUID.randomUUID();

        TransportTransactionRequestDto request = createValidRequest(truckId, branchId, grainId);

        TruckEntity truck = createTruck(truckId);
        BranchEntity branch = createBranch(branchId);
        GrainTypeEntity grainType = createGrainType(grainId);

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);

        TransportTransactionEntity tt = createOpenTransaction(
                ttId,
                truck,
                branch,
                grainType,
                start
        );

        Mockito.when(transportTransactionServiceMock.open(any(TransportTransactionRequestDto.class)))
                .thenReturn(tt);

        mockMvc.perform(post("/api/transport-transactions/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/transport-transactions/" + ttId))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ttId.toString()))
                .andExpect(jsonPath("$.truckId").value(truckId.toString()))
                .andExpect(jsonPath("$.branchId").value(branchId.toString()))
                .andExpect(jsonPath("$.grainTypeId").value(grainId.toString()))
                .andExpect(jsonPath("$.startTimestamp").value(org.hamcrest.Matchers.startsWith("2025-01-01T10:00")))
                .andExpect(jsonPath("$.endTimestamp").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/transport-transactions/open → 400 Bad Request quando body é inválido")
    void openTransportTransactionInvalidBodyReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                  "truckId": null,
                  "branchId": null,
                  "grainTypeId": null,
                  "appliedMargin": 0.123456
                }
                """;

        mockMvc.perform(post("/api/transport-transactions/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/transport-transactions/{id}/close → 200 OK com body correto")
    void closeTransportTransactionReturnsOk() throws Exception {
        UUID ttId = UUID.randomUUID();
        UUID truckId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID grainId = UUID.randomUUID();

        TruckEntity truck = createTruck(truckId);
        BranchEntity branch = createBranch(branchId);
        GrainTypeEntity grainType = createGrainType(grainId);

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime end   = LocalDateTime.of(2025, 1, 2, 12, 30);

        TransportTransactionEntity closed = createClosedTransaction(
                ttId,
                truck,
                branch,
                grainType,
                start,
                end
        );

        Mockito.when(transportTransactionServiceMock.close(eq(ttId)))
                .thenReturn(closed);

        mockMvc.perform(post("/api/transport-transactions/{id}/close", ttId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ttId.toString()))
                .andExpect(jsonPath("$.truckId").value(truckId.toString()))
                .andExpect(jsonPath("$.branchId").value(branchId.toString()))
                .andExpect(jsonPath("$.grainTypeId").value(grainId.toString()))
                .andExpect(jsonPath("$.startTimestamp").value(org.hamcrest.Matchers.startsWith("2025-01-01T10:00")))
                .andExpect(jsonPath("$.endTimestamp").value(org.hamcrest.Matchers.startsWith("2025-01-02T12:30")))
                .andExpect(jsonPath("$.totalNetWeightKg").value(350.000))
                .andExpect(jsonPath("$.totalLoadCost").value(186.00))
                .andExpect(jsonPath("$.totalEstimatedRevenue").value(196.00))
                .andExpect(jsonPath("$.estimatedProfit").value(10.00));
    }
}
