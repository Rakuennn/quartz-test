package com.codewithpot.store.quartz.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SendNowRequest {
    private String notificationTitle;
    private String notificationMessage;
    private RecipientType RecipientType;
    private String notificationType;
    private String notificationGroup;
}
