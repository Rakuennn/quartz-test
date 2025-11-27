package com.codewithpot.store.json.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Sample {
    private String employeeCode;

    private String email;

    @JsonProperty("evaluation_sessions")
    private List<SampleData> evaluationSessions;
}
