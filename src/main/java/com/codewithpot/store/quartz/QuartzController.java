package com.codewithpot.store.quartz;

import com.codewithpot.store.common.base.BaseController;
import com.codewithpot.store.quartz.Entity.NotificationConfigEntity;
import com.codewithpot.store.quartz.dto.RecurringRequest;
import com.codewithpot.store.quartz.dto.RescheduleOnceRequest;
import com.codewithpot.store.quartz.dto.ScheduleRequest;
import com.codewithpot.store.quartz.repository.NotificationConfigRepository;
import com.codewithpot.store.quartz.util.CronPattern;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@RestController
@RequestMapping("/api/quartz-mem")
public class QuartzController extends BaseController {

    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
    private static final String GROUP = "CUSTOM";

    private final Scheduler scheduler;
    private final NotificationConfigRepository notificationConfigRepository;

    public QuartzController(Scheduler scheduler,
                            NotificationConfigRepository notificationConfigRepository) {
        this.scheduler = scheduler;
        this.notificationConfigRepository = notificationConfigRepository;
    }
    @PostMapping("/send-now")
    public ResponseEntity<?> sendNow() throws SchedulerException {
        NotificationConfigEntity e = new NotificationConfigEntity();
        e.setSendingType("Send_now");
        e.setSendTime(LocalDateTime.now(BANGKOK));
        e.setStatus("A");
        e.setCreatedBy("system");
        e.setCreatedDate(LocalDateTime.now(BANGKOK));
        e = notificationConfigRepository.save(e);

        JobKey jk = jobKeyOf(e.getNotificationConfigId());
        TriggerKey tk = triggerKeyOf(e.getNotificationConfigId());

        JobDetail job = newJob(MyQuartzJob.class)
                .withIdentity(jk)
                .usingJobData("configId", e.getNotificationConfigId())
                .storeDurably()
                .build();

        Trigger trigger = newTrigger()
                .withIdentity(tk)
                .forJob(job)
                .startNow()
                .withSchedule(simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();

        scheduler.scheduleJob(job, trigger);

        return ResponseEntity.ok(Map.of(
                "message","Dispatched",
                "id", e.getNotificationConfigId(),
                "jobKey", jk.getName(),
                "triggerKey", tk.getName()
        ));
    }
    @PostMapping("/schedule")
    public ResponseEntity<?> schedule(@RequestBody ScheduleRequest req) throws SchedulerException {
        if (req.getScheduledAt() == null)
            return ResponseEntity.badRequest().body(Map.of("error","scheduledAt is required"));

        Date fireAt = Date.from(req.getScheduledAt().atZone(BANGKOK).toInstant());

        if (fireAt.before(new Date()))
            return ResponseEntity.badRequest().body(Map.of("error","scheduledAt must be in the future"));

        NotificationConfigEntity e = new NotificationConfigEntity();
        e.setSendingType("Schedule");
        e.setSendTime(req.getScheduledAt());
        e.setStatus("A");
        e.setCreatedBy("system");
        e.setCreatedDate(LocalDateTime.now(BANGKOK));
        e = notificationConfigRepository.save(e);

        JobKey jk = jobKeyOf(e.getNotificationConfigId());
        TriggerKey tk = triggerKeyOf(e.getNotificationConfigId());

        JobDetail job = newJob(MyQuartzJob.class)
                .withIdentity(jk)
                .usingJobData("configId", e.getNotificationConfigId())
                .storeDurably()
                .build();

        Trigger trigger = newTrigger()
                .withIdentity(tk)
                .forJob(job)
                .startAt(fireAt)
                .withSchedule(simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();

        scheduler.scheduleJob(job, trigger);

        return ResponseEntity.ok(Map.of(
                "message","Scheduled one-time",
                "id", e.getNotificationConfigId(),
                "scheduledAt", req.getScheduledAt().toString()
        ));
    }
    @PostMapping("/recurring")
    public ResponseEntity<?> createRecurring(@RequestBody RecurringRequest req) throws Exception {
        String cron;
        try { cron = CronPattern.from(req); }
        catch (IllegalArgumentException ex) { return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage())); }
        if (!CronExpression.isValidExpression(cron))
            return ResponseEntity.badRequest().body(Map.of("error","Invalid cron", "cron", cron));

        Date start = toDate(req.getStartDateTime());
        Date end   = toDate(req.getEndDateTime());
        if (!willFire(cron, start, end, TimeZone.getTimeZone(BANGKOK)))
            return ResponseEntity.badRequest().body(Map.of("error","Trigger will never fire", "cron", cron));

        NotificationConfigEntity e = new NotificationConfigEntity()
                .setRecurringType(String.valueOf(req.getRecurrenceType()))
                .setEffectiveStartDate(req.getStartDateTime())
                .setEffectiveEndDate(req.getEndDateTime())
                .setRecurTime(req.getTime())
                .setRecurDay(req.getDayOfMonth())
                .setRecurDayOfWeek(listToCsv(req.getDaysOfWeek()))
                .setRecurMonth(req.getMonth() != null ? req.getMonth().getValue() : null)
                .setStatus("A");
        e = notificationConfigRepository.save(e);

        JobKey jk = jobKeyOf(e.getNotificationConfigId());
        TriggerKey tk = triggerKeyOf(e.getNotificationConfigId());

        JobDetail job = newJob(MyQuartzJob.class)
                .withIdentity(jk)
                .usingJobData("configId", e.getNotificationConfigId())
                .storeDurably()
                .build();

        TriggerBuilder<CronTrigger> tb = newTrigger()
                .withIdentity(tk)
                .forJob(job)
                .withSchedule(
                        cronSchedule(cron)
                                .inTimeZone(TimeZone.getTimeZone(BANGKOK))
                                .withMisfireHandlingInstructionDoNothing()
                );
        if (start != null) tb.startAt(start);
        if (end != null) tb.endAt(end);

        scheduler.scheduleJob(job, tb.build());

        return ResponseEntity.ok(Map.of(
                "message","Scheduled recurring",
                "id", e.getNotificationConfigId(),
                "cron", cron
        ));
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> listNotifications() throws SchedulerException {
        List<NotificationConfigEntity> rows = notificationConfigRepository.findAll();
        List<Map<String,Object>> out = new ArrayList<>();
        for (NotificationConfigEntity n : rows) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", n.getNotificationConfigId());
            m.put("sendingType", n.getSendingType());
            m.put("recurringType", n.getRecurringType());
            m.put("sendTime", n.getSendTime());
            m.put("start", n.getEffectiveStartDate());
            m.put("end", n.getEffectiveEndDate());
            m.put("recurDay", n.getRecurDay());
            m.put("recurDOW", n.getRecurDayOfWeek());
            m.put("recurMonth", n.getRecurMonth());
            m.put("recurTime", n.getRecurTime());
            m.put("status", n.getStatus());

            TriggerKey tk = triggerKeyOf(n.getNotificationConfigId());
            Trigger t = scheduler.getTrigger(tk);
            if (t != null) {
                m.put("trigger", triggerView(t, scheduler));
            } else {
                m.put("trigger", null);
            }
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/notifications/{id}")
    public ResponseEntity<?> getNotification(@PathVariable Long id) throws SchedulerException {
        var n = notificationConfigRepository.findById(id).orElse(null);
        if (n == null) return ResponseEntity.status(404).body(Map.of("error","Not found"));

        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", n.getNotificationConfigId());
        m.put("sendingType", n.getSendingType());
        m.put("recurringType", n.getRecurringType());
        m.put("sendTime", n.getSendTime());
        m.put("start", n.getEffectiveStartDate());
        m.put("end", n.getEffectiveEndDate());
        m.put("recurDay", n.getRecurDay());
        m.put("recurDOW", n.getRecurDayOfWeek());
        m.put("recurMonth", n.getRecurMonth());
        m.put("recurTime", n.getRecurTime());
        m.put("status", n.getStatus());

        Trigger t = scheduler.getTrigger(triggerKeyOf(id));
        m.put("trigger", t != null ? triggerView(t, scheduler) : null);

        return ResponseEntity.ok(m);
    }

    @PutMapping("/notifications/{id}/reschedule/once")
    public ResponseEntity<?> rescheduleOnce(@PathVariable Long id,
                                            @RequestBody RescheduleOnceRequest req) throws SchedulerException {
        if (req.getFireAt() == null) {
            return ResponseEntity.badRequest().body(Map.of("error","fireAt is required"));
        }

        var n = notificationConfigRepository.findById(id).orElse(null);
        if (n == null) {
            return ResponseEntity.status(404).body(Map.of("error","Not found"));
        }

        TriggerKey tk = triggerKeyOf(id);
        JobKey jk = jobKeyOf(id);
        Trigger old = scheduler.getTrigger(tk);

        if (!scheduler.checkExists(jk)) {
            JobDetail job = newJob(MyQuartzJob.class)
                    .withIdentity(jk)
                    .usingJobData("configId", id)
                    .storeDurably()
                    .build();
            scheduler.addJob(job, true);
        }

        Instant fireAtInstant = req.getFireAt().toInstant();
        if (!fireAtInstant.isAfter(Instant.now())) {
            return ResponseEntity.badRequest().body(Map.of("error","fireAt must be in the future"));
        }
        Date fireAt = Date.from(fireAtInstant);

        SimpleTrigger trig = newTrigger()
                .withIdentity(tk)
                .forJob(jk)
                .startAt(fireAt)
                .withSchedule(simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();

        Date next;
        if (old != null) {
            next = scheduler.rescheduleJob(tk, trig);
        } else {
            scheduler.scheduleJob(trig);
            next = trig.getNextFireTime();
        }

        n.setSendingType("Schedule");
        n.setSendTime(LocalDateTime.ofInstant(fireAtInstant, BANGKOK));
        n.setUpdatedBy("system");
        n.setUpdatedDate(LocalDateTime.now(BANGKOK));
        notificationConfigRepository.save(n);

        return ResponseEntity.ok(Map.of(
                "message","Rescheduled (once)",
                "nextFireTime", next
        ));
    }


    @PutMapping("/notifications/{id}/reschedule/recurring")
    public ResponseEntity<?> rescheduleRecurring(@PathVariable Long id, @RequestBody RecurringRequest req) throws Exception {
        var n = notificationConfigRepository.findById(id).orElse(null);
        if (n == null) return ResponseEntity.status(404).body(Map.of("error","Not found"));

        String cron;
        try { cron = CronPattern.from(req); }
        catch (IllegalArgumentException ex) { return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage())); }
        if (!CronExpression.isValidExpression(cron))
            return ResponseEntity.badRequest().body(Map.of("error","Invalid cron", "cron", cron));

        Date start = toDate(req.getStartDateTime());
        Date end   = toDate(req.getEndDateTime());
        if (!willFire(cron, start, end, TimeZone.getTimeZone(BANGKOK)))
            return ResponseEntity.badRequest().body(Map.of("error","Trigger will never fire", "cron", cron));

        JobKey jk = jobKeyOf(id);
        TriggerKey tk = triggerKeyOf(id);

        if (!scheduler.checkExists(jk)) {
            JobDetail job = newJob(MyQuartzJob.class)
                    .withIdentity(jk)
                    .usingJobData("configId", id)
                    .storeDurably()
                    .build();
            scheduler.addJob(job, true);
        }

        TriggerBuilder<CronTrigger> tb = newTrigger()
                .withIdentity(tk)
                .forJob(jk)
                .withSchedule(cronSchedule(cron)
                        .inTimeZone(TimeZone.getTimeZone(BANGKOK))
                        .withMisfireHandlingInstructionDoNothing());
        if (start != null) tb.startAt(start);
        if (end != null) tb.endAt(end);

        Date next = scheduler.rescheduleJob(tk, tb.build());

        n.setRecurringType(String.valueOf(req.getRecurrenceType()));
        n.setEffectiveStartDate(req.getStartDateTime());
        n.setEffectiveEndDate(req.getEndDateTime());
        n.setRecurTime(req.getTime());
        n.setRecurDay(req.getDayOfMonth());
        n.setRecurDayOfWeek(listToCsv(req.getDaysOfWeek()));
        n.setRecurMonth(req.getMonth() != null ? req.getMonth().getValue() : null);
        n.setUpdatedBy("system");
        n.setUpdatedDate(LocalDateTime.now(BANGKOK));
        notificationConfigRepository.save(n);

        return ResponseEntity.ok(Map.of("message","Rescheduled (recurring)", "cron", cron, "nextFireTime", next));
    }

    @PutMapping("/notifications/{id}/pause")
    public ResponseEntity<?> pauseById(@PathVariable Long id) throws SchedulerException {
        TriggerKey tk = triggerKeyOf(id);
        Trigger t = scheduler.getTrigger(tk);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));

        scheduler.pauseTrigger(tk);

        notificationConfigRepository.findById(id).ifPresent(n -> {
            n.setStatus("I");
            n.setUpdatedBy("system");
            n.setUpdatedDate(LocalDateTime.now(BANGKOK));
            notificationConfigRepository.save(n);
        });

        Trigger.TriggerState st = scheduler.getTriggerState(tk);
        return ResponseEntity.ok(Map.of(
                "message", "Paused",
                "id", id,
                "triggerState", st.name(),
                "status", "I"
        ));
    }

    @PutMapping("/notifications/{id}/resume")
    public ResponseEntity<?> resumeById(@PathVariable Long id) throws SchedulerException {
        TriggerKey tk = triggerKeyOf(id);
        Trigger t = scheduler.getTrigger(tk);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));

        scheduler.resumeTrigger(tk);

        notificationConfigRepository.findById(id).ifPresent(n -> {
            n.setStatus("A");
            n.setUpdatedBy("system");
            n.setUpdatedDate(LocalDateTime.now(BANGKOK));
            notificationConfigRepository.save(n);
        });

        Trigger.TriggerState st = scheduler.getTriggerState(tk);
        return ResponseEntity.ok(Map.of(
                "message", "Resumed",
                "id", id,
                "triggerState", st.name(),
                "status", "A"
        ));
    }



    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) throws SchedulerException {
        TriggerKey tk = triggerKeyOf(id);
        JobKey jk = jobKeyOf(id);
        scheduler.unscheduleJob(tk);
        scheduler.deleteJob(jk);

        if (!notificationConfigRepository.existsById(id))
            return ResponseEntity.status(404).body(Map.of("error","Not found"));
        notificationConfigRepository.deleteById(id);

        return ResponseEntity.ok(Map.of("message","Deleted", "id", id));
    }


    private static JobKey jobKeyOf(Long id) {
        return JobKey.jobKey("notify-" + id, GROUP);
    }
    private static TriggerKey triggerKeyOf(Long id) {
        return TriggerKey.triggerKey("notify-" + id + "_trigger", GROUP);
    }

    private static String listToCsv(List<? extends Enum<?>> enums) {
        if (enums == null || enums.isEmpty()) return null;
        return enums.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private static Date toDate(LocalDateTime ldt) {
        return (ldt == null) ? null : Date.from(ldt.atZone(BANGKOK).toInstant());
    }

    private static boolean willFire(String cron, Date startAt, Date endAt, TimeZone tz) {
        try {
            CronExpression expr = new CronExpression(cron);
            expr.setTimeZone(tz);
            Date base = (startAt != null) ? startAt : new Date();
            Date first = expr.getNextValidTimeAfter(base);
            if (first == null) return false;
            return endAt == null || first.before(endAt);
        } catch (Exception e) {
            return false;
        }
    }

    private static Map<String, Object> triggerView(Trigger t, Scheduler scheduler) throws SchedulerException {
        Map<String, Object> m = new LinkedHashMap<>();
        TriggerKey tk = t.getKey();
        m.put("name", tk.getName());
        m.put("group", tk.getGroup());
        m.put("jobName", t.getJobKey().getName());
        m.put("jobGroup", t.getJobKey().getGroup());
        m.put("type", t.getClass().getSimpleName());
        m.put("state", scheduler.getTriggerState(tk).name());
        m.put("startAt", t.getStartTime());
        m.put("endAt", t.getEndTime());
        m.put("prevFireTime", t.getPreviousFireTime());
        m.put("nextFireTime", t.getNextFireTime());
        if (t instanceof CronTrigger ct) {
            m.put("cronExpression", ct.getCronExpression());
            m.put("timeZone", ct.getTimeZone() != null ? ct.getTimeZone().getID() : null);
        }
        return m;
    }
}
