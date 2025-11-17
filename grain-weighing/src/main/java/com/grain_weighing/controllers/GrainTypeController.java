package com.grain_weighing.controllers;

import com.grain_weighing.dto.GrainTypeRequestDto;
import com.grain_weighing.dto.GrainTypeResponseDto;
import com.grain_weighing.entities.GrainTypeEntity;
import com.grain_weighing.repositories.GrainTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/grain-types")
@RequiredArgsConstructor
@Tag(name = "Grains types", description = "Grain type management")
public class GrainTypeController {

    private final GrainTypeRepository grainTypeRepository;

    @PostMapping
    @Operation(summary = "Create a new grain type")
    public ResponseEntity<GrainTypeResponseDto> create(@RequestBody GrainTypeRequestDto request) {
        GrainTypeEntity gt = GrainTypeEntity.builder()
                .name(request.name())
                .purchasePricePerTon(request.purchasePricePerTon())
                .minMargin(request.minMargin())
                .maxMargin(request.maxMargin())
                .build();

        gt = grainTypeRepository.save(gt);

        return ResponseEntity
                .created(URI.create("/api/grain-types/" + gt.getId()))
                .body(new GrainTypeResponseDto(
                        gt.getId(),
                        gt.getName(),
                        gt.getPurchasePricePerTon(),
                        gt.getMinMargin(),
                        gt.getMaxMargin()
                ));
    }

    @GetMapping
    @Operation(summary = "List all grains types")
    public List<GrainTypeResponseDto> list() {
        return grainTypeRepository.findAll().stream()
                .map(gt -> new GrainTypeResponseDto(
                        gt.getId(),
                        gt.getName(),
                        gt.getPurchasePricePerTon(),
                        gt.getMinMargin(),
                        gt.getMaxMargin()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get grain type by ID")
    public ResponseEntity<GrainTypeResponseDto> get(@PathVariable UUID id) {
        return grainTypeRepository.findById(id)
                .map(gt -> ResponseEntity.ok(new GrainTypeResponseDto(
                        gt.getId(),
                        gt.getName(),
                        gt.getPurchasePricePerTon(),
                        gt.getMinMargin(),
                        gt.getMaxMargin()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
