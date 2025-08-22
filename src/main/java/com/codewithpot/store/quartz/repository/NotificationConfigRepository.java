package com.codewithpot.store.quartz.repository;

import com.codewithpot.store.quartz.Entity.NotificationConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationConfigRepository extends JpaRepository<NotificationConfigEntity, Long> {

}
