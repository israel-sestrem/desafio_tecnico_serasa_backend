package com.grain_weighing.repositories;

import com.grain_weighing.entities.ScaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ScaleRepository extends JpaRepository<ScaleEntity, UUID> {

    Optional<ScaleEntity> findByExternalId(String externalId);

}
