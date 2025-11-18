package com.grain_weighing.services;

import com.grain_weighing.dto.TransportTransactionRequestDto;
import com.grain_weighing.entities.*;
import com.grain_weighing.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransportTransactionService {

    private final TruckRepository truckRepository;
    private final BranchRepository branchRepository;
    private final GrainTypeRepository grainTypeRepository;
    private final WeighingRepository weighingRepository;
    private final TransportTransactionRepository transportTransactionRepository;

    public TransportTransactionEntity open(TransportTransactionRequestDto request){
        TruckEntity truck = truckRepository.findById(request.truckId())
                .orElseThrow(() -> new IllegalArgumentException("Truck not found: " + request.truckId()));

        BranchEntity branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + request.branchId()));

        GrainTypeEntity grainType = grainTypeRepository.findById(request.grainTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Grain type not found: " + request.grainTypeId()));

        BigDecimal appliedMargin = request.appliedMargin();
        BigDecimal minMargin = grainType.getMinMargin();
        BigDecimal maxMargin = grainType.getMaxMargin();

        if(appliedMargin == null){
            appliedMargin = minMargin;
        }

        if (appliedMargin.compareTo(minMargin) < 0
                || appliedMargin.compareTo(maxMargin) > 0) {
            throw new IllegalArgumentException("Applied margin must be between " + minMargin + " and " + maxMargin);
        }

        BigDecimal purchase = grainType.getPurchasePricePerTon();
        BigDecimal sale = purchase.multiply(BigDecimal.ONE.add(appliedMargin))
                .setScale(2, RoundingMode.HALF_UP);

        TransportTransactionEntity tt = TransportTransactionEntity.builder()
                .truck(truck)
                .branch(branch)
                .grainType(grainType)
                .startTimestamp(LocalDateTime.now())
                .appliedMargin(appliedMargin)
                .purchasePricePerTon(purchase)
                .salePricePerTon(sale)
                .build();

        return transportTransactionRepository.save(tt);
    }

    public TransportTransactionEntity close(UUID id){
        TransportTransactionEntity tt = transportTransactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transport transaction not found: " + id));

        tt.setEndTimestamp(LocalDateTime.now());

        List<WeighingEntity> weighings = weighingRepository.findByTransportTransaction(tt);

        BigDecimal totalNetWeightKg = BigDecimal.ZERO;

        for (WeighingEntity w : weighings) {
            if (w.getNetWeightKg() != null) {
                totalNetWeightKg = totalNetWeightKg.add(w.getNetWeightKg());
            }
        }

        BigDecimal totalEstimatedRevenue = BigDecimal.ZERO;
        BigDecimal totalLoadCost = BigDecimal.ZERO;
        BigDecimal estimatedProfit = BigDecimal.ZERO;

        if (tt.getSalePricePerTon() != null && tt.getPurchasePricePerTon() != null) {

            BigDecimal totalTons = totalNetWeightKg
                    .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

            BigDecimal revenueInternal = totalTons.multiply(tt.getSalePricePerTon());
            BigDecimal costInternal = totalTons.multiply(tt.getPurchasePricePerTon());
            BigDecimal profitInternal = revenueInternal.subtract(costInternal);

            totalEstimatedRevenue = revenueInternal.setScale(2, RoundingMode.HALF_UP);
            totalLoadCost = costInternal.setScale(2, RoundingMode.HALF_UP);
            estimatedProfit = profitInternal.setScale(2, RoundingMode.HALF_UP);
        }

        tt.setTotalNetWeightKg(totalNetWeightKg);
        tt.setTotalLoadCost(totalLoadCost);
        tt.setTotalEstimatedRevenue(totalEstimatedRevenue);
        tt.setEstimatedProfit(estimatedProfit);

        return transportTransactionRepository.save(tt);
    }

}
