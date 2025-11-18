package com.grain_weighing.services;

import com.grain_weighing.dto.ScaleRequestDto;
import com.grain_weighing.entities.BranchEntity;
import com.grain_weighing.entities.ScaleEntity;
import com.grain_weighing.repositories.BranchRepository;
import com.grain_weighing.repositories.ScaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScaleServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ScaleRepository scaleRepository;

    @InjectMocks
    private ScaleService scaleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        List<ScaleEntity> scales = List.of(new ScaleEntity(), new ScaleEntity());
        when(scaleRepository.findAll()).thenReturn(scales);

        List<ScaleEntity> result = scaleService.findAll();

        assertEquals(2, result.size());
        verify(scaleRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        ScaleEntity scale = ScaleEntity.builder().id(id).build();
        when(scaleRepository.findById(id)).thenReturn(Optional.of(scale));

        Optional<ScaleEntity> result = scaleService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(scaleRepository, times(1)).findById(id);
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(scaleRepository.findById(id)).thenReturn(Optional.empty());

        Optional<ScaleEntity> result = scaleService.findById(id);

        assertFalse(result.isPresent());
        verify(scaleRepository, times(1)).findById(id);
    }

    @Test
    void testCreate() {
        UUID branchId = UUID.randomUUID();
        ScaleRequestDto request = new ScaleRequestDto(branchId, "EXT123", "Scale A", "token123", true);
        BranchEntity branch = BranchEntity.builder().id(branchId).build();
        ScaleEntity scale = ScaleEntity.builder()
                .id(UUID.randomUUID())
                .externalId(request.externalId())
                .description(request.description())
                .branch(branch)
                .apiToken(request.apiToken())
                .active(request.active())
                .build();

        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(scaleRepository.findByExternalId(request.externalId())).thenReturn(Optional.empty());
        when(scaleRepository.save(any(ScaleEntity.class))).thenReturn(scale);

        ScaleEntity result = scaleService.create(request);

        assertNotNull(result);
        assertEquals(request.externalId(), result.getExternalId());
        verify(branchRepository, times(1)).findById(branchId);
        verify(scaleRepository, times(1)).findByExternalId(request.externalId());
        verify(scaleRepository, times(1)).save(any(ScaleEntity.class));
    }

    @Test
    void testCreate_BranchNotFound() {
        UUID branchId = UUID.randomUUID();
        ScaleRequestDto request = new ScaleRequestDto(branchId, "EXT123", "Scale A", "token123", true);

        when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> scaleService.create(request));

        assertEquals("Branch not found: " + branchId, exception.getMessage());
        verify(branchRepository, times(1)).findById(branchId);
        verify(scaleRepository, never()).save(any(ScaleEntity.class));
    }

    @Test
    void testCreate_DuplicateExternalId() {
        UUID branchId = UUID.randomUUID();
        ScaleRequestDto request = new ScaleRequestDto(branchId, "EXT123", "Scale A", "token123", true);
        BranchEntity branch = BranchEntity.builder().id(branchId).build();
        ScaleEntity existingScale = ScaleEntity.builder().externalId(request.externalId()).build();

        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(scaleRepository.findByExternalId(request.externalId())).thenReturn(Optional.of(existingScale));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> scaleService.create(request));

        assertEquals("Scale with external ID already exists: " + request.externalId(), exception.getMessage());
        verify(branchRepository, times(1)).findById(branchId);
        verify(scaleRepository, times(1)).findByExternalId(request.externalId());
        verify(scaleRepository, never()).save(any(ScaleEntity.class));
    }

    @Test
    void testUpdate() {
        UUID id = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        ScaleRequestDto request = new ScaleRequestDto(branchId, "EXT123", "Updated Scale", "newToken", false);
        BranchEntity branch = BranchEntity.builder().id(branchId).build();
        ScaleEntity existingScale = ScaleEntity.builder().id(id).externalId("OLD123").build();

        when(scaleRepository.findById(id)).thenReturn(Optional.of(existingScale));
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(scaleRepository.findByExternalId(request.externalId())).thenReturn(Optional.empty());
        when(scaleRepository.save(any(ScaleEntity.class))).thenReturn(existingScale);

        ScaleEntity result = scaleService.update(id, request);

        assertNotNull(result);
        assertEquals(request.externalId(), result.getExternalId());
        assertEquals(request.description(), result.getDescription());
        verify(scaleRepository, times(1)).findById(id);
        verify(branchRepository, times(1)).findById(branchId);
        verify(scaleRepository, times(1)).findByExternalId(request.externalId());
        verify(scaleRepository, times(1)).save(existingScale);
    }

    @Test
    void testUpdate_ScaleNotFound() {
        UUID id = UUID.randomUUID();
        ScaleRequestDto request = new ScaleRequestDto(UUID.randomUUID(), "EXT123", "Updated Scale", "newToken", false);

        when(scaleRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> scaleService.update(id, request));

        assertEquals("Scale not found", exception.getMessage());
        verify(scaleRepository, times(1)).findById(id);
        verify(branchRepository, never()).findById(any());
        verify(scaleRepository, never()).save(any(ScaleEntity.class));
    }

    @Test
    void testUpdate_BranchNotFound() {
        UUID id = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        ScaleRequestDto request = new ScaleRequestDto(branchId, "EXT123", "Updated Scale", "newToken", false);
        ScaleEntity existingScale = ScaleEntity.builder().id(id).build();

        when(scaleRepository.findById(id)).thenReturn(Optional.of(existingScale));
        when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> scaleService.update(id, request));

        assertEquals("Branch not found", exception.getMessage());
        verify(scaleRepository, times(1)).findById(id);
        verify(branchRepository, times(1)).findById(branchId);
        verify(scaleRepository, never()).save(any(ScaleEntity.class));
    }

    @Test
    void testUpdate_DuplicateExternalId() {
        UUID id = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        ScaleRequestDto request = new ScaleRequestDto(branchId, "EXT123", "Updated Scale", "newToken", false);
        BranchEntity branch = BranchEntity.builder().id(branchId).build();
        ScaleEntity existingScale = ScaleEntity.builder().id(id).externalId("OLD123").build();
        ScaleEntity duplicateScale = ScaleEntity.builder().id(UUID.randomUUID()).externalId(request.externalId()).build();

        when(scaleRepository.findById(id)).thenReturn(Optional.of(existingScale));
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(scaleRepository.findByExternalId(request.externalId())).thenReturn(Optional.of(duplicateScale));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> scaleService.update(id, request));

        assertEquals("Scale with external ID already exists: " + request.externalId(), exception.getMessage());
        verify(scaleRepository, times(1)).findById(id);
        verify(branchRepository, times(1)).findById(branchId);
        verify(scaleRepository, times(1)).findByExternalId(request.externalId());
        verify(scaleRepository, never()).save(any(ScaleEntity.class));
    }
}