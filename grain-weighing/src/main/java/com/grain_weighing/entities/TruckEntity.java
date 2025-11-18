package com.grain_weighing.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "truck")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "license_plate", nullable = false, unique = true, length = 10)
    private String licensePlate;

    @Column(name = "tare_weight_kg", nullable = false, precision = 15, scale = 2)
    private BigDecimal tareWeightKg;

    @Column(nullable = false, length = 80)
    private String model;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "truck")
    private List<TransportTransactionEntity> transportTransactions;
}

