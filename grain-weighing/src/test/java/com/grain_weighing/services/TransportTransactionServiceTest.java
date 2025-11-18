package com.grain_weighing.services;

import com.grain_weighing.dto.TransportTransactionRequestDto;
import com.grain_weighing.entities.*;
import com.grain_weighing.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransportTransactionServiceTest {

    @Mock
    private TruckRepository truckRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private GrainTypeRepository grainTypeRepository;

    @Mock
    private WeighingRepository weighingRepository;

    @Mock
    private TransportTransactionRepository transportTransactionRepository;

    @InjectMocks
    private TransportTransactionService transportTransactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testOpen_ValidRequest() {
        UUID truckId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID grainTypeId = UUID.randomUUID();
        BigDecimal appliedMargin = BigDecimal.valueOf(0.15);

        TruckEntity truck = TruckEntity.builder().id(truckId).build();
        BranchEntity branch = BranchEntity.builder().id(branchId).build();
        GrainTypeEntity grainType = GrainTypeEntity.builder()
                .id(grainTypeId)
                .purchasePricePerTon(BigDecimal.valueOf(100))
                .minMargin(BigDecimal.valueOf(0.1))
                .maxMargin(BigDecimal.valueOf(0.2))
                .build();

        TransportTransactionRequestDto request = new TransportTransactionRequestDto(truckId, branchId, grainTypeId, appliedMargin);

        when(truckRepository.findById(truckId)).thenReturn(Optional.of(truck));
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(grainTypeRepository.findById(grainTypeId)).thenReturn(Optional.of(grainType));
        when(transportTransactionRepository.save(any(TransportTransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransportTransactionEntity result = transportTransactionService.open(request);

        assertNotNull(result);
        assertEquals(appliedMargin, result.getAppliedMargin());
        assertEquals(BigDecimal.valueOf(115).setScale(2), result.getSalePricePerTon());
        verify(truckRepository, times(1)).findById(truckId);
        verify(branchRepository, times(1)).findById(branchId);
        verify(grainTypeRepository, times(1)).findById(grainTypeId);
        verify(transportTransactionRepository, times(1)).save(any(TransportTransactionEntity.class));
    }

    @Test
    void testOpen_TruckNotFound() {
        UUID truckId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID grainTypeId = UUID.randomUUID();

        TransportTransactionRequestDto request = new TransportTransactionRequestDto(truckId, branchId, grainTypeId, BigDecimal.valueOf(0.15));

        when(truckRepository.findById(truckId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transportTransactionService.open(request));

        assertEquals("Truck not found: " + truckId, exception.getMessage());
        verify(truckRepository, times(1)).findById(truckId);
        verify(branchRepository, never()).findById(any());
        verify(grainTypeRepository, never()).findById(any());
        verify(transportTransactionRepository, never()).save(any());
    }

    @Test
    void testClose_ValidTransaction() {
        UUID transactionId = UUID.randomUUID();
        TransportTransactionEntity transaction = TransportTransactionEntity.builder()
                .id(transactionId)
                .purchasePricePerTon(BigDecimal.valueOf(100))
                .salePricePerTon(BigDecimal.valueOf(150))
                .build();

        WeighingEntity weighing1 = WeighingEntity.builder().netWeightKg(BigDecimal.valueOf(2000)).build();
        WeighingEntity weighing2 = WeighingEntity.builder().netWeightKg(BigDecimal.valueOf(3000)).build();

        when(transportTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(weighingRepository.findByTransportTransaction(transaction)).thenReturn(List.of(weighing1, weighing2));
        when(transportTransactionRepository.save(any(TransportTransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransportTransactionEntity result = transportTransactionService.close(transactionId);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(5000), result.getTotalNetWeightKg());
        assertEquals(BigDecimal.valueOf(500).setScale(2), result.getTotalLoadCost());
        assertEquals(BigDecimal.valueOf(750).setScale(2), result.getTotalEstimatedRevenue());
        assertEquals(BigDecimal.valueOf(250).setScale(2), result.getEstimatedProfit());
        verify(transportTransactionRepository, times(1)).findById(transactionId);
        verify(weighingRepository, times(1)).findByTransportTransaction(transaction);
        verify(transportTransactionRepository, times(1)).save(transaction);
    }

    @Test
    void testClose_TransactionNotFound() {
        UUID transactionId = UUID.randomUUID();

        when(transportTransactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transportTransactionService.close(transactionId));

        assertEquals("Transport transaction not found: " + transactionId, exception.getMessage());
        verify(transportTransactionRepository, times(1)).findById(transactionId);
        verify(weighingRepository, never()).findByTransportTransaction(any());
        verify(transportTransactionRepository, never()).save(any());
    }
}