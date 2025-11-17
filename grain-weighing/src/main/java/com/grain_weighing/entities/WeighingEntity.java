package com.grain_weighing.entities;

import com.grain_weighing.enums.WeighingType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "weighing")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeighingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "scale_id", nullable = false, columnDefinition = "uuid")
    private ScaleEntity scale;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grain_type_id", nullable = false, columnDefinition = "uuid")
    private GrainTypeEntity grainType;

    @ManyToOne
    @JoinColumn(name = "truck_id", columnDefinition = "uuid")
    private TruckEntity truck;

    @ManyToOne
    @JoinColumn(name = "transport_transaction_id", columnDefinition = "uuid")
    private TransportTransactionEntity transportTransaction;

    @Column(name = "license_plate", nullable = false, length = 10)
    private String licensePlate;

    @Column(name = "gross_weight_kg", nullable = false, precision = 15, scale = 3)
    private BigDecimal grossWeightKg;

    @Column(name = "tare_weight_kg", nullable = false, precision = 15, scale = 3)
    private BigDecimal tareWeightKg;

    @Column(name = "net_weight_kg", nullable = false, precision = 15, scale = 3)
    private BigDecimal netWeightKg;

    @Column(name = "weighing_timestamp", nullable = false)
    private LocalDateTime weighingTimestamp;

    @Column(name = "load_cost", precision = 15, scale = 2)
    private BigDecimal loadCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "weighing_type", length = 20)
    private WeighingType weighingType;
}
