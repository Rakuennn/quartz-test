package com.codewithpot.store.quartz.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ScheduleRequest {
    private LocalDateTime scheduledAt;
}
