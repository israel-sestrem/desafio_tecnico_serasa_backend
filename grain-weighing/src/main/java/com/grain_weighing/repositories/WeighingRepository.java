package com.grain_weighing.repositories;

import com.grain_weighing.entities.TransportTransactionEntity;
import com.grain_weighing.entities.WeighingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface WeighingRepository extends JpaRepository<WeighingEntity, UUID> {

    List<WeighingEntity> findByWeighingTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<WeighingEntity> findByTransportTransaction(TransportTransactionEntity transportTransaction);

}
