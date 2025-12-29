package com.codewithpot.store.common.entity.shoply;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "source_scoring")
@Data
public class SourceScoringEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "maximum_score_per_evaluation")
    private Integer maximumScorePerEvaluation;

    @Column(name = "accepted_min_score")
    private Double acceptedMinScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", nullable = false)
    private CriteriaEntity criteria;
}

