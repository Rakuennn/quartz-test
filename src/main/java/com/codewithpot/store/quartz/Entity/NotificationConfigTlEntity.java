package com.codewithpot.store.quartz.Entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "notification_config_tl")
public class NotificationConfigTlEntity {

    @EmbeddedId
    private NotificationConfigTlId id;

    private String notificationTitle;
    private String notificationMessage;
}
