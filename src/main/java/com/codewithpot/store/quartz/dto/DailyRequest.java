package com.codewithpot.store.quartz.dto;

import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class DailyRequest {
    private List<DayOfWeek> daysOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer interval;
    private TimeUnit unit;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private MisfirePolicy misfirePolicy = MisfirePolicy.DO_NOTHING;
    private String notificationTitle;
    private String notificationMessage;

    public enum TimeUnit { SECONDS, MINUTES, HOURS }
    public enum MisfirePolicy { DO_NOTHING, FIRE_AND_PROCEED, IGNORE_MISFIRES }
}
