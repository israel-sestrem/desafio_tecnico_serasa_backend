package com.grain_weighing.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grain_weighing.dto.WeighingSummaryResponseDto;
import com.grain_weighing.entities.BranchEntity;
import com.grain_weighing.entities.GrainTypeEntity;
import com.grain_weighing.entities.ScaleEntity;
import com.grain_weighing.entities.TruckEntity;
import com.grain_weighing.entities.WeighingEntity;
import com.grain_weighing.services.ReportService;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class)
@Import(ReportControllerMockConfig.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WeighingService weighingServiceMock;

    @Autowired
    private ReportService reportServiceMock;

    private BranchEntity createBranch(UUID id) {
        return BranchEntity.builder()
                .id(id)
                .code("BR001")
                .name("Branch Test")
                .city("Blumenau")
                .state("SC")
                .build();
    }

    private ScaleEntity createScale(UUID id, BranchEntity branch, String externalId) {
        return ScaleEntity.builder()
                .id(id)
                .branch(branch)
                .externalId(externalId)
                .description("Scale desc")
                .apiToken("token-123")
                .active(true)
                .build();
    }

    private TruckEntity createTruck(UUID id, String plate) {
        return TruckEntity.builder()
                .id(id)
                .licensePlate(plate)
                .tareWeightKg(new BigDecimal("12000"))
                .model("Truck model")
                .active(true)
                .build();
    }

    private GrainTypeEntity createGrainType(UUID id, String name) {
        return GrainTypeEntity.builder()
                .id(id)
                .name(name)
                .purchasePricePerTon(new BigDecimal("1000"))
                .minMargin(new BigDecimal("0.05"))
                .maxMargin(new BigDecimal("0.15"))
                .build();
    }

    private WeighingEntity createWeighing(UUID id,
                                          ScaleEntity scale,
                                          TruckEntity truck,
                                          GrainTypeEntity grainType) {
        return WeighingEntity.builder()
                .id(id)
                .scale(scale)
                .truck(truck)
                .grainType(grainType)
                .build();
    }

    @Test
    @DisplayName("GET /api/reports/weighings → 200 OK e lista com todos os weighings sem filtros")
    void listWeighingsWithoutFiltersReturnsAll() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end   = LocalDateTime.of(2025, 1, 31, 23, 59);

        UUID branchId1 = UUID.randomUUID();
        UUID branchId2 = UUID.randomUUID();
        UUID truckId1  = UUID.randomUUID();
        UUID truckId2  = UUID.randomUUID();
        UUID grainId1  = UUID.randomUUID();
        UUID grainId2  = UUID.randomUUID();

        BranchEntity b1 = createBranch(branchId1);
        BranchEntity b2 = createBranch(branchId2);

        ScaleEntity s1 = createScale(UUID.randomUUID(), b1, "SCALE-1");
        ScaleEntity s2 = createScale(UUID.randomUUID(), b2, "SCALE-2");

        TruckEntity t1 = createTruck(truckId1, "ABC1D23");
        TruckEntity t2 = createTruck(truckId2, "DEF4G56");

        GrainTypeEntity g1 = createGrainType(grainId1, "Soybean");
        GrainTypeEntity g2 = createGrainType(grainId2, "Corn");

        WeighingEntity w1 = createWeighing(UUID.randomUUID(), s1, t1, g1);
        WeighingEntity w2 = createWeighing(UUID.randomUUID(), s2, t2, g2);

        Mockito.when(weighingServiceMock.findByWeighingTimestampBetween(start, end))
                .thenReturn(List.of(w1, w2));

        mockMvc.perform(get("/api/reports/weighings")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/reports/weighings → 200 OK e respeita filtros de branch, truck e grainType")
    void listWeighingsWithFiltersAppliesFiltering() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end   = LocalDateTime.of(2025, 1, 31, 23, 59);

        UUID branchId1 = UUID.randomUUID();
        UUID branchId2 = UUID.randomUUID();
        UUID truckId1  = UUID.randomUUID();
        UUID truckId2  = UUID.randomUUID();
        UUID grainId1  = UUID.randomUUID();
        UUID grainId2  = UUID.randomUUID();

        BranchEntity b1 = createBranch(branchId1);
        BranchEntity b2 = createBranch(branchId2);

        ScaleEntity s1 = createScale(UUID.randomUUID(), b1, "SCALE-1");
        ScaleEntity s2 = createScale(UUID.randomUUID(), b2, "SCALE-2");

        TruckEntity t1 = createTruck(truckId1, "ABC1D23");
        TruckEntity t2 = createTruck(truckId2, "DEF4G56");

        GrainTypeEntity g1 = createGrainType(grainId1, "Soybean");
        GrainTypeEntity g2 = createGrainType(grainId2, "Corn");

        WeighingEntity w1 = createWeighing(UUID.randomUUID(), s1, t1, g1);
        WeighingEntity w2 = createWeighing(UUID.randomUUID(), s2, t2, g2);

        Mockito.when(weighingServiceMock.findByWeighingTimestampBetween(start, end))
                .thenReturn(List.of(w1, w2));

        mockMvc.perform(get("/api/reports/weighings")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("branchId", branchId1.toString())
                        .param("truckId", truckId1.toString())
                        .param("grainTypeId", grainId1.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/reports/summary → 200 OK com resumo retornado pelo service")
    void summaryReturnsOkWithBody() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end   = LocalDateTime.of(2025, 1, 31, 23, 59);

        WeighingSummaryResponseDto summary = new WeighingSummaryResponseDto(
                new BigDecimal("350"),
                new BigDecimal("186"),
                new BigDecimal("196"),
                new BigDecimal("10")
        );

        Mockito.when(reportServiceMock.summarize(eq(start), eq(end),
                        isNull(), isNull(), isNull()))
                .thenReturn(summary);

        mockMvc.perform(get("/api/reports/summary")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalNetWeightKg").value(350))
                .andExpect(jsonPath("$.totalLoadCost").value(186))
                .andExpect(jsonPath("$.estimatedRevenue").value(196))
                .andExpect(jsonPath("$.estimatedProfit").value(10));

        Mockito.verify(reportServiceMock)
                .summarize(eq(start), eq(end), isNull(), isNull(), isNull());
    }
}
