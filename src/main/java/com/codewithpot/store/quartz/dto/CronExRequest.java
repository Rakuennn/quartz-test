package com.codewithpot.store.quartz.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
@Data
@Accessors(chain = true)
public class CronExRequest {
    private String cronExpression;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
