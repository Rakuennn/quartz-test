package com.codewithpot.store.json.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SampleData {

    private Date date;

    private Double score;

    private Boolean accepted;

    @JsonProperty("source_type")
    private String sourceType;

    private String context;

    @JsonProperty("evidence_count")
    private Integer evidenceCount;

    private List<Evidence> evidences;
}
