package com.codewithpot.store.quartz.dto;

import lombok.Data;
import org.quartz.DateBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class CalendarIntervalRequest {
    private Integer interval;
    private DateBuilder.IntervalUnit unit;
    private LocalTime fireTime;
    private LocalDate startAt;
    private LocalDate endAt;
    private String notificationTitle;
    private String notificationMessage;
    private MisfirePolicy misfirePolicy = MisfirePolicy.DO_NOTHING;

    public enum MisfirePolicy {
        DO_NOTHING,
        FIRE_AND_PROCEED,
        IGNORE_MISFIRES
    }
}
