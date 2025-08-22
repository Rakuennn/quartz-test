package com.codewithpot.store.quartz.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

@Entity
@Accessors(chain = true)
@Data
@Table(name ="Quartz")
public class QuartzEntity {
        @Id
        @GeneratedValue
        private Long id;

        private String jobName;
        private String cronExpression;
        private String jobData;
        private String status;
        private LocalDateTime lastModified;
}
