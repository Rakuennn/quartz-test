package com.codewithpot.store.competency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IDP {
    @JsonProperty("maximum_score_per_evaluation")
    private Integer maximumScorePerEvaluation;

    @JsonProperty("accepted_min_score")
    private Double acceptedMinScore;
}
