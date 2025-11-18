package com.grain_weighing.controllers;

import com.grain_weighing.dto.GrainTypeRequestDto;
import com.grain_weighing.dto.GrainTypeResponseDto;
import com.grain_weighing.entities.GrainTypeEntity;
import com.grain_weighing.services.GrainTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    private final GrainTypeService grainTypeService;

    @PostMapping
    @Operation(summary = "Create a new grain type")
    public ResponseEntity<GrainTypeResponseDto> create(@Valid @RequestBody GrainTypeRequestDto request) {
        GrainTypeEntity gt = grainTypeService.create(request);

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

    @PutMapping("/{id}")
    public ResponseEntity<GrainTypeResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody GrainTypeRequestDto request
    ) {
        GrainTypeEntity updated = grainTypeService.update(id, request);
        return ResponseEntity.ok(GrainTypeResponseDto.from(updated));
    }


    @GetMapping
    @Operation(summary = "List all grains types")
    public List<GrainTypeResponseDto> list() {
        return grainTypeService.findAll().stream()
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
        return grainTypeService.findById(id)
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
