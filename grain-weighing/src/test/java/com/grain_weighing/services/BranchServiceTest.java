package com.grain_weighing.services;

import com.grain_weighing.dto.BranchRequestDto;
import com.grain_weighing.entities.BranchEntity;
import com.grain_weighing.repositories.BranchRepository;
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

class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BranchService branchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        List<BranchEntity> branches = List.of(new BranchEntity(), new BranchEntity());
        when(branchRepository.findAll()).thenReturn(branches);

        List<BranchEntity> result = branchService.findAll();

        assertEquals(2, result.size());
        verify(branchRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        BranchEntity branch = BranchEntity.builder().id(id).build();
        when(branchRepository.findById(id)).thenReturn(Optional.of(branch));

        Optional<BranchEntity> result = branchService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(branchRepository, times(1)).findById(id);
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(branchRepository.findById(id)).thenReturn(Optional.empty());

        Optional<BranchEntity> result = branchService.findById(id);

        assertFalse(result.isPresent());
        verify(branchRepository, times(1)).findById(id);
    }

    @Test
    void testCreate() {
        BranchRequestDto request = new BranchRequestDto("001", "Branch A", "City A", "State A");
        BranchEntity branch = BranchEntity.builder()
                .id(UUID.randomUUID())
                .code(request.code())
                .name(request.name())
                .city(request.city())
                .state(request.state())
                .build();

        when(branchRepository.save(any(BranchEntity.class))).thenReturn(branch);

        BranchEntity result = branchService.create(request);

        assertNotNull(result);
        assertEquals(request.code(), result.getCode());
        verify(branchRepository, times(1)).save(any(BranchEntity.class));
    }

    @Test
    void testUpdate() {
        UUID id = UUID.randomUUID();
        BranchRequestDto request = new BranchRequestDto("002", "Updated Branch", "Updated City", "Updated State");
        BranchEntity existingBranch = BranchEntity.builder().id(id).build();

        when(branchRepository.findById(id)).thenReturn(Optional.of(existingBranch));
        when(branchRepository.save(any(BranchEntity.class))).thenReturn(existingBranch);

        BranchEntity result = branchService.update(id, request);

        assertNotNull(result);
        assertEquals(request.code(), result.getCode());
        assertEquals(request.name(), result.getName());
        verify(branchRepository, times(1)).findById(id);
        verify(branchRepository, times(1)).save(existingBranch);
    }

    @Test
    void testUpdate_BranchNotFound() {
        UUID id = UUID.randomUUID();
        BranchRequestDto request = new BranchRequestDto("002", "Updated Branch", "Updated City", "Updated State");

        when(branchRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> branchService.update(id, request));

        assertEquals("Branch not found", exception.getMessage());
        verify(branchRepository, times(1)).findById(id);
        verify(branchRepository, never()).save(any(BranchEntity.class));
    }
}