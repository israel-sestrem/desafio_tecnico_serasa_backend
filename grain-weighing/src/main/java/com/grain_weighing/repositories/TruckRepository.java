package com.grain_weighing.repositories;

import com.grain_weighing.entities.TruckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TruckRepository extends JpaRepository<TruckEntity, UUID> {

    Optional<TruckEntity> findByLicensePlate(String licensePlate);

}
