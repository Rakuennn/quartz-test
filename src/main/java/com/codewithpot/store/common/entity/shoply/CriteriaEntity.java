package com.codewithpot.store.common.entity.shoply;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "criteria")
@Accessors(chain = true)
@Data
public class CriteriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "criteria_id")
    private Integer id;

    @Column(name = "criteria_code")
    private String code;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String thai;

    @Column(columnDefinition = "TEXT")
    private String english;

    @Column(name = "maximum_score")
    private Integer maximumScore;

    @Column(name = "total_evaluations_allowed")
    private Integer totalEvaluationsAllowed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_id", nullable = false)
    private CompetencyEntity competency;

    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL)
    private List<SourceScoringEntity> sourceScoring = new ArrayList<>();
}
