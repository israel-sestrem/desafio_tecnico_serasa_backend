package com.grain_weighing.repositories;

import com.grain_weighing.entities.GrainTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GrainTypeRepository extends JpaRepository<GrainTypeEntity, UUID> {
}
