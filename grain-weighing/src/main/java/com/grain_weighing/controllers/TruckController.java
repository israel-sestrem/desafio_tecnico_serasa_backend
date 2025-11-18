package com.grain_weighing.controllers;

import com.grain_weighing.dto.TruckRequestDto;
import com.grain_weighing.dto.TruckResponseDto;
import com.grain_weighing.entities.TruckEntity;
import com.grain_weighing.services.TruckService;
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
@RequestMapping("/api/trucks")
@RequiredArgsConstructor
@Tag(name = "Trucks", description = "Truck management")
public class TruckController {

    private final TruckService truckService;

    @PostMapping
    @Operation(summary = "Create a new truck")
    public ResponseEntity<TruckResponseDto> create(@Valid @RequestBody TruckRequestDto request) {
        TruckEntity truck = truckService.create(request);

        return ResponseEntity
                .created(URI.create("/api/trucks/" + truck.getId()))
                .body(new TruckResponseDto(
                        truck.getId(),
                        truck.getLicensePlate(),
                        truck.getTareWeightKg(),
                        truck.getModel(),
                        truck.isActive()
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TruckResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody TruckRequestDto request
    ) {
        TruckEntity updated = truckService.update(id, request);
        return ResponseEntity.ok(TruckResponseDto.from(updated));
    }

    @GetMapping
    @Operation(summary = "List all trucks")
    public List<TruckResponseDto> list() {
        return truckService.findAll().stream()
                .map(t -> new TruckResponseDto(
                        t.getId(),
                        t.getLicensePlate(),
                        t.getTareWeightKg(),
                        t.getModel(),
                        t.isActive()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get truck by ID")
    public ResponseEntity<TruckResponseDto> get(@PathVariable UUID id) {
        return truckService.findById(id)
                .map(t -> ResponseEntity.ok(new TruckResponseDto(
                        t.getId(),
                        t.getLicensePlate(),
                        t.getTareWeightKg(),
                        t.getModel(),
                        t.isActive()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
