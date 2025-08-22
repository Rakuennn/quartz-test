package com.codewithpot.store.quartzjdbc;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MyQuartzJob implements Job {
    private static final ZoneId BKK = ZoneId.of("Asia/Bangkok");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss VV");
    @Override
    public void execute(JobExecutionContext ctx) {
        var firedTH = ZonedDateTime.ofInstant(ctx.getFireTime().toInstant(), BKK);
        System.out.println("🔥 Quartz Job Executed (TH): " + FMT.format(firedTH));
    }
}
