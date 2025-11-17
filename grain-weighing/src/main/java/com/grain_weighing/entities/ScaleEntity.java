package com.grain_weighing.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "scale")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScaleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchEntity branch;

    @Column(name = "external_id", nullable = false, unique = true, length = 50)
    private String externalId;

    @Column(length = 100)
    private String description;

    @Column(name = "api_token", length = 120)
    private String apiToken;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "scale")
    private List<WeighingEntity> weighings;
}

