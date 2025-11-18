package com.grain_weighing.services;

import com.grain_weighing.dto.TruckRequestDto;
import com.grain_weighing.entities.TruckEntity;
import com.grain_weighing.repositories.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TruckService {

    private final TruckRepository truckRepository;

    public List<TruckEntity> findAll() {
        return truckRepository.findAll();
    }

    public Optional<TruckEntity> findById(UUID id) {
        return truckRepository.findById(id);
    }

    public TruckEntity create(TruckRequestDto request){
        TruckEntity truck = TruckEntity.builder()
                .licensePlate(request.licensePlate())
                .tareWeightKg(request.tareWeightKg())
                .model(request.model())
                .active(request.active())
                .build();

        return truckRepository.save(truck);
    }

    public TruckEntity update(UUID id, TruckRequestDto request) {
        TruckEntity truck = truckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Truck not found"));

        truck.setLicensePlate(request.licensePlate());
        truck.setTareWeightKg(request.tareWeightKg());
        truck.setModel(request.model());
        truck.setActive(request.active());

        return truckRepository.save(truck);
    }

}
