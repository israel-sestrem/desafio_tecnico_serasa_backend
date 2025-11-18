package com.grain_weighing.controllers;

import com.grain_weighing.dto.ScaleRequestDto;
import com.grain_weighing.dto.ScaleResponseDto;
import com.grain_weighing.entities.ScaleEntity;
import com.grain_weighing.services.ScaleService;
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
@RequestMapping("/api/scales")
@RequiredArgsConstructor
@Tag(name = "Scales", description = "Scale management")
public class ScaleController {

    private final ScaleService scaleService;

    @PostMapping
    @Operation(summary = "Create a new scale")
    public ResponseEntity<?> create(@Valid @RequestBody ScaleRequestDto request) {
        ScaleEntity scale = scaleService.create(request);

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

    @PutMapping("/{id}")
    public ResponseEntity<ScaleResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody ScaleRequestDto request
    ) {
        ScaleEntity updated = scaleService.update(id, request);
        return ResponseEntity.ok(ScaleResponseDto.from(updated));
    }

    @GetMapping
    @Operation(summary = "List all scales")
    public List<ScaleResponseDto> list() {
        return scaleService.findAll().stream()
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
        return scaleService.findById(id)
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
