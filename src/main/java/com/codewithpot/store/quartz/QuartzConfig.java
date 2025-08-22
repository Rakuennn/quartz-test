package com.codewithpot.store.quartz;

import com.codewithpot.store.quartz.Entity.NotificationConfigEntity;
import com.codewithpot.store.quartz.repository.NotificationConfigRepository;
import com.codewithpot.store.quartz.util.CronPattern;
import org.quartz.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
public class QuartzConfig {

    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");

    private final Scheduler scheduler;
    private final NotificationConfigRepository notificationConfigRepository;

    public QuartzConfig(Scheduler scheduler, NotificationConfigRepository notificationConfigRepository) {
        this.scheduler = scheduler;
        this.notificationConfigRepository = notificationConfigRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadAndScheduleJobs() {
        List<NotificationConfigEntity> rows = notificationConfigRepository.findAll();

        for (NotificationConfigEntity n : rows) {
            if (!"A".equalsIgnoreCase(n.getStatus())) continue;

            String group = "CUSTOM";
            String name  = "notif_" + n.getNotificationConfigId();
            JobKey jobKey = JobKey.jobKey(name, group);
            TriggerKey triggerKey = TriggerKey.triggerKey(name + "_trigger", group);

            try {
                String sending = n.getSendingType() == null ? "" : n.getSendingType().trim().toUpperCase();

                if ("RECURRING".equals(sending)) {
                    String cron = CronPattern.from(n);
                    if (cron == null || !CronExpression.isValidExpression(cron)) continue;

                    JobDetail jobDetail = newJob(MyQuartzJob.class)
                            .withIdentity(jobKey)
                            .usingJobData("daysOfWeek", nz(n.getRecurDayOfWeek()))
                            .usingJobData("month", String.valueOf(n.getRecurMonth()))
                            .usingJobData("dayOfMonth", String.valueOf(n.getRecurDay()))
                            .usingJobData("time", n.getRecurTime() != null ? n.getRecurTime().toString() : "")
                            .storeDurably()
                            .build();

                    TriggerBuilder<CronTrigger> tb = newTrigger()
                            .withIdentity(triggerKey)
                            .forJob(jobDetail)
                            .withSchedule(
                                    cronSchedule(cron)
                                            .inTimeZone(TimeZone.getTimeZone(BANGKOK))
                                            .withMisfireHandlingInstructionDoNothing()
                            );

                    if (n.getEffectiveStartDate() != null) tb.startAt(toDate(n.getEffectiveStartDate()));
                    if (n.getEffectiveEndDate() != null)   tb.endAt(toDate(n.getEffectiveEndDate()));

                    CronTrigger trig = tb.build();

                    if (scheduler.checkExists(jobKey)) {
                        scheduler.addJob(jobDetail, true);
                        scheduler.rescheduleJob(triggerKey, trig);
                    } else {
                        scheduler.scheduleJob(jobDetail, trig);
                    }
                }

                else if ("SCHEDULE".equals(sending)) {
                    if (n.getSendTime() == null) continue;
                    Date fireAt = toDate(n.getSendTime());
                    if (fireAt.before(new Date())) continue;

                    JobDetail jobDetail = newJob(MyQuartzJob.class)
                            .withIdentity(jobKey)
                            .usingJobData("configId", n.getNotificationConfigId())
                            .storeDurably()
                            .build();

                    SimpleTrigger trig = newTrigger()
                            .withIdentity(triggerKey)
                            .forJob(jobDetail)
                            .startAt(fireAt)
                            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                            .build();

                    if (scheduler.checkExists(jobKey)) {
                        scheduler.addJob(jobDetail, true);
                        scheduler.rescheduleJob(triggerKey, trig);
                    } else {
                        scheduler.scheduleJob(jobDetail, trig);
                    }
                }

            } catch (SchedulerException ignored) { }
            catch (IllegalArgumentException ignored) { }
        }
    }

    private static Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(BANGKOK).toInstant());
    }
    private static String nz(String s) { return s == null ? "" : s; }
}
