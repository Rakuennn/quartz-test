package com.codewithpot.store.competency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Theme {
    private String primary;
    private String dark;
    private String light;
    private String lighter;
    private String text;
    private String bg;

    @JsonProperty("pre_bg")
    private String preBg;
    private String border;

    @JsonProperty("stat_bg")
    private String statBg;
}