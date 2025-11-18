package com.grain_weighing.services;

import com.grain_weighing.config.StabilizationProperties;
import com.grain_weighing.dto.WeighingRequestDto;
import com.grain_weighing.entities.*;
import com.grain_weighing.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WeighingService {
    private final StabilizationProperties properties;

    private final ScaleRepository scaleRepository;
    private final TruckRepository truckRepository;
    private final TransportTransactionRepository transportTransactionRepository;
    private final WeighingRepository weighingRepository;

    private final Map<String, StabilizationState> stateByKey = new ConcurrentHashMap<>();

    public List<WeighingEntity> findByWeighingTimestampBetween(LocalDateTime start, LocalDateTime end) {
        return weighingRepository.findByWeighingTimestampBetween(start, end);
    }

    @Transactional
    public Optional<WeighingEntity> insertRawWeighing(WeighingRequestDto request, String token) {
        ScaleEntity scale = scaleRepository.findByExternalId(request.scaleExternalId())
                .orElseThrow(() -> new IllegalArgumentException("Scale not found"));

        if (!scale.isActive())
            throw new IllegalStateException("Scale is disabled");

        if (!Objects.equals(scale.getApiToken(), token))
            throw new SecurityException("Invalid scale token");

        TruckEntity truck = truckRepository.findByLicensePlate(request.licensePlate())
                .orElseThrow(() -> new IllegalArgumentException("Truck not found: " + request.licensePlate()));

        String key = buildKey(request.scaleExternalId(), request.licensePlate());
        BigDecimal currentWeight = request.weight();

        StabilizationState state = stateByKey.get(key);
        if (state == null) {
            stateByKey.put(key, new StabilizationState(currentWeight, 1, false, System.currentTimeMillis()));
            return Optional.empty();
        }

        BigDecimal diff = currentWeight.subtract(state.lastWeight()).abs();
        if (diff.compareTo(properties.getMaxDiffBetweenReadingsKg()) > 0) {
            stateByKey.put(key, new StabilizationState(currentWeight, 1, false, System.currentTimeMillis()));
            return Optional.empty();
        }

        int newCount = state.stableCount() + 1;
        boolean alreadyStabilized = state.stabilized();

        stateByKey.put(key, new StabilizationState(currentWeight, newCount, alreadyStabilized, System.currentTimeMillis()));

        if (!alreadyStabilized && newCount >= properties.getRequiredStableReadings()) {
            stateByKey.put(key, new StabilizationState(currentWeight, newCount, true, System.currentTimeMillis()));

            WeighingEntity weighing = persistStabilizedWeighing(request, scale, truck);
            return Optional.of(weighing);
        }

        return Optional.empty();
    }

    private String buildKey(String scaleExternalId, String licensePlate) {
        return scaleExternalId + "|" + licensePlate;
    }

    private WeighingEntity persistStabilizedWeighing(WeighingRequestDto request, ScaleEntity scale, TruckEntity truck) {
        Optional<TransportTransactionEntity> openTransactionOpt =
                transportTransactionRepository.findFirstByTruckAndEndTimestampIsNullOrderByStartTimestampDesc(truck);

        GrainTypeEntity grainType = openTransactionOpt
                .map(TransportTransactionEntity::getGrainType)
                .orElseThrow(() -> new IllegalStateException("No open transport transaction for truck " + truck.getLicensePlate()));

        BigDecimal grossWeightKg = request.weight();
        BigDecimal tareKg = truck.getTareWeightKg();
        BigDecimal netWeightKg = grossWeightKg.subtract(tareKg);

        BigDecimal tons = netWeightKg
                .divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);

        BigDecimal loadCost = tons
                .multiply(grainType.getPurchasePricePerTon())
                .setScale(2, RoundingMode.HALF_UP);

        LocalDateTime now = LocalDateTime.now();

        WeighingEntity weighing = WeighingEntity.builder()
                .licensePlate(truck.getLicensePlate())
                .grossWeightKg(grossWeightKg)
                .tareWeightKg(tareKg)
                .netWeightKg(netWeightKg)
                .weighingTimestamp(now)
                .scale(scale)
                .truck(truck)
                .grainType(grainType)
                .transportTransaction(openTransactionOpt.orElse(null))
                .loadCost(loadCost)
                .build();

        return weighingRepository.save(weighing);
    }

    private record StabilizationState(
            BigDecimal lastWeight,
            int stableCount,
            boolean stabilized,
            long lastUpdateMillis
    ) {}
}
