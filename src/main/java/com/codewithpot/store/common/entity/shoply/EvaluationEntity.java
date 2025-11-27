package com.codewithpot.store.common.entity.shoply;

import com.codewithpot.store.json.model.Evidence;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "evaluation")
@Accessors(chain = true)
@Data
public class EvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID evaluationId;

    @Column(name = "date")
    private Date date;

    @Column(name = "score")
    private Double score;

    @Column(name = "accepted")
    private Boolean accepted;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "context")
    private String context;

    @Column(name = "evidence_count")
    private Integer evidenceCount;

}
