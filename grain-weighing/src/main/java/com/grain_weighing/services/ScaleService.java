package com.grain_weighing.services;

import com.grain_weighing.dto.ScaleRequestDto;
import com.grain_weighing.entities.BranchEntity;
import com.grain_weighing.entities.ScaleEntity;
import com.grain_weighing.repositories.BranchRepository;
import com.grain_weighing.repositories.ScaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScaleService {

    private final BranchRepository branchRepository;
    private final ScaleRepository scaleRepository;

    public List<ScaleEntity> findAll() {
        return scaleRepository.findAll();
    }

    public Optional<ScaleEntity> findById(UUID id) {
        return scaleRepository.findById(id);
    }

    public ScaleEntity create(ScaleRequestDto request){
        BranchEntity branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + request.branchId()));

        Optional<ScaleEntity> existingScale = scaleRepository.findByExternalId(request.externalId());
        if(existingScale.isPresent()){
            throw new IllegalArgumentException("Scale with external ID already exists: " + request.externalId());
        }

        ScaleEntity scale = ScaleEntity.builder()
                .externalId(request.externalId())
                .description(request.description())
                .branch(branch)
                .apiToken(request.apiToken())
                .active(true)
                .build();

        return scaleRepository.save(scale);
    }

    public ScaleEntity update(UUID id, ScaleRequestDto request) {
        ScaleEntity scale = scaleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scale not found"));

        BranchEntity branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        Optional<ScaleEntity> existingScale = scaleRepository.findByExternalId(request.externalId());
        if(existingScale.isPresent() && !existingScale.get().getId().equals(id)){
            throw new IllegalArgumentException("Scale with external ID already exists: " + request.externalId());
        }

        scale.setExternalId(request.externalId());
        scale.setDescription(request.description());
        scale.setBranch(branch);
        scale.setApiToken(request.apiToken());
        scale.setActive(request.active());

        return scaleRepository.save(scale);
    }

}
