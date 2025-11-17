package com.grain_weighing.controllers;

import com.grain_weighing.dto.TruckRequestDto;
import com.grain_weighing.dto.TruckResponseDto;
import com.grain_weighing.entities.TruckEntity;
import com.grain_weighing.repositories.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trucks")
@RequiredArgsConstructor
public class TruckController {

    private final TruckRepository truckRepository;

    @PostMapping
    public ResponseEntity<TruckResponseDto> create(@RequestBody TruckRequestDto request) {
        TruckEntity truck = TruckEntity.builder()
                .licensePlate(request.licensePlate())
                .tareWeightKg(request.tareWeightKg())
                .model(request.model())
                .active(true)
                .build();

        truck = truckRepository.save(truck);

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

    @GetMapping
    public List<TruckResponseDto> list() {
        return truckRepository.findAll().stream()
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
    public ResponseEntity<TruckResponseDto> get(@PathVariable UUID id) {
        return truckRepository.findById(id)
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
