package com.codewithpot.store.json.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Evidence {
    @JsonProperty("evidence_piece")
    private String evidencePiece;

    private Double score;

    private String reasoning;

    @JsonProperty("source_type")
    private String sourceType;
}
