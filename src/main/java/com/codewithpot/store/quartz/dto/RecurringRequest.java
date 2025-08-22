package com.codewithpot.store.quartz.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

@Data
@Accessors(chain = true)
public class RecurringRequest {
    private RecurrenceType recurrenceType;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalTime time;
    private List<DayOfWeek> daysOfWeek;
    private Integer dayOfMonth;
    private Month month;
}

