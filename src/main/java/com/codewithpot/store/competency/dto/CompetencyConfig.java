package com.codewithpot.store.competency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class CompetencyConfig {
    @JsonProperty("display_name")
    private String displayName;
    private String icon;
    private Integer order;

    @JsonProperty("competency_level")
    private Integer competencyLevel;
    private Theme theme;

    @JsonProperty("criteria")
    private Map<String, Criteria> criteria;
}
