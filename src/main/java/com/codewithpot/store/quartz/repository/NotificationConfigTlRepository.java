package com.codewithpot.store.quartz.repository;

import com.codewithpot.store.quartz.Entity.NotificationConfigTlEntity;
import com.codewithpot.store.quartz.Entity.NotificationConfigTlId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationConfigTlRepository extends JpaRepository<NotificationConfigTlEntity , NotificationConfigTlId> {

}
