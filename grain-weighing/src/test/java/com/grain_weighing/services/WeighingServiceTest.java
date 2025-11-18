package com.grain_weighing.services;

import com.grain_weighing.config.StabilizationProperties;
import com.grain_weighing.dto.WeighingRequestDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeighingServiceTest {

    @Mock
    private StabilizationProperties properties;

    @Mock
    private ScaleRepository scaleRepository;

    @Mock
    private TruckRepository truckRepository;

    @Mock
    private TransportTransactionRepository transportTransactionRepository;

    @Mock
    private WeighingRepository weighingRepository;

    @InjectMocks
    private WeighingService weighingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(properties.getMaxDiffBetweenReadingsKg()).thenReturn(BigDecimal.valueOf(5));
        when(properties.getRequiredStableReadings()).thenReturn(3);
    }

    @Test
    void testFindByWeighingTimestampBetween() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        WeighingEntity weighing = WeighingEntity.builder().build();

        when(weighingRepository.findByWeighingTimestampBetween(start, end)).thenReturn(List.of(weighing));

        List<WeighingEntity> result = weighingService.findByWeighingTimestampBetween(start, end);

        assertEquals(1, result.size());
        verify(weighingRepository, times(1)).findByWeighingTimestampBetween(start, end);
    }

    @Test
    void testInsertRawWeighing_ScaleNotFound() {
        WeighingRequestDto request = new WeighingRequestDto("EXT123", "ABC123", BigDecimal.valueOf(1000));
        when(scaleRepository.findByExternalId(request.scaleExternalId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> weighingService.insertRawWeighing(request, "token"));

        assertEquals("Scale not found", exception.getMessage());
        verify(scaleRepository, times(1)).findByExternalId(request.scaleExternalId());
    }

    @Test
    void testInsertRawWeighing_ScaleDisabled() {
        ScaleEntity scale = ScaleEntity.builder().active(false).build();
        WeighingRequestDto request = new WeighingRequestDto("EXT123", "ABC123", BigDecimal.valueOf(1000));
        when(scaleRepository.findByExternalId(request.scaleExternalId())).thenReturn(Optional.of(scale));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> weighingService.insertRawWeighing(request, "token"));

        assertEquals("Scale is disabled", exception.getMessage());
        verify(scaleRepository, times(1)).findByExternalId(request.scaleExternalId());
    }

    @Test
    void testInsertRawWeighing_InvalidToken() {
        ScaleEntity scale = ScaleEntity.builder().active(true).apiToken("validToken").build();
        WeighingRequestDto request = new WeighingRequestDto("EXT123", "ABC123", BigDecimal.valueOf(1000));
        when(scaleRepository.findByExternalId(request.scaleExternalId())).thenReturn(Optional.of(scale));

        SecurityException exception = assertThrows(SecurityException.class, () -> weighingService.insertRawWeighing(request, "invalidToken"));

        assertEquals("Invalid scale token", exception.getMessage());
        verify(scaleRepository, times(1)).findByExternalId(request.scaleExternalId());
    }

    @Test
    void testInsertRawWeighing_TruckNotFound() {
        ScaleEntity scale = ScaleEntity.builder().active(true).apiToken("token").build();
        WeighingRequestDto request = new WeighingRequestDto("EXT123", "ABC123", BigDecimal.valueOf(1000));
        when(scaleRepository.findByExternalId(request.scaleExternalId())).thenReturn(Optional.of(scale));
        when(truckRepository.findByLicensePlate(request.licensePlate())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> weighingService.insertRawWeighing(request, "token"));

        assertEquals("Truck not found: ABC123", exception.getMessage());
        verify(scaleRepository, times(1)).findByExternalId(request.scaleExternalId());
        verify(truckRepository, times(1)).findByLicensePlate(request.licensePlate());
    }

    @Test
    void testInsertRawWeighing_WeightNotStabilized() {
        ScaleEntity scale = ScaleEntity.builder().active(true).apiToken("token").build();
        TruckEntity truck = TruckEntity.builder().licensePlate("ABC123").build();
        WeighingRequestDto request = new WeighingRequestDto("EXT123", "ABC123", BigDecimal.valueOf(1000));
        when(scaleRepository.findByExternalId(request.scaleExternalId())).thenReturn(Optional.of(scale));
        when(truckRepository.findByLicensePlate(request.licensePlate())).thenReturn(Optional.of(truck));

        Optional<WeighingEntity> result = weighingService.insertRawWeighing(request, "token");

        assertTrue(result.isEmpty());
        verify(scaleRepository, times(1)).findByExternalId(request.scaleExternalId());
        verify(truckRepository, times(1)).findByLicensePlate(request.licensePlate());
    }

    @Test
    void testInsertRawWeighing_WeightStabilized() {
        ScaleEntity scale = ScaleEntity.builder().active(true).apiToken("token").build();
        TruckEntity truck = TruckEntity.builder().licensePlate("ABC123").tareWeightKg(BigDecimal.valueOf(500)).build();
        GrainTypeEntity grainType = GrainTypeEntity.builder().purchasePricePerTon(BigDecimal.valueOf(100)).build();
        TransportTransactionEntity transaction = TransportTransactionEntity.builder().grainType(grainType).build();
        WeighingRequestDto request = new WeighingRequestDto("EXT123", "ABC123", BigDecimal.valueOf(1500));
        when(scaleRepository.findByExternalId(request.scaleExternalId())).thenReturn(Optional.of(scale));
        when(truckRepository.findByLicensePlate(request.licensePlate())).thenReturn(Optional.of(truck));
        when(transportTransactionRepository.findFirstByTruckAndEndTimestampIsNullOrderByStartTimestampDesc(truck)).thenReturn(Optional.of(transaction));
        when(weighingRepository.save(any(WeighingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        weighingService.insertRawWeighing(request, "token");
        weighingService.insertRawWeighing(request, "token");
        Optional<WeighingEntity> result = weighingService.insertRawWeighing(request, "token");

        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(1000), result.get().getNetWeightKg());
        verify(weighingRepository, times(1)).save(any(WeighingEntity.class));
    }
}