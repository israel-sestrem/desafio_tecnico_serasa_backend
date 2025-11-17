package com.grain_weighing.controllers;

import com.grain_weighing.dto.TransportTransactionRequestDto;
import com.grain_weighing.dto.TransportTransactionResponseDto;
import com.grain_weighing.entities.*;
import com.grain_weighing.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transport-transactions")
@RequiredArgsConstructor
public class TransportTransactionController {

    private final TransportTransactionRepository transportTransactionRepository;
    private final TruckRepository truckRepository;
    private final BranchRepository branchRepository;
    private final GrainTypeRepository grainTypeRepository;
    private final WeighingRepository weighingRepository;

    @PostMapping("/open")
    public ResponseEntity<?> open(@RequestBody TransportTransactionRequestDto request) {
        TruckEntity truck = truckRepository.findById(request.truckId())
                .orElseThrow(() -> new IllegalArgumentException("Truck not found: " + request.truckId()));

        BranchEntity branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + request.branchId()));

        GrainTypeEntity grainType = grainTypeRepository.findById(request.grainTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Grain type not found: " + request.grainTypeId()));

        BigDecimal appliedMargin = request.appliedMargin();
        if (appliedMargin == null) {
            appliedMargin = grainType.getMinMargin();
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

        tt = transportTransactionRepository.save(tt);

        return ResponseEntity
                .created(URI.create("/api/transport-transactions/" + tt.getId()))
                .body(toResponse(tt));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> close(@PathVariable UUID id) {
        TransportTransactionEntity tt = transportTransactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transport transaction not found: " + id));

        tt.setEndTimestamp(LocalDateTime.now());

        List<WeighingEntity> weighings = weighingRepository.findByTransportTransaction(tt);

        BigDecimal totalNetWeightKg = BigDecimal.ZERO;
        BigDecimal totalLoadCost = BigDecimal.ZERO;

        for (WeighingEntity w : weighings) {
            if (w.getNetWeightKg() != null) {
                totalNetWeightKg = totalNetWeightKg.add(w.getNetWeightKg());
            }
            if (w.getLoadCost() != null) {
                totalLoadCost = totalLoadCost.add(w.getLoadCost());
            }
        }

        tt.setTotalNetWeightKg(totalNetWeightKg);
        tt.setTotalLoadCost(totalLoadCost);

        BigDecimal totalEstimatedRevenue = BigDecimal.ZERO;
        BigDecimal estimatedProfit = BigDecimal.ZERO;

        if (tt.getSalePricePerTon() != null) {
            BigDecimal totalTons = totalNetWeightKg
                    .divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);

            totalEstimatedRevenue = totalTons
                    .multiply(tt.getSalePricePerTon())
                    .setScale(2, RoundingMode.HALF_UP);

            estimatedProfit = totalEstimatedRevenue
                    .subtract(totalLoadCost)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        tt.setTotalEstimatedRevenue(totalEstimatedRevenue);
        tt.setEstimatedProfit(estimatedProfit);

        tt = transportTransactionRepository.save(tt);

        return ResponseEntity.ok(toResponse(tt));
    }

    private TransportTransactionResponseDto toResponse(TransportTransactionEntity tt) {
        return new TransportTransactionResponseDto(
                tt.getId(),
                tt.getTruck().getId(),
                tt.getBranch().getId(),
                tt.getGrainType().getId(),
                tt.getStartTimestamp(),
                tt.getEndTimestamp(),
                tt.getAppliedMargin(),
                tt.getPurchasePricePerTon(),
                tt.getSalePricePerTon(),
                tt.getTotalNetWeightKg(),
                tt.getTotalLoadCost(),
                tt.getTotalEstimatedRevenue(),
                tt.getEstimatedProfit()
        );
    }
}
