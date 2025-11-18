package com.grain_weighing.services;

import com.grain_weighing.dto.WeighingSummaryResponseDto;
import com.grain_weighing.entities.*;
import com.grain_weighing.repositories.WeighingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private WeighingRepository weighingRepository;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSummarize() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        UUID branchId = UUID.randomUUID();
        UUID truckId = UUID.randomUUID();
        UUID grainTypeId = UUID.randomUUID();

        BranchEntity branch = BranchEntity.builder().id(branchId).build();
        TruckEntity truck = TruckEntity.builder().id(truckId).build();
        GrainTypeEntity grainType = GrainTypeEntity.builder().id(grainTypeId).build();

        TransportTransactionEntity transaction = TransportTransactionEntity.builder()
                .purchasePricePerTon(BigDecimal.valueOf(100))
                .salePricePerTon(BigDecimal.valueOf(150))
                .build();

        ScaleEntity scale = ScaleEntity.builder().branch(branch).build();

        WeighingEntity weighing1 = WeighingEntity.builder()
                .netWeightKg(BigDecimal.valueOf(2000))
                .scale(scale)
                .truck(truck)
                .grainType(grainType)
                .transportTransaction(transaction)
                .build();

        WeighingEntity weighing2 = WeighingEntity.builder()
                .netWeightKg(BigDecimal.valueOf(3000))
                .scale(scale)
                .truck(truck)
                .grainType(grainType)
                .transportTransaction(transaction)
                .build();

        when(weighingRepository.findByWeighingTimestampBetween(start, end)).thenReturn(List.of(weighing1, weighing2));

        WeighingSummaryResponseDto result = reportService.summarize(start, end, branchId, truckId, grainTypeId);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(5000), result.totalNetWeightKg());
        assertEquals(BigDecimal.valueOf(500).setScale(2), result.totalLoadCost());
        assertEquals(BigDecimal.valueOf(750).setScale(2), result.estimatedRevenue());
        assertEquals(BigDecimal.valueOf(250).setScale(2), result.estimatedProfit());

        verify(weighingRepository, times(1)).findByWeighingTimestampBetween(start, end);
    }

    @Test
    void testSummarize_NoFilters() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        WeighingEntity weighing = WeighingEntity.builder()
                .netWeightKg(BigDecimal.valueOf(1000))
                .transportTransaction(TransportTransactionEntity.builder()
                        .purchasePricePerTon(BigDecimal.valueOf(80))
                        .salePricePerTon(BigDecimal.valueOf(120))
                        .build())
                .build();

        when(weighingRepository.findByWeighingTimestampBetween(start, end)).thenReturn(List.of(weighing));

        WeighingSummaryResponseDto result = reportService.summarize(start, end, null, null, null);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1000), result.totalNetWeightKg());
        assertEquals(BigDecimal.valueOf(80).setScale(2), result.totalLoadCost());
        assertEquals(BigDecimal.valueOf(120).setScale(2), result.estimatedRevenue());
        assertEquals(BigDecimal.valueOf(40).setScale(2), result.estimatedProfit());

        verify(weighingRepository, times(1)).findByWeighingTimestampBetween(start, end);
    }

    @Test
    void testSummarize_EmptyResults() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        when(weighingRepository.findByWeighingTimestampBetween(start, end)).thenReturn(List.of());

        WeighingSummaryResponseDto result = reportService.summarize(start, end, null, null, null);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.totalNetWeightKg());
        assertEquals(BigDecimal.ZERO.setScale(2), result.totalLoadCost());
        assertEquals(BigDecimal.ZERO.setScale(2), result.estimatedRevenue());
        assertEquals(BigDecimal.ZERO.setScale(2), result.estimatedProfit());

        verify(weighingRepository, times(1)).findByWeighingTimestampBetween(start, end);
    }
}