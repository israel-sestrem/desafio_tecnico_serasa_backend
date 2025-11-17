package com.grain_weighing.repositories;

import com.grain_weighing.entities.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BranchRepository extends JpaRepository<BranchEntity, UUID> {
}
