package com.grain_weighing.repositories;

import com.grain_weighing.entities.TransportTransactionEntity;
import com.grain_weighing.entities.TruckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransportTransactionRepository extends JpaRepository<TransportTransactionEntity, UUID> {

    Optional<TransportTransactionEntity> findFirstByTruckAndEndTimestampIsNullOrderByStartTimestampDesc(TruckEntity truck);
}
