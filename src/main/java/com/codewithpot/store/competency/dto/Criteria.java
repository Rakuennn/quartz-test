package com.codewithpot.store.competency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Criteria {

    private String title;
    private String thai;
    private String english;

    private List<String> keywords;
    private List<String> sources;

    @JsonProperty("source_scoring")
    private Map<String,SourceScoring> sourceScoring;

    @JsonProperty("maximum_score")
    private Integer maximumScore;

    @JsonProperty("total_evaluations_allowed")
    private Integer totalEvaluationsAllowed;

}
