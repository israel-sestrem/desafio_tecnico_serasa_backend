package com.grain_weighing.services;

import com.grain_weighing.dto.TruckRequestDto;
import com.grain_weighing.entities.TruckEntity;
import com.grain_weighing.repositories.TruckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

class TruckServiceTest {

    @Mock
    private TruckRepository truckRepository;

    @InjectMocks
    private TruckService truckService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        List<TruckEntity> trucks = List.of(new TruckEntity(), new TruckEntity());
        when(truckRepository.findAll()).thenReturn(trucks);

        List<TruckEntity> result = truckService.findAll();

        assertEquals(2, result.size());
        verify(truckRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        TruckEntity truck = TruckEntity.builder().id(id).build();
        when(truckRepository.findById(id)).thenReturn(Optional.of(truck));

        Optional<TruckEntity> result = truckService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(truckRepository, times(1)).findById(id);
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(truckRepository.findById(id)).thenReturn(Optional.empty());

        Optional<TruckEntity> result = truckService.findById(id);

        assertFalse(result.isPresent());
        verify(truckRepository, times(1)).findById(id);
    }

    @Test
    void testCreate() {
        TruckRequestDto request = new TruckRequestDto("ABC123", BigDecimal.valueOf(5000), "Model X", true);
        TruckEntity truck = TruckEntity.builder()
                .id(UUID.randomUUID())
                .licensePlate(request.licensePlate())
                .tareWeightKg(request.tareWeightKg())
                .model(request.model())
                .active(request.active())
                .build();

        when(truckRepository.save(any(TruckEntity.class))).thenReturn(truck);

        TruckEntity result = truckService.create(request);

        assertNotNull(result);
        assertEquals(request.licensePlate(), result.getLicensePlate());
        verify(truckRepository, times(1)).save(any(TruckEntity.class));
    }

    @Test
    void testUpdate() {
        UUID id = UUID.randomUUID();
        TruckRequestDto request = new TruckRequestDto("XYZ789", BigDecimal.valueOf(6000), "Model Y", false);
        TruckEntity existingTruck = TruckEntity.builder().id(id).build();

        when(truckRepository.findById(id)).thenReturn(Optional.of(existingTruck));
        when(truckRepository.save(any(TruckEntity.class))).thenReturn(existingTruck);

        TruckEntity result = truckService.update(id, request);

        assertNotNull(result);
        assertEquals(request.licensePlate(), result.getLicensePlate());
        assertEquals(request.tareWeightKg(), result.getTareWeightKg());
        verify(truckRepository, times(1)).findById(id);
        verify(truckRepository, times(1)).save(existingTruck);
    }

    @Test
    void testUpdate_TruckNotFound() {
        UUID id = UUID.randomUUID();
        TruckRequestDto request = new TruckRequestDto("XYZ789", BigDecimal.valueOf(6000), "Model Y", false);

        when(truckRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> truckService.update(id, request));

        assertEquals("Truck not found", exception.getMessage());
        verify(truckRepository, times(1)).findById(id);
        verify(truckRepository, never()).save(any(TruckEntity.class));
    }
}