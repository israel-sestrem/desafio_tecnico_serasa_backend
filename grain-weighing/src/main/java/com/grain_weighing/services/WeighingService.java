package com.grain_weighing.services;

import com.grain_weighing.dto.WeighingInsertionRequestDto;
import com.grain_weighing.entities.*;
import com.grain_weighing.enums.WeighingType;
import com.grain_weighing.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WeighingService {

    private static final int REQUIRED_STABLE_READINGS = 5;
    private static final BigDecimal MAX_DIFF_BETWEEN_READINGS_KG = new BigDecimal("50.0");

    private final ScaleRepository scaleRepository;
    private final TruckRepository truckRepository;
    private final GrainTypeRepository grainTypeRepository;
    private final TransportTransactionRepository transportTransactionRepository;
    private final WeighingRepository weighingRepository;

    private final Map<String, StabilizationState> stateByKey = new HashMap<>();

    @Transactional
    public Optional<WeighingEntity> insertRawWeighing(WeighingInsertionRequestDto request) {
        String key = buildKey(request.scaleExternalId(), request.licensePlate());
        BigDecimal currentWeight = request.weight();

        StabilizationState state = stateByKey.get(key);
        if (state == null) {
            state = new StabilizationState(currentWeight, 1, false);
            stateByKey.put(key, state);
            return Optional.empty();
        }

        BigDecimal diff = currentWeight.subtract(state.lastWeight()).abs();
        if (diff.compareTo(MAX_DIFF_BETWEEN_READINGS_KG) > 0) {
            state = new StabilizationState(currentWeight, 1, false);
            stateByKey.put(key, state);
            return Optional.empty();
        }

        int newCount = state.stableCount() + 1;
        boolean alreadyStabilized = state.stabilized();

        state = new StabilizationState(currentWeight, newCount, alreadyStabilized);
        stateByKey.put(key, state);

        if (!alreadyStabilized && newCount >= REQUIRED_STABLE_READINGS) {
            state = new StabilizationState(currentWeight, newCount, true);
            stateByKey.put(key, state);

            WeighingEntity weighing = persistStabilizedWeighing(request);
            return Optional.of(weighing);
        }

        return Optional.empty();
    }

    private String buildKey(String scaleExternalId, String licensePlate) {
        return scaleExternalId + "|" + licensePlate;
    }

    private WeighingEntity persistStabilizedWeighing(WeighingInsertionRequestDto request) {
        ScaleEntity scale = scaleRepository.findByExternalId(request.scaleExternalId())
                .orElseThrow(() -> new IllegalArgumentException("Scale not found: " + request.scaleExternalId()));

        TruckEntity truck = truckRepository.findByLicensePlate(request.licensePlate())
                .orElseThrow(() -> new IllegalArgumentException("Truck not found: " + request.licensePlate()));

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
                .id(UUID.randomUUID())
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
                .weighingType(WeighingType.INBOUND)
                .build();

        return weighingRepository.save(weighing);
    }

    private record StabilizationState(
            BigDecimal lastWeight,
            int stableCount,
            boolean stabilized
    ) {}
}
