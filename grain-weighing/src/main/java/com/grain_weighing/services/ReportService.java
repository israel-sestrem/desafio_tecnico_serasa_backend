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
        BigDecimal totalCostInternal = BigDecimal.ZERO;
        BigDecimal totalRevenueInternal = BigDecimal.ZERO;

        for (WeighingEntity w : filtered) {

            BigDecimal net = w.getNetWeightKg() != null ? w.getNetWeightKg() : BigDecimal.ZERO;
            totalNet = totalNet.add(net);

            BigDecimal tons = net.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

            if (w.getTransportTransaction() != null &&
                    w.getTransportTransaction().getPurchasePricePerTon() != null) {

                BigDecimal purchasePrice = w.getTransportTransaction().getPurchasePricePerTon();
                BigDecimal cost = tons.multiply(purchasePrice);
                totalCostInternal = totalCostInternal.add(cost);
            }

            if (w.getTransportTransaction() != null &&
                    w.getTransportTransaction().getSalePricePerTon() != null) {

                BigDecimal salePrice = w.getTransportTransaction().getSalePricePerTon();
                BigDecimal revenue = tons.multiply(salePrice);
                totalRevenueInternal = totalRevenueInternal.add(revenue);
            }
        }

        BigDecimal totalCost = totalCostInternal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedRevenue = totalRevenueInternal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal profitInternal = totalRevenueInternal.subtract(totalCostInternal);
        BigDecimal estimatedProfit = profitInternal.setScale(2, RoundingMode.HALF_UP);

        return new WeighingSummaryResponseDto(
                totalNet,
                totalCost,
                estimatedRevenue,
                estimatedProfit
        );
    }
}

