package com.codewithpot.store.quartzjdbc;

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

@RestController
public class QuartzJdbcController {

    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
    private final Scheduler scheduler;
    private final NotificationConfigRepository notifyRepo;

    public QuartzJdbcController(Scheduler scheduler, NotificationConfigRepository notifyRepo) {
        this.scheduler = scheduler;
        this.notifyRepo = notifyRepo;
    }

    private static String listToCsv(Collection<?> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list.stream().map(String::valueOf).toList());
    }

    @PostMapping("/send-now")
    public ResponseEntity<?> sendNow() throws Exception {
        String key = UUID.randomUUID().toString();

        JobDetail job = JobBuilder.newJob(MyQuartzJob.class)
                .withIdentity(key, "CUSTOM")
                .usingJobData("data", "")
                .storeDurably()
                .build();

        String trigKey = key + "_trigger";
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(trigKey, "CUSTOM")
                .forJob(job)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        scheduler.scheduleJob(job, trigger);

        NotificationConfigEntity e = new NotificationConfigEntity();
        e.setSendingType("Send_now");
        e.setSendTime(LocalDateTime.now(BANGKOK));
        e.setStatus("A");
        e.setCreatedBy("system");
        e.setCreatedDate(LocalDateTime.now(BANGKOK));
        notifyRepo.save(e);

        return ResponseEntity.ok(Map.of(
                "message", "Dispatched",
                "jobKey", key,
                "triggerKey", trigKey
        ));
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> schedule(@RequestBody ScheduleRequest req) throws Exception {
        if (req.getScheduledAt() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "scheduledAt is required"));
        }
        Date fireAt = Date.from(req.getScheduledAt().atZone(BANGKOK).toInstant());
        if (fireAt.before(new Date())) {
            return ResponseEntity.badRequest().body(Map.of("error","scheduledAt must be in the future"));
        }

        String key = UUID.randomUUID().toString();
        JobDetail job = JobBuilder.newJob(MyQuartzJob.class)
                .withIdentity(key, "CUSTOM")
                .storeDurably()
                .build();

        String trigKey = key + "_trigger";
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(trigKey, "CUSTOM")
                .forJob(job)
                .startAt(fireAt)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        scheduler.scheduleJob(job, trigger);

        NotificationConfigEntity e = new NotificationConfigEntity();
        e.setSendingType("Schedule");
        e.setSendTime(req.getScheduledAt());
        e.setStatus("A");
        e.setCreatedBy("system");
        e.setCreatedDate(LocalDateTime.now(BANGKOK));
        notifyRepo.save(e);

        return ResponseEntity.ok(Map.of(
                "message","Scheduled one-time",
                "jobKey", key,
                "triggerKey", trigKey,
                "scheduled", req.getScheduledAt().toString()
        ));
    }

    @PostMapping("/recurring")
    public ResponseEntity<?> recurring(@RequestBody RecurringRequest req) throws Exception {
        String cron = CronPattern.from(req);
        if (!CronExpression.isValidExpression(cron)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid cron", "cron", cron));
        }

        String jobKey = UUID.randomUUID().toString();
        String triggerKey = jobKey + "_trigger";

        JobDetail job = JobBuilder.newJob(MyQuartzJob.class)
                .withIdentity(jobKey, "CUSTOM")
                .storeDurably()
                .build();

        TriggerBuilder<CronTrigger> tb = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey, "CUSTOM")
                .forJob(job)
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(cron)
                                .inTimeZone(TimeZone.getTimeZone(BANGKOK))
                                .withMisfireHandlingInstructionDoNothing()
                );

        if (req.getStartDateTime() != null) {
            tb.startAt(Date.from(req.getStartDateTime().atZone(BANGKOK).toInstant()));
        }
        if (req.getEndDateTime() != null) {
            tb.endAt(Date.from(req.getEndDateTime().atZone(BANGKOK).toInstant()));
        }

        scheduler.scheduleJob(job, tb.build());

        NotificationConfigEntity notify = new NotificationConfigEntity()
                .setRecurringType(String.valueOf(req.getRecurrenceType()))
                .setEffectiveStartDate(req.getStartDateTime())
                .setEffectiveEndDate(req.getEndDateTime())
                .setRecurTime(req.getTime())
                .setRecurDay(req.getDayOfMonth())
                .setRecurDayOfWeek(listToCsv(req.getDaysOfWeek()))
                .setRecurMonth(req.getMonth() != null ? req.getMonth().getValue() : null)
                .setStatus("A");
        notifyRepo.save(notify);

        return ResponseEntity.ok(Map.of(
                "message", "Scheduled recurring",
                "jobKey", jobKey,
                "triggerKey", triggerKey,
                "cron", cron
        ));
    }

    @PutMapping("/jobs/{group}/{name}/pause")
    public ResponseEntity<?> pauseJob(@PathVariable String group, @PathVariable String name) throws Exception {
        JobKey jk = JobKey.jobKey(name, group);
        if (!scheduler.checkExists(jk))
            return ResponseEntity.status(404).body(Map.of("error", "Job not found"));
        scheduler.pauseJob(jk);
        return ResponseEntity.ok(Map.of("message", "Paused", "job", name, "group", group));
    }

    @PutMapping("/jobs/{group}/{name}/resume")
    public ResponseEntity<?> resumeJob(@PathVariable String group, @PathVariable String name) throws Exception {
        JobKey jk = JobKey.jobKey(name, group);
        if (!scheduler.checkExists(jk))
            return ResponseEntity.status(404).body(Map.of("error", "Job not found"));
        scheduler.resumeJob(jk);
        return ResponseEntity.ok(Map.of("message", "Resumed", "job", name, "group", group));
    }

    @PutMapping("/trigger/{group}/{name}/pause")
    public ResponseEntity<?> pauseTrigger(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        TriggerKey tk = TriggerKey.triggerKey(name, group);
        Trigger t = scheduler.getTrigger(tk);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));

        scheduler.pauseTrigger(tk);
        Trigger.TriggerState st = scheduler.getTriggerState(tk);
        return ResponseEntity.ok(Map.of("message","Paused trigger", "trigger", name, "group", group, "state", st.name()));
    }

    @PutMapping("/trigger/{group}/{name}/resume")
    public ResponseEntity<?> resumeTrigger(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        TriggerKey tk = TriggerKey.triggerKey(name, group);
        Trigger t = scheduler.getTrigger(tk);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));

        scheduler.resumeTrigger(tk);
        Trigger.TriggerState st = scheduler.getTriggerState(tk);
        return ResponseEntity.ok(Map.of("message","Resumed trigger", "trigger", name, "group", group, "state", st.name()));
    }

    @PutMapping("/triggers/{group}/{name}/reschedule/recurring")
    public ResponseEntity<?> rescheduleRecurring(@PathVariable String group,
                                                 @PathVariable String name,
                                                 @RequestBody RecurringRequest req) throws SchedulerException {
        TriggerKey tk = TriggerKey.triggerKey(name, group);
        Trigger old = scheduler.getTrigger(tk);
        if (old == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));

        final String cron;
        try { cron = CronPattern.from(req); }
        catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
        if (!CronExpression.isValidExpression(cron))
            return ResponseEntity.badRequest().body(Map.of("error","Invalid cron generated", "cron", cron));

        TimeZone tz = TimeZone.getTimeZone(BANGKOK);
        TriggerBuilder<CronTrigger> tb = TriggerBuilder.newTrigger()
                .withIdentity(tk)
                .forJob(old.getJobKey())
                .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                        .inTimeZone(tz)
                        .withMisfireHandlingInstructionDoNothing());

        if (req.getStartDateTime()!=null) tb.startAt(Date.from(req.getStartDateTime().atZone(BANGKOK).toInstant()));
        if (req.getEndDateTime()!=null)   tb.endAt(Date.from(req.getEndDateTime().atZone(BANGKOK).toInstant()));

        Date next = scheduler.rescheduleJob(tk, tb.build());

        NotificationConfigEntity e = new NotificationConfigEntity();
        e.setSendingType("Recurring");
        e.setRecurringType(String.valueOf(req.getRecurrenceType()));
        e.setEffectiveStartDate(req.getStartDateTime());
        e.setEffectiveEndDate(req.getEndDateTime());
        e.setRecurTime(req.getTime());
        e.setRecurDay(req.getDayOfMonth());
        e.setRecurDayOfWeek(req.getDaysOfWeek() == null ? null : req.getDaysOfWeek().stream()
                .map(DayOfWeek::name)
                .collect(Collectors.joining(",")));
        e.setRecurMonth(req.getMonth() != null ? req.getMonth().getValue() : null);
        e.setStatus("A");
        e.setUpdatedBy("system");
        e.setUpdatedDate(LocalDateTime.now(BANGKOK));
        notifyRepo.save(e);

        return ResponseEntity.ok(Map.of(
                "message", "Rescheduled (recurring)",
                "cron", cron,
                "nextFireTime", next
        ));
    }

    @PutMapping("/triggers/{group}/{name}/reschedule/schedule")
    public ResponseEntity<?> rescheduleOnce(@PathVariable String group, @PathVariable String name,
                                            @RequestBody RescheduleOnceRequest req) throws Exception {
        if (req.getFireAt()==null) {
            return ResponseEntity.badRequest().body(Map.of("error","fireAt is required"));
        }
        TriggerKey tk = TriggerKey.triggerKey(name, group);
        Trigger old = scheduler.getTrigger(tk);
        if (old == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));

        JobDetail jd = scheduler.getJobDetail(old.getJobKey());
        Trigger newTrig = TriggerBuilder.newTrigger()
                .withIdentity(tk)
                .forJob(jd)
                .startAt(new Date(req.getFireAt().getTime()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        Date next = scheduler.rescheduleJob(tk, newTrig);

        NotificationConfigEntity e = new NotificationConfigEntity();
        e.setSendingType("Schedule");
        e.setSendTime(req.getFireAt().toInstant().atZone(BANGKOK).toLocalDateTime());
        e.setStatus("A");
        e.setUpdatedBy("system");
        e.setUpdatedDate(LocalDateTime.now(BANGKOK));
        notifyRepo.save(e);

        return ResponseEntity.ok(Map.of(
                "message","Rescheduled (once)",
                "nextFireTime", next
        ));
    }

    @DeleteMapping("/jobs/{group}/{name}")
    public ResponseEntity<?> deleteJob(@PathVariable String group, @PathVariable String name) throws Exception {
        JobKey jk = JobKey.jobKey(name, group);
        boolean removed = scheduler.deleteJob(jk);
        if (!removed) return ResponseEntity.status(404).body(Map.of("error","Job not found"));
        return ResponseEntity.ok(Map.of("message","Deleted job", "job", name, "group", group));
    }

    @DeleteMapping("/triggers/{group}/{name}")
    public ResponseEntity<?> deleteTrigger(@PathVariable String group, @PathVariable String name) throws Exception {
        TriggerKey tk = TriggerKey.triggerKey(name, group);
        boolean removed = scheduler.unscheduleJob(tk);
        if (!removed) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));
        return ResponseEntity.ok(Map.of("message","Unschedule trigger", "trigger", name, "group", group));
    }

    @GetMapping("/jobs")
    public ResponseEntity<?> listJobs() throws SchedulerException {
        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyGroup());
        List<Map<String, Object>> result = new ArrayList<>();

        for (JobKey jk : jobKeys) {
            JobDetail jd = scheduler.getJobDetail(jk);
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jk);

            Map<String, Object> jv = new LinkedHashMap<>();
            jv.put("name", jk.getName());
            jv.put("group", jk.getGroup());
            jv.put("jobData", new LinkedHashMap<>(jd.getJobDataMap()));

            List<Map<String, Object>> trigViews = new ArrayList<>();
            for (Trigger t : triggers) {
                try {
                    trigViews.add(triggerView(t, scheduler));
                } catch (SchedulerException ignored) {}
            }
            jv.put("triggers", trigViews);

            result.add(jv);
        }

        result.sort(Comparator
                .comparing((Map<String, Object> m) -> (String) m.get("group"))
                .thenComparing(m -> (String) m.get("name")));

        return ResponseEntity.ok(result);
    }

    @GetMapping("/triggers")
    public ResponseEntity<?> listTriggers(@RequestParam(required = false) String group,
                                          @RequestParam(required = false) String name) throws SchedulerException {

        if (notBlank(group) && notBlank(name)) {
            TriggerKey tk = TriggerKey.triggerKey(name, group);
            Trigger t = scheduler.getTrigger(tk);
            if (t == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));
            return ResponseEntity.ok(triggerView(t, scheduler));
        }

        Set<TriggerKey> keys;
        if (notBlank(group)) {
            keys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group));
        } else {
            keys = scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (TriggerKey tk : keys) {
            if (notBlank(name) && !tk.getName().equals(name)) continue; // กรองชื่อถ้ามี
            Trigger t = scheduler.getTrigger(tk);
            if (t == null) continue;
            out.add(triggerView(t, scheduler));
        }

        out.sort(Comparator
                .comparing((Map<String, Object> m) -> (String) m.get("group"))
                .thenComparing(m -> (String) m.get("name")));

        return ResponseEntity.ok(out);
    }


    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
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
        m.put("priority", t.getPriority());
        m.put("startAt", t.getStartTime());
        m.put("endAt", t.getEndTime());
        m.put("prevFireTime", t.getPreviousFireTime());
        m.put("nextFireTime", t.getNextFireTime());
        m.put("misfireInstruction", t.getMisfireInstruction());
        m.put("description", t.getDescription());

        if (t instanceof CronTrigger ct) {
            m.put("cronExpression", ct.getCronExpression());
            m.put("timeZone", ct.getTimeZone() != null ? ct.getTimeZone().getID() : null);
        } else if (t instanceof SimpleTrigger st) {
            m.put("repeatIntervalMs", st.getRepeatInterval());
            m.put("repeatCount", st.getRepeatCount());
            m.put("timesTriggered", st.getTimesTriggered());
        }
        return m;
    }

    @GetMapping("/triggers/cron")
    public ResponseEntity<?> listCronTriggers(@RequestParam(required = false) String group,
                                              @RequestParam(required = false) String name) throws SchedulerException {
        // ถ้าระบุทั้ง group และ name -> ตัวเดียว
        if (notBlank(group) && notBlank(name)) {
            TriggerKey tk = TriggerKey.triggerKey(name, group);
            Trigger t = scheduler.getTrigger(tk);
            if (t == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));
            if (!(t instanceof CronTrigger ct)) {
                return ResponseEntity.badRequest().body(Map.of("error","Not a CronTrigger", "actualType", t.getClass().getSimpleName()));
            }
            return ResponseEntity.ok(cronTriggerView(ct, scheduler));
        }

        // เลือก key ตาม group
        Set<TriggerKey> keys = notBlank(group)
                ? scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group))
                : scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());

        List<Map<String,Object>> out = new ArrayList<>();
        for (TriggerKey tk : keys) {
            if (notBlank(name) && !tk.getName().equals(name)) continue;
            Trigger t = scheduler.getTrigger(tk);
            if (t instanceof CronTrigger ct) {
                out.add(cronTriggerView(ct, scheduler));
            }
        }
        out.sort(Comparator
                .comparing((Map<String,Object> m) -> (String)m.get("group"))
                .thenComparing(m -> (String)m.get("name")));
        return ResponseEntity.ok(out);
    }

    @GetMapping("/triggers/simple")
    public ResponseEntity<?> listSimpleTriggers(@RequestParam(required = false) String group,
                                                @RequestParam(required = false) String name) throws SchedulerException {
        if (notBlank(group) && notBlank(name)) {
            TriggerKey tk = TriggerKey.triggerKey(name, group);
            Trigger t = scheduler.getTrigger(tk);
            if (t == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));
            if (!(t instanceof SimpleTrigger st)) {
                return ResponseEntity.badRequest().body(Map.of("error","Not a SimpleTrigger", "actualType", t.getClass().getSimpleName()));
            }
            return ResponseEntity.ok(simpleTriggerView(st, scheduler));
        }

        Set<TriggerKey> keys = notBlank(group)
                ? scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group))
                : scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());

        List<Map<String,Object>> out = new ArrayList<>();
        for (TriggerKey tk : keys) {
            if (notBlank(name) && !tk.getName().equals(name)) continue;
            Trigger t = scheduler.getTrigger(tk);
            if (t instanceof SimpleTrigger st) {
                out.add(simpleTriggerView(st, scheduler));
            }
        }
        out.sort(Comparator
                .comparing((Map<String,Object> m) -> (String)m.get("group"))
                .thenComparing(m -> (String)m.get("name")));
        return ResponseEntity.ok(out);
    }

    private static Map<String,Object> cronTriggerView(CronTrigger ct, Scheduler scheduler) throws SchedulerException {
        Map<String,Object> m = new LinkedHashMap<>();
        TriggerKey tk = ct.getKey();
        m.put("name", tk.getName());
        m.put("group", tk.getGroup());
        m.put("jobName", ct.getJobKey().getName());
        m.put("jobGroup", ct.getJobKey().getGroup());
        m.put("type", "CronTrigger");
        m.put("state", scheduler.getTriggerState(tk).name());
        m.put("priority", ct.getPriority());
        m.put("cronExpression", ct.getCronExpression());
        m.put("timeZone", ct.getTimeZone()!=null ? ct.getTimeZone().getID() : null);
        m.put("startAt", ct.getStartTime());
        m.put("endAt", ct.getEndTime());
        m.put("prevFireTime", ct.getPreviousFireTime());
        m.put("nextFireTime", ct.getNextFireTime());
        m.put("misfireInstruction", ct.getMisfireInstruction());
        m.put("description", ct.getDescription());
        return m;
    }

    private static Map<String,Object> simpleTriggerView(SimpleTrigger st, Scheduler scheduler) throws SchedulerException {
        Map<String,Object> m = new LinkedHashMap<>();
        TriggerKey tk = st.getKey();
        m.put("name", tk.getName());
        m.put("group", tk.getGroup());
        m.put("jobName", st.getJobKey().getName());
        m.put("jobGroup", st.getJobKey().getGroup());
        m.put("type", "SimpleTrigger");
        m.put("state", scheduler.getTriggerState(tk).name());
        m.put("priority", st.getPriority());
        m.put("startAt", st.getStartTime());
        m.put("endAt", st.getEndTime());
        m.put("prevFireTime", st.getPreviousFireTime());
        m.put("nextFireTime", st.getNextFireTime());
        m.put("misfireInstruction", st.getMisfireInstruction());
        m.put("repeatIntervalMs", st.getRepeatInterval());
        m.put("repeatCount", st.getRepeatCount());
        m.put("timesTriggered", st.getTimesTriggered());
        m.put("description", st.getDescription());
        return m;
    }
}
