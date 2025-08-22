package com.codewithpot.store.quartz.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@Table(name = "notification_config")
@Accessors(chain = true)
public class NotificationConfigEntity {

    @Id
    @GeneratedValue
    private Long notificationConfigId;

    private String notificationType;
    private String landingPage;
    private String backPage;
    private String createdBy;
    private LocalDateTime createdDate;
    private String updatedBy;
    private LocalDateTime updatedDate;
    private String notificationGroup;
    private String recipientType;
    private LocalDateTime sendTime;
    private String sendingType;
    private String recurringType;
    private LocalDateTime effectiveStartDate;
    private LocalDateTime effectiveEndDate;
    private Integer recurDay;
    private String recurDayOfWeek;
    private Integer recurMonth;
    private LocalTime recurTime;
    private String notificationStatus;
    private String status;
}
