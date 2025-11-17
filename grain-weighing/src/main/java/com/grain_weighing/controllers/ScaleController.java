package com.grain_weighing.controllers;

import com.grain_weighing.dto.ScaleRequestDto;
import com.grain_weighing.dto.ScaleResponseDto;
import com.grain_weighing.entities.BranchEntity;
import com.grain_weighing.entities.ScaleEntity;
import com.grain_weighing.repositories.BranchRepository;
import com.grain_weighing.repositories.ScaleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scales")
@RequiredArgsConstructor
@Tag(name = "Scales", description = "Scale management")
public class ScaleController {

    private final ScaleRepository scaleRepository;
    private final BranchRepository branchRepository;

    @PostMapping
    @Operation(summary = "Create a new scale")
    public ResponseEntity<?> create(@RequestBody ScaleRequestDto request) {
        BranchEntity branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + request.branchId()));

        ScaleEntity scale = ScaleEntity.builder()
                .externalId(request.externalId())
                .description(request.description())
                .branch(branch)
                .apiToken(request.apiToken())
                .active(true)
                .build();

        scale = scaleRepository.save(scale);

        return ResponseEntity
                .created(URI.create("/api/scales/" + scale.getId()))
                .body(new ScaleResponseDto(
                        scale.getId(),
                        scale.getExternalId(),
                        scale.getDescription(),
                        scale.getBranch().getId(),
                        scale.isActive()
                ));
    }

    @GetMapping
    @Operation(summary = "List all scales")
    public List<ScaleResponseDto> list() {
        return scaleRepository.findAll().stream()
                .map(s -> new ScaleResponseDto(
                        s.getId(),
                        s.getExternalId(),
                        s.getDescription(),
                        s.getBranch().getId(),
                        s.isActive()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get scale by ID")
    public ResponseEntity<ScaleResponseDto> get(@PathVariable UUID id) {
        return scaleRepository.findById(id)
                .map(s -> ResponseEntity.ok(new ScaleResponseDto(
                        s.getId(),
                        s.getExternalId(),
                        s.getDescription(),
                        s.getBranch().getId(),
                        s.isActive()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
