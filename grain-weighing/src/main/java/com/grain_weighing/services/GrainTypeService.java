package com.grain_weighing.services;

import com.grain_weighing.dto.GrainTypeRequestDto;
import com.grain_weighing.entities.GrainTypeEntity;
import com.grain_weighing.repositories.GrainTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrainTypeService {

    private final GrainTypeRepository grainTypeRepository;

    public List<GrainTypeEntity> findAll() {
        return grainTypeRepository.findAll();
    }

    public Optional<GrainTypeEntity> findById(UUID id) {
        return grainTypeRepository.findById(id);
    }

    public GrainTypeEntity create(GrainTypeRequestDto request){
        if (request.minMargin().compareTo(request.maxMargin()) > 0) {
            throw new IllegalArgumentException("minMargin cannot be greater than maxMargin");
        }

        GrainTypeEntity gt = GrainTypeEntity.builder()
                .name(request.name())
                .purchasePricePerTon(request.purchasePricePerTon())
                .minMargin(request.minMargin())
                .maxMargin(request.maxMargin())
                .build();

        return grainTypeRepository.save(gt);
    }

    public GrainTypeEntity update(UUID id, GrainTypeRequestDto request) {
        GrainTypeEntity grainType = grainTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grain type not found"));

        if (request.minMargin().compareTo(request.maxMargin()) > 0) {
            throw new IllegalArgumentException("minMargin cannot be greater than maxMargin");
        }

        grainType.setName(request.name());
        grainType.setPurchasePricePerTon(request.purchasePricePerTon());
        grainType.setMinMargin(request.minMargin());
        grainType.setMaxMargin(request.maxMargin());

        return grainTypeRepository.save(grainType);
    }

}
