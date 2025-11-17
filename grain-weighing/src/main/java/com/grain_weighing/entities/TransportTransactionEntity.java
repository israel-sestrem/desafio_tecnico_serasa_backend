package com.grain_weighing.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transport_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "truck_id", nullable = false, columnDefinition = "uuid")
    private TruckEntity truck;

    @ManyToOne(optional = false)
    @JoinColumn(name = "branch_id", nullable = false, columnDefinition = "uuid")
    private BranchEntity branch;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grain_type_id", nullable = false, columnDefinition = "uuid")
    private GrainTypeEntity grainType;

    @Column(name = "start_timestamp", nullable = false)
    private LocalDateTime startTimestamp;

    @Column(name = "end_timestamp")
    private LocalDateTime endTimestamp;

    @Column(name = "applied_margin", precision = 5, scale = 4)
    private BigDecimal appliedMargin;

    @Column(name = "purchase_price_per_ton", precision = 15, scale = 2)
    private BigDecimal purchasePricePerTon;

    @Column(name = "sale_price_per_ton", precision = 15, scale = 2)
    private BigDecimal salePricePerTon;

    @Column(name = "total_net_weight_kg", precision = 15, scale = 3)
    private BigDecimal totalNetWeightKg;

    @Column(name = "total_load_cost", precision = 15, scale = 2)
    private BigDecimal totalLoadCost;

    @Column(name = "total_estimated_revenue", precision = 15, scale = 2)
    private BigDecimal totalEstimatedRevenue;

    @Column(name = "estimated_profit", precision = 15, scale = 2)
    private BigDecimal estimatedProfit;

    @OneToMany(mappedBy = "transportTransaction")
    private List<WeighingEntity> weighings;
}
