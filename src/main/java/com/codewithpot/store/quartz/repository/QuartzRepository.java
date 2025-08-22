package com.codewithpot.store.quartz.repository;

import com.codewithpot.store.quartz.Entity.QuartzEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuartzRepository extends JpaRepository<QuartzEntity,Long> {
    Optional<QuartzEntity> findByJobName(String jobName);
}
