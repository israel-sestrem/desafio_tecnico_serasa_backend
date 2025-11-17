package com.grain_weighing.services;

import com.grain_weighing.dto.WeighingSummaryResponseDto;
import com.grain_weighing.entities.WeighingEntity;
import com.grain_weighing.repositories.WeighingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final WeighingRepository weighingRepository;

    public WeighingSummaryResponseDto summarize(
            LocalDateTime start,
            LocalDateTime end,
            UUID branchId,
            UUID truckId,
            UUID grainTypeId
    ) {
        List<WeighingEntity> list = weighingRepository.findByWeighingTimestampBetween(start, end);

        var filtered = list.stream()
                .filter(w -> branchId == null || w.getScale().getBranch().getId().equals(branchId))
                .filter(w -> truckId == null || (w.getTruck() != null && w.getTruck().getId().equals(truckId)))
                .filter(w -> grainTypeId == null || w.getGrainType().getId().equals(grainTypeId))
                .toList();

        BigDecimal totalNet = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (WeighingEntity w : filtered) {
            if (w.getNetWeightKg() != null) {
                totalNet = totalNet.add(w.getNetWeightKg());
            }
            if (w.getLoadCost() != null) {
                totalCost = totalCost.add(w.getLoadCost());
            }
        }

        BigDecimal estimatedRevenue = totalCost.multiply(new BigDecimal("1.10"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedProfit = estimatedRevenue.subtract(totalCost)
                .setScale(2, RoundingMode.HALF_UP);

        return new WeighingSummaryResponseDto(
                totalNet,
                totalCost,
                estimatedRevenue,
                estimatedProfit
        );
    }
}

