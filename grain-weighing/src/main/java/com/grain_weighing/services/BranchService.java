package com.grain_weighing.services;

import com.grain_weighing.dto.BranchRequestDto;
import com.grain_weighing.entities.BranchEntity;
import com.grain_weighing.repositories.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    public List<BranchEntity> findAll() {
        return branchRepository.findAll();
    }

    public Optional<BranchEntity> findById(UUID id) {
        return branchRepository.findById(id);
    }

    public BranchEntity create(BranchRequestDto request){
        BranchEntity branch = BranchEntity.builder()
                .code(request.code())
                .name(request.name())
                .city(request.city())
                .state(request.state())
                .build();

        return branchRepository.save(branch);
    }

    public BranchEntity update(UUID id, BranchRequestDto request) {
        BranchEntity branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        branch.setCode(request.code());
        branch.setName(request.name());
        branch.setCity(request.city());
        branch.setState(request.state());

        return branchRepository.save(branch);
    }


}
