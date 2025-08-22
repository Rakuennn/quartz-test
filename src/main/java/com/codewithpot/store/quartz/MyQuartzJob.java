package com.codewithpot.store.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;

public class MyQuartzJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("🔥 Quartz Job Executed at: " + LocalDateTime.now());
    }
}
