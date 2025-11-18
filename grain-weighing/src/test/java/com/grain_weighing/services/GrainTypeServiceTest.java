package com.grain_weighing.services;

import com.grain_weighing.dto.GrainTypeRequestDto;
import com.grain_weighing.entities.GrainTypeEntity;
import com.grain_weighing.repositories.GrainTypeRepository;
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

class GrainTypeServiceTest {

    @Mock
    private GrainTypeRepository grainTypeRepository;

    @InjectMocks
    private GrainTypeService grainTypeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        List<GrainTypeEntity> grainTypes = List.of(new GrainTypeEntity(), new GrainTypeEntity());
        when(grainTypeRepository.findAll()).thenReturn(grainTypes);

        List<GrainTypeEntity> result = grainTypeService.findAll();

        assertEquals(2, result.size());
        verify(grainTypeRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        GrainTypeEntity grainType = GrainTypeEntity.builder().id(id).build();
        when(grainTypeRepository.findById(id)).thenReturn(Optional.of(grainType));

        Optional<GrainTypeEntity> result = grainTypeService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(grainTypeRepository, times(1)).findById(id);
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(grainTypeRepository.findById(id)).thenReturn(Optional.empty());

        Optional<GrainTypeEntity> result = grainTypeService.findById(id);

        assertFalse(result.isPresent());
        verify(grainTypeRepository, times(1)).findById(id);
    }

    @Test
    void testCreate() {
        GrainTypeRequestDto request = new GrainTypeRequestDto("Wheat", BigDecimal.valueOf(100), BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.2));
        GrainTypeEntity grainType = GrainTypeEntity.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .purchasePricePerTon(request.purchasePricePerTon())
                .minMargin(request.minMargin())
                .maxMargin(request.maxMargin())
                .build();

        when(grainTypeRepository.save(any(GrainTypeEntity.class))).thenReturn(grainType);

        GrainTypeEntity result = grainTypeService.create(request);

        assertNotNull(result);
        assertEquals(request.name(), result.getName());
        verify(grainTypeRepository, times(1)).save(any(GrainTypeEntity.class));
    }

    @Test
    void testCreate_InvalidMargins() {
        GrainTypeRequestDto request = new GrainTypeRequestDto("Wheat", BigDecimal.valueOf(100), BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.2));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> grainTypeService.create(request));

        assertEquals("minMargin cannot be greater than maxMargin", exception.getMessage());
        verify(grainTypeRepository, never()).save(any(GrainTypeEntity.class));
    }

    @Test
    void testUpdate() {
        UUID id = UUID.randomUUID();
        GrainTypeRequestDto request = new GrainTypeRequestDto("Updated Wheat", BigDecimal.valueOf(120), BigDecimal.valueOf(0.15), BigDecimal.valueOf(0.25));
        GrainTypeEntity existingGrainType = GrainTypeEntity.builder().id(id).build();

        when(grainTypeRepository.findById(id)).thenReturn(Optional.of(existingGrainType));
        when(grainTypeRepository.save(any(GrainTypeEntity.class))).thenReturn(existingGrainType);

        GrainTypeEntity result = grainTypeService.update(id, request);

        assertNotNull(result);
        assertEquals(request.name(), result.getName());
        assertEquals(request.purchasePricePerTon(), result.getPurchasePricePerTon());
        verify(grainTypeRepository, times(1)).findById(id);
        verify(grainTypeRepository, times(1)).save(existingGrainType);
    }

    @Test
    void testUpdate_InvalidMargins() {
        UUID id = UUID.randomUUID();
        GrainTypeRequestDto request = new GrainTypeRequestDto("Updated Wheat", BigDecimal.valueOf(120), BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.2));
        GrainTypeEntity existingGrainType = GrainTypeEntity.builder().id(id).build();

        when(grainTypeRepository.findById(id)).thenReturn(Optional.of(existingGrainType));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> grainTypeService.update(id, request));

        assertEquals("minMargin cannot be greater than maxMargin", exception.getMessage());
        verify(grainTypeRepository, times(1)).findById(id);
        verify(grainTypeRepository, never()).save(any(GrainTypeEntity.class));
    }

    @Test
    void testUpdate_GrainTypeNotFound() {
        UUID id = UUID.randomUUID();
        GrainTypeRequestDto request = new GrainTypeRequestDto("Updated Wheat", BigDecimal.valueOf(120), BigDecimal.valueOf(0.15), BigDecimal.valueOf(0.25));

        when(grainTypeRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> grainTypeService.update(id, request));

        assertEquals("Grain type not found", exception.getMessage());
        verify(grainTypeRepository, times(1)).findById(id);
        verify(grainTypeRepository, never()).save(any(GrainTypeEntity.class));
    }
}