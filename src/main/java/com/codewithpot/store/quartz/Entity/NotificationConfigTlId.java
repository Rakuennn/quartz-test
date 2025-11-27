package com.codewithpot.store.quartz.Entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@Embeddable
public class NotificationConfigTlId implements Serializable {

    private Long notificationConfigId;

    private String language_name;
}
