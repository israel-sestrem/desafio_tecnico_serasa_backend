package com.grain_weighing.controllers;

import com.grain_weighing.dto.BranchRequestDto;
import com.grain_weighing.dto.BranchResponseDto;
import com.grain_weighing.entities.BranchEntity;
import com.grain_weighing.repositories.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchRepository branchRepository;

    @PostMapping
    public ResponseEntity<BranchResponseDto> create(@RequestBody BranchRequestDto request) {
        BranchEntity branch = BranchEntity.builder()
                .code(request.code())
                .name(request.name())
                .city(request.city())
                .state(request.state())
                .build();

        branch = branchRepository.save(branch);

        return ResponseEntity
                .created(URI.create("/api/branches/" + branch.getId()))
                .body(new BranchResponseDto(
                        branch.getId(),
                        branch.getCode(),
                        branch.getName(),
                        branch.getCity(),
                        branch.getState()
                ));
    }

    @GetMapping
    public List<BranchResponseDto> list() {
        return branchRepository.findAll().stream()
                .map(b -> new BranchResponseDto(
                        b.getId(),
                        b.getCode(),
                        b.getName(),
                        b.getCity(),
                        b.getState()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchResponseDto> get(@PathVariable UUID id) {
        return branchRepository.findById(id)
                .map(b -> ResponseEntity.ok(new BranchResponseDto(
                        b.getId(),
                        b.getCode(),
                        b.getName(),
                        b.getCity(),
                        b.getState()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}

