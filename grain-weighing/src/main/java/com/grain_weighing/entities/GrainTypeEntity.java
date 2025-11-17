package com.grain_weighing.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "grain_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrainTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(name = "purchase_price_per_ton", nullable = false, precision = 15, scale = 2)
    private BigDecimal purchasePricePerTon;

    @Column(name = "min_margin", nullable = false, precision = 5, scale = 4)
    private BigDecimal minMargin;

    @Column(name = "max_margin", nullable = false, precision = 5, scale = 4)
    private BigDecimal maxMargin;

    @OneToMany(mappedBy = "grainType")
    private List<TransportTransactionEntity> transportTransactions;

    @OneToMany(mappedBy = "grainType")
    private List<WeighingEntity> weighings;
}
