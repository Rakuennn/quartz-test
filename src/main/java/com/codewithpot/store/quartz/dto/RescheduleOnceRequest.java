package com.codewithpot.store.quartz.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
public class RescheduleOnceRequest {
    private Timestamp fireAt;
}
