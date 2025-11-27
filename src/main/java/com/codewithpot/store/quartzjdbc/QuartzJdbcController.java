//package com.codewithpot.store.quartzjdbc;
//
//import com.codewithpot.store.quartz.Entity.NotificationConfigEntity;
//import com.codewithpot.store.quartz.Entity.NotificationConfigTlEntity;
//import com.codewithpot.store.quartz.Entity.NotificationConfigTlId;
//import com.codewithpot.store.quartz.dto.*;
//import com.codewithpot.store.quartz.repository.NotificationConfigRepository;
//import com.codewithpot.store.quartz.repository.NotificationConfigTlRepository;
//import com.codewithpot.store.quartz.util.CronPattern;
//import org.quartz.*;
//import org.quartz.impl.matchers.GroupMatcher;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.*;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.stream.Collectors;
//
////@RestController
//public class QuartzJdbcController {
//
//    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
//    private final Scheduler scheduler;
//    private final NotificationConfigRepository notifyRepo;
//    private static final DateTimeFormatter TH_FMT =
//            DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy HH:mm:ss z");
//    @Autowired
//    private NotificationConfigTlRepository notificationConfigTlRepository;
//    public QuartzJdbcController(Scheduler scheduler, NotificationConfigRepository notifyRepo) {
//        this.scheduler = scheduler;
//        this.notifyRepo = notifyRepo;
//    }
//
//    private static String listToCsv(Collection<?> list) {
//        if (list == null || list.isEmpty()) return null;
//        return String.join(",", list.stream().map(String::valueOf).toList());
//    }
//    private static String nz(String s) { return (s == null) ? "" : s; }
//
//    @PostMapping("/simprop-calendar")
//    public ResponseEntity<?> simpropCalendarInterval(@RequestBody CalendarIntervalRequest req) throws SchedulerException {
//        if (req.getInterval() == null || req.getInterval() <= 0)
//            return ResponseEntity.badRequest().body(Map.of("error","interval must be > 0"));
//        if (req.getUnit() == null)
//            return ResponseEntity.badRequest().body(Map.of("error","unit is required"));
//        if (req.getFireTime() == null)
//            return ResponseEntity.badRequest().body(Map.of("error","fireTime is required"));
//
//
//        final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
//
//        LocalDate startDate = (req.getStartAt() != null) ? req.getStartAt()
//                : LocalDate.now(BANGKOK);
//        LocalTime fireTime = req.getFireTime();
//
//        ZonedDateTime candidate = ZonedDateTime.of(startDate, fireTime, BANGKOK);
//
//        ZonedDateTime nowTh = ZonedDateTime.now(BANGKOK);
//        while (candidate.isBefore(nowTh)) {
//            candidate = plus(candidate, req.getUnit(), req.getInterval());
//        }
//
//        Date endAt = null;
//        if (req.getEndAt() != null) {
//            ZonedDateTime endZdt = ZonedDateTime.of(req.getEndAt(), fireTime, BANGKOK);
//            endAt = Date.from(endZdt.toInstant());
//            if (!candidate.isBefore(endZdt)) {
//                return ResponseEntity.badRequest().body(Map.of("error","windowEndDate is before/equal first fire"));
//            }
//        }
//
//        String group = "CUSTOM";
//        String key = UUID.randomUUID().toString();
//        String trigName = "t_calv2_" + key;
//
//        JobDetail jd = JobBuilder.newJob(MyQuartzJob.class)
//                .withIdentity(key, group)
//                .usingJobData("scheduleType", "Simprop Calendar (V2)")
//                .usingJobData("title", nz(req.getNotificationTitle()))
//                .usingJobData("message", nz(req.getNotificationMessage()))
//                .storeDurably()
//                .build();
//
//        CalendarIntervalScheduleBuilder sb = CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
//                .withInterval(req.getInterval(), req.getUnit());
//
//        switch (req.getMisfirePolicy()) {
//            case FIRE_AND_PROCEED -> sb.withMisfireHandlingInstructionFireAndProceed();
//            case IGNORE_MISFIRES  -> sb.withMisfireHandlingInstructionIgnoreMisfires();
//            case DO_NOTHING       -> sb.withMisfireHandlingInstructionDoNothing();
//        }
//
//        TriggerBuilder<CalendarIntervalTrigger> tb = TriggerBuilder.newTrigger()
//                .withIdentity(trigName, group)
//                .forJob(jd)
//                .usingJobData("scheduleType", "Simprop Calendar")
//                .usingJobData("title", nz(req.getNotificationTitle()))
//                .usingJobData("message", nz(req.getNotificationMessage()))
//                .startAt(Date.from(candidate.toInstant()))
//                .withSchedule(sb);
//
//        if (endAt != null) tb.endAt(endAt);
//
//        Trigger t = tb.build();
//        scheduler.scheduleJob(jd, t);
//
//        return ResponseEntity.ok(Map.ofEntries(
//                Map.entry("type","CalendarIntervalTrigger (simprop) – V2 (separate window & fireTime)"),
//                Map.entry("jobKey", jd.getKey().toString()),
//                Map.entry("triggerKey", t.getKey().toString()),
//                Map.entry("firstFireAtTH", candidate.toString()),
//                Map.entry("interval", req.getInterval()),
//                Map.entry("unit", req.getUnit().name()),
//                Map.entry("StartDate", startDate.toString()),
//                Map.entry("EndDate", req.getEndAt()!=null? req.getEndAt().toString(): null),
//                Map.entry("fireTime", fireTime.toString()),
//                Map.entry("misfirePolicy", req.getMisfirePolicy().name())
//        ));
//    }
//    private static ZonedDateTime plus(ZonedDateTime base, org.quartz.DateBuilder.IntervalUnit unit, int n) {
//        return switch (unit) {
//            case SECOND -> base.plusSeconds(n);
//            case MINUTE -> base.plusMinutes(n);
//            case HOUR   -> base.plusHours(n);
//            case DAY    -> base.plusDays(n);
//            case WEEK   -> base.plusWeeks(n);
//            case MONTH  -> base.plusMonths(n);
//            case YEAR   -> base.plusYears(n);
//            default     -> base;
//        };
//    }
//
//    @PostMapping("/simprop-daily")
//    public ResponseEntity<?> simpropDaily(@RequestBody DailyRequest req) throws SchedulerException {
//        if (req.getDaysOfWeek() == null || req.getDaysOfWeek().isEmpty()) {
//            return ResponseEntity.badRequest().body(Map.of("error", "daysOfWeek is required"));
//        }
//        if (req.getStartTime() == null || req.getEndTime() == null) {
//            return ResponseEntity.badRequest().body(Map.of("error", "startTime and endTime are required"));
//        }
//        if (!req.getEndTime().isAfter(req.getStartTime())) {
//            return ResponseEntity.badRequest().body(Map.of("error", "endTime must be after startTime"));
//        }
//        if (req.getInterval() == null || req.getInterval() <= 0) {
//            return ResponseEntity.badRequest().body(Map.of("error", "interval must be > 0"));
//        }
//        if (req.getUnit() == null) {
//            return ResponseEntity.badRequest().body(Map.of("error", "unit is required (SECONDS/MINUTES/HOURS)"));
//        }
//
//        final ZoneId TH = ZoneId.of("Asia/Bangkok");
//        final ZoneId SCHED_ZONE = TimeZone.getDefault().toZoneId(); // โซนของ Scheduler/JVM
//        final String group = "CUSTOM";
//        final String key = UUID.randomUUID().toString();
//        final String trigName = "t_daily_" + key;
//
//        JobDetail jd = JobBuilder.newJob(MyQuartzJob.class)
//                .withIdentity(key, group)
//                .usingJobData("scheduleType", "Simprop Daily")
//                .usingJobData("title", nz(req.getNotificationTitle()))
//                .usingJobData("message", nz(req.getNotificationMessage()))
//                .storeDurably()
//                .build();
//
//        MappedWindow win = mapThaiWindowToScheduler(
//                req.getStartTime(), req.getEndTime(), req.getDaysOfWeek(), TH, SCHED_ZONE);
//
//        DailyTimeIntervalScheduleBuilder sb = DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule()
//                .startingDailyAt(win.start)
//                .endingDailyAt(win.end);
//
//        Integer[] quartzDows = Arrays.stream(win.dows).boxed().toArray(Integer[]::new);
//        sb = sb.onDaysOfTheWeek(quartzDows);
//
//        switch (req.getUnit()) {
//            case SECONDS -> sb = sb.withIntervalInSeconds(req.getInterval());
//            case MINUTES -> sb = sb.withIntervalInMinutes(req.getInterval());
//            case HOURS   -> sb = sb.withIntervalInHours(req.getInterval());
//        }
//
//        DailyRequest.MisfirePolicy mp = (req.getMisfirePolicy() == null)
//                ? DailyRequest.MisfirePolicy.FIRE_AND_PROCEED
//                : req.getMisfirePolicy();
//        switch (mp) {
//            case FIRE_AND_PROCEED -> sb = sb.withMisfireHandlingInstructionFireAndProceed();
//            case IGNORE_MISFIRES  -> sb = sb.withMisfireHandlingInstructionIgnoreMisfires();
//            case DO_NOTHING       -> sb = sb.withMisfireHandlingInstructionDoNothing();
//        }
//
//        TriggerBuilder<DailyTimeIntervalTrigger> tb = TriggerBuilder.newTrigger()
//                .withIdentity(trigName, group)
//                .forJob(jd)
//                .usingJobData("scheduleType", "Simprop Daily")
//                .usingJobData("title", nz(req.getNotificationTitle()))
//                .usingJobData("message", nz(req.getNotificationMessage()))
//                .withSchedule(sb);
//
//        if (req.getStartAt() != null) {
//            tb.startAt(Date.from(req.getStartAt().atZone(TH).toInstant()));
//        } else {
//            tb.startNow();
//        }
//        if (req.getEndAt() != null) {
//            tb.endAt(Date.from(req.getEndAt().atZone(TH).toInstant()));
//        }
//
//        Trigger daily = tb.build();
//        scheduler.scheduleJob(jd, daily);
//
//        ZonedDateTime nowSched = ZonedDateTime.now(SCHED_ZONE);
//        int todayQuartz = toQuartzDow(nowSched.getDayOfWeek());
//
//        boolean dayAllowed = Arrays.stream(win.dows).anyMatch(d -> d == todayQuartz);
//
//        int startSec = todToSecondOfDay(win.start);
//        int endSec   = todToSecondOfDay(win.end);
//        int nowSec   = nowSched.toLocalTime().toSecondOfDay();
//        boolean inWindow = nowSec >= startSec && nowSec <= endSec;
//
//        int intervalSec = switch (req.getUnit()) {
//            case SECONDS -> req.getInterval();
//            case MINUTES -> req.getInterval() * 60;
//            case HOURS   -> req.getInterval() * 3600;
//        };
//
//        int deltaFromStart = Math.max(0, nowSec - startSec); // ภายในวันเดียว
//        int remainder = (intervalSec == 0) ? 0 : (deltaFromStart % intervalSec);
//        int secUntilNextGrid = (remainder == 0) ? 0 : (intervalSec - remainder);
//
//        if (dayAllowed && inWindow && secUntilNextGrid > 0) {
//            Trigger kick = TriggerBuilder.newTrigger()
//                    .withIdentity("kick_" + key, group)
//                    .forJob(jd)
//                    .startNow()
//                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
//                            .withMisfireHandlingInstructionFireNow())
//                    .build();
//            scheduler.scheduleJob(kick);
//        }
//
//        return ResponseEntity.ok(Map.ofEntries(
//                Map.entry("type", "DailyTimeIntervalTrigger (simprop)"),
//                Map.entry("jobKey", jd.getKey().toString()),
//                Map.entry("triggerKey", daily.getKey().toString()),
//                Map.entry("daysOfWeekTH", req.getDaysOfWeek().stream().map(Enum::name).collect(Collectors.joining(","))),
//                Map.entry("startTimeTH", req.getStartTime().toString()),
//                Map.entry("endTimeTH", req.getEndTime().toString()),
//                Map.entry("mappedStartTimeScheduler", win.start.toString()),
//                Map.entry("mappedEndTimeScheduler", win.end.toString()),
//                Map.entry("schedulerZone", SCHED_ZONE.toString()),
//                Map.entry("interval", req.getInterval()),
//                Map.entry("unit", req.getUnit().name()),
//                Map.entry("startAt", daily.getStartTime()),
//                Map.entry("endAt", daily.getEndTime()),
//                Map.entry("misfirePolicy", mp.name()),
//                Map.entry("kickedNow", (dayAllowed && inWindow && secUntilNextGrid > 0))
//        ));
//    }
//    private static class MappedWindow {
//        final org.quartz.TimeOfDay start;
//        final org.quartz.TimeOfDay end;
//        final int[] dows; // Quartz DOW 1..7
//        MappedWindow(org.quartz.TimeOfDay start, org.quartz.TimeOfDay end, int[] dows) {
//            this.start = start; this.end = end; this.dows = dows;
//        }
//    }
//
//    private static MappedWindow mapThaiWindowToScheduler(
//            LocalTime thStart, LocalTime thEnd, List<java.time.DayOfWeek> thDows,
//            ZoneId thZone, ZoneId schedZone) {
//
//        LocalDate sample = LocalDate.now(thZone);
//
//        LocalTime schStartLT = ZonedDateTime.of(sample, thStart, thZone)
//                .withZoneSameInstant(schedZone)
//                .toLocalTime();
//        LocalTime schEndLT = ZonedDateTime.of(sample, thEnd, thZone)
//                .withZoneSameInstant(schedZone)
//                .toLocalTime();
//
//        int[] schDows = thDows.stream()
//                .map(d -> {
//                    LocalDate ref = sample.with(java.time.temporal.TemporalAdjusters.nextOrSame(d));
//                    java.time.DayOfWeek schDow = ZonedDateTime.of(ref, thStart, thZone)
//                            .withZoneSameInstant(schedZone)
//                            .getDayOfWeek();
//                    return toQuartzDow(schDow);
//                })
//                .distinct()
//                .sorted()
//                .mapToInt(Integer::intValue)
//                .toArray();
//
//        org.quartz.TimeOfDay schStart = org.quartz.TimeOfDay.hourMinuteAndSecondOfDay(
//                schStartLT.getHour(), schStartLT.getMinute(), schStartLT.getSecond());
//        org.quartz.TimeOfDay schEnd = org.quartz.TimeOfDay.hourMinuteAndSecondOfDay(
//                schEndLT.getHour(), schEndLT.getMinute(), schEndLT.getSecond());
//
//        return new MappedWindow(schStart, schEnd, schDows);
//    }
//
//    private static int todToSecondOfDay(org.quartz.TimeOfDay tod) {
//        return tod.getHour() * 3600 + tod.getMinute() * 60 + tod.getSecond();
//    }
//
//    private static int toQuartzDow(java.time.DayOfWeek d) {
//        return switch (d) {
//            case MONDAY    -> DateBuilder.MONDAY;
//            case TUESDAY   -> DateBuilder.TUESDAY;
//            case WEDNESDAY -> DateBuilder.WEDNESDAY;
//            case THURSDAY  -> DateBuilder.THURSDAY;
//            case FRIDAY    -> DateBuilder.FRIDAY;
//            case SATURDAY  -> DateBuilder.SATURDAY;
//            case SUNDAY    -> DateBuilder.SUNDAY;
//        };
//    }
//
//    @PostMapping("/send-now")
//    public ResponseEntity<?> sendNow(@RequestBody(required = false) SendNowRequest req) throws Exception {
//        ZonedDateTime nowTh = ZonedDateTime.now(BANGKOK);
//        System.out.println("🔥 Quartz Job Executed (TH): " + nowTh.format(TH_FMT)+" Type: Send Now"+" Title:"+req.getNotificationTitle());
//
//        NotificationConfigEntity e = new NotificationConfigEntity();
//        e.setRecipientType(req.getRecipientType().toString());
//        e.setSendingType("Send_now");
//        e.setSendTime(LocalDateTime.now(BANGKOK));
//        e.setStatus("A");
//        e.setCreatedBy("system");
//        e.setCreatedDate(LocalDateTime.now(BANGKOK));
//        e.setNotificationGroup(req.getNotificationGroup());
//        e.setNotificationType(req.getNotificationType());
//        notifyRepo.save(e);
//
//        NotificationConfigTlId notificationConfigTlId = new NotificationConfigTlId();
//        notificationConfigTlId
//                .setNotificationConfigId(e.getNotificationConfigId())
//                .setLanguage_name("EN");
//
//        NotificationConfigTlEntity notificationConfigTlEntity = new NotificationConfigTlEntity();
//        notificationConfigTlEntity
//                .setNotificationTitle(req.getNotificationTitle())
//                .setId(notificationConfigTlId)
//                .setNotificationMessage(req.getNotificationMessage());
//
//        notificationConfigTlRepository.save(notificationConfigTlEntity);
//
//        return ResponseEntity.ok(Map.of(
//                "Title", req.getNotificationTitle(),
//                "message", req.getNotificationMessage()
//        ));
//    }
//
//    @PostMapping("/schedule")
//    public ResponseEntity<?> schedule(@RequestBody ScheduleRequest req) throws Exception {
//        if (req.getScheduledAt() == null) {
//            return ResponseEntity.badRequest().body(Map.of("error", "scheduledAt is required"));
//        }
//        Date fireAt = Date.from(req.getScheduledAt().atZone(BANGKOK).toInstant());
//        if (fireAt.before(new Date())) {
//            return ResponseEntity.badRequest().body(Map.of("error","scheduledAt must be in the future"));
//        }
//
//        String key = UUID.randomUUID().toString();
//        JobDetail job = JobBuilder.newJob(MyQuartzJob.class)
//                .withIdentity(key, "CUSTOM")
//                .storeDurably()
//                .build();
//
//        String trigKey = key + "_trigger";
//        Trigger trigger = TriggerBuilder.newTrigger()
//                .withIdentity(trigKey, "CUSTOM")
//                .forJob(job)
//                .usingJobData("scheduleType", "Schedule")
//                .usingJobData("title",req.getNotificationTitle())
//                .startAt(fireAt)
//                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
//                        .withMisfireHandlingInstructionFireNow())
//                .build();
//
//        scheduler.scheduleJob(job, trigger);
//
//        NotificationConfigEntity e = new NotificationConfigEntity();
//        e.setRecipientType(req.getRecipientType().toString());
//        e.setSendingType("Schedule");
//        e.setSendTime(req.getScheduledAt());
//        e.setStatus("A");
//        e.setCreatedBy("system");
//        e.setCreatedDate(LocalDateTime.now(BANGKOK));
//        e.setNotificationGroup(req.getNotificationGroup());
//        e.setNotificationType(req.getNotificationType());
//        notifyRepo.save(e);
//
//        NotificationConfigTlId notificationConfigTlId = new NotificationConfigTlId();
//        notificationConfigTlId
//                .setNotificationConfigId(e.getNotificationConfigId())
//                .setLanguage_name("EN");
//
//        NotificationConfigTlEntity notificationConfigTlEntity = new NotificationConfigTlEntity();
//        notificationConfigTlEntity
//                .setNotificationTitle(req.getNotificationTitle())
//                .setId(notificationConfigTlId)
//                .setNotificationMessage(req.getNotificationMessage());
//        notificationConfigTlRepository.save(notificationConfigTlEntity);
//        return ResponseEntity.ok(Map.of(
//                "Title", req.getNotificationTitle(),
//                "message", req.getNotificationMessage(),
//                "jobKey", key,
//                "triggerKey", trigKey,
//                "scheduled", req.getScheduledAt().toString()
//        ));
//    }
//
//    @PostMapping("/recurring")
//    public ResponseEntity<?> recurring(@RequestBody RecurringRequest req) throws Exception {
//        String cron = CronPattern.from(req);
//        if (!CronExpression.isValidExpression(cron)) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Invalid cron", "cron", cron));
//        }
//
//        String jobKey = UUID.randomUUID().toString();
//        String triggerKey = jobKey + "_trigger";
//
//        JobDetail job = JobBuilder.newJob(MyQuartzJob.class)
//                .withIdentity(jobKey, "CUSTOM")
//                .storeDurably()
//                .build();
//
//        TriggerBuilder<CronTrigger> tb = TriggerBuilder.newTrigger()
//                .withIdentity(triggerKey, "CUSTOM")
//                .forJob(job)
//                .usingJobData("scheduleType", "Recurring")
//                .usingJobData("title",req.getNotificationTitle())
//                .withSchedule(
//                        CronScheduleBuilder.cronSchedule(cron)
//                                .inTimeZone(TimeZone.getTimeZone(BANGKOK))
//                                .withMisfireHandlingInstructionDoNothing()
//                );
//
//        if (req.getStartDateTime() != null) {
//            tb.startAt(Date.from(req.getStartDateTime().atZone(BANGKOK).toInstant()));
//        }
//        if (req.getEndDateTime() != null) {
//            tb.endAt(Date.from(req.getEndDateTime().atZone(BANGKOK).toInstant()));
//        }
//
//        scheduler.scheduleJob(job, tb.build());
//
//        NotificationConfigEntity notify = new NotificationConfigEntity()
//                .setRecipientType(req.getRecipientType().toString())
//                .setRecurringType(String.valueOf(req.getRecurrenceType()))
//                .setEffectiveStartDate(req.getStartDateTime())
//                .setEffectiveEndDate(req.getEndDateTime())
//                .setRecurTime(req.getTime())
//                .setRecurDay(req.getDayOfMonth())
//                .setRecurDayOfWeek(listToCsv(req.getDaysOfWeek()))
//                .setRecurMonth(req.getMonth() != null ? req.getMonth().getValue() : null)
//                .setNotificationType(req.getNotificationType())
//                .setNotificationGroup(req.getNotificationGroup())
//                .setSendingType("Recurring")
//                .setCreatedBy("system")
//                .setCreatedDate(LocalDateTime.now(BANGKOK))
//                .setSendTime(LocalDateTime.now(BANGKOK))
//                .setStatus("A");
//        notifyRepo.save(notify);
//
//        NotificationConfigTlId notificationConfigTlId = new NotificationConfigTlId();
//        notificationConfigTlId
//                .setNotificationConfigId(notify.getNotificationConfigId())
//                .setLanguage_name("EN");
//
//        NotificationConfigTlEntity notificationConfigTlEntity = new NotificationConfigTlEntity();
//        notificationConfigTlEntity
//                .setId(notificationConfigTlId)
//                .setNotificationTitle(req.getNotificationTitle())
//                .setNotificationMessage(req.getNotificationMessage());
//
//        notificationConfigTlRepository.save(notificationConfigTlEntity);
//
//        return ResponseEntity.ok(Map.of(
//                "Title", req.getNotificationTitle(),
//                "message", req.getNotificationMessage(),
//                "jobKey", jobKey,
//                "triggerKey", triggerKey,
//                "cron", cron
//        ));
//    }
//
//    @PostMapping("/cron-expression-trigger")
//    public ResponseEntity<?> preview(@RequestBody CronExRequest req) {
//        if (req.getCronExpression() == null || !CronExpression.isValidExpression(req.getCronExpression())) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "error", "Invalid cron expression",
//                    "cron", req.getCronExpression()
//            ));
//        }
//        if (req.getStartDate() == null) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "error", "startDate (LocalDateTime) is required"
//            ));
//        }
//
//        ZonedDateTime startZ = req.getStartDate().atZone(BANGKOK);
//        ZonedDateTime endZ = (req.getEndDate() != null) ? req.getEndDate().atZone(BANGKOK) : null;
//        if (endZ != null && !endZ.isAfter(startZ)) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "error", "endDate must be after startDate"
//            ));
//        }
//
//        String jobKeyStr = UUID.randomUUID().toString();
//        String triggerKeyStr = jobKeyStr + "_trigger";
//
//        try {
//            System.out.println(org.quartz.CronExpression.class
//                    .getProtectionDomain().getCodeSource().getLocation());
//            System.out.println(org.quartz.impl.triggers.CronTriggerImpl.class
//                    .getProtectionDomain().getCodeSource().getLocation());
//
//            CronTrigger tmp = TriggerBuilder.newTrigger()
//                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0 9 L * ?")
//                            .inTimeZone(TimeZone.getTimeZone("Asia/Bangkok")))
//                    .build();
//            System.out.println("parsed cron = " + ((CronTrigger) tmp).getCronExpression());
//
//            JobDetail job = JobBuilder.newJob(MyQuartzJob.class)
//                    .withIdentity(jobKeyStr, "CUSTOM")
//                    .withDescription("Scheduled from API /api/cron/schedule")
//                    .usingJobData("scheduleType", "API")
//                    .storeDurably(false)
//                    .build();
//
//            TriggerBuilder<CronTrigger> tb = TriggerBuilder.newTrigger()
//                    .withIdentity(triggerKeyStr, "CUSTOM")
//                    .forJob(job)
//                    .withSchedule(
//                            CronScheduleBuilder.cronSchedule(req.getCronExpression())
//                                    .inTimeZone(TimeZone.getTimeZone(BANGKOK))
//                                    .withMisfireHandlingInstructionDoNothing()
//                    );
//            tb.startAt(Date.from(startZ.toInstant()));
//            if (endZ != null) {
//                tb.endAt(Date.from(endZ.toInstant()));
//            }
//
//            CronTrigger trigger = tb.build();
//
//            Date firstFireAt = scheduler.scheduleJob(job, trigger);
//
//            TriggerKey tk = trigger.getKey();
//            CronTrigger saved = (CronTrigger) scheduler.getTrigger(tk);
//
//            Map<String, Object> resp = new LinkedHashMap<>();
//            resp.put("jobKey", job.getKey().getName());
//            resp.put("jobGroup", job.getKey().getGroup());
//            resp.put("triggerKey", saved.getKey().getName());
//            resp.put("triggerGroup", saved.getKey().getGroup());
//            resp.put("cron", saved.getCronExpression());
//            resp.put("timezone", saved.getTimeZone().getID());
//            resp.put("startAt", TH_FMT.format(startZ));
//            resp.put("endAt", (endZ != null) ? TH_FMT.format(endZ) : null);
//            resp.put("firstFireAt", (firstFireAt != null)
//                    ? TH_FMT.format(ZonedDateTime.ofInstant(firstFireAt.toInstant(), BANGKOK)) : null);
//            resp.put("nextFireTime", (saved.getNextFireTime() != null)
//                    ? TH_FMT.format(ZonedDateTime.ofInstant(saved.getNextFireTime().toInstant(), BANGKOK)) : null);
//            resp.put("previousFireTime", (saved.getPreviousFireTime() != null)
//                    ? TH_FMT.format(ZonedDateTime.ofInstant(saved.getPreviousFireTime().toInstant(), BANGKOK)) : null);
//            resp.put("state", scheduler.getTriggerState(tk).name());
//            resp.put("misfireInstruction", saved.getMisfireInstruction());
//
//            return ResponseEntity.ok(resp);
//        } catch (SchedulerException e) {
//            return ResponseEntity.internalServerError().body(Map.of(
//                    "error", "Schedule failed",
//                    "message", e.getMessage()
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "error", "Failed to build job/trigger",
//                    "message", e.getMessage()
//            ));
//        }
//
//    }
//
//    @PutMapping("/jobs/{group}/{name}/pause")
//    public ResponseEntity<?> pauseJob(@PathVariable String group, @PathVariable String name) throws Exception {
//        JobKey jk = JobKey.jobKey(name, group);
//        if (!scheduler.checkExists(jk))
//            return ResponseEntity.status(404).body(Map.of("error", "Job not found"));
//        scheduler.pauseJob(jk);
//        return ResponseEntity.ok(Map.of("message", "Paused", "job", name, "group", group));
//    }
//
//    @PutMapping("/jobs/{group}/{name}/resume")
//    public ResponseEntity<?> resumeJob(@PathVariable String group, @PathVariable String name) throws Exception {
//        JobKey jk = JobKey.jobKey(name, group);
//        if (!scheduler.checkExists(jk))
//            return ResponseEntity.status(404).body(Map.of("error", "Job not found"));
//        scheduler.resumeJob(jk);
//        return ResponseEntity.ok(Map.of("message", "Resumed", "job", name, "group", group));
//    }
//
//    @PutMapping("/trigger/{group}/{name}/pause")
//    public ResponseEntity<?> pauseTrigger(@PathVariable String group, @PathVariable String name) throws SchedulerException {
//        TriggerKey tk = TriggerKey.triggerKey(name, group);
//        Trigger t = scheduler.getTrigger(tk);
//        if (t == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));
//
//        scheduler.pauseTrigger(tk);
//        Trigger.TriggerState st = scheduler.getTriggerState(tk);
//        return ResponseEntity.ok(Map.of("message","Paused trigger", "trigger", name, "group", group, "state", st.name()));
//    }
//
//    @PutMapping("/trigger/{group}/{name}/resume")
//    public ResponseEntity<?> resumeTrigger(@PathVariable String group, @PathVariable String name) throws SchedulerException {
//        TriggerKey tk = TriggerKey.triggerKey(name, group);
//        Trigger t = scheduler.getTrigger(tk);
//        if (t == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));
//
//        scheduler.resumeTrigger(tk);
//        Trigger.TriggerState st = scheduler.getTriggerState(tk);
//        return ResponseEntity.ok(Map.of("message","Resumed trigger", "trigger", name, "group", group, "state", st.name()));
//    }
//
//    @PutMapping("/triggers/{group}/{name}/reschedule/recurring")
//    public ResponseEntity<?> rescheduleRecurring(@PathVariable String group,
//                                                 @PathVariable String name,
//                                                 @RequestBody RecurringRequest req) throws SchedulerException {
//        TriggerKey tk = TriggerKey.triggerKey(name, group);
//        Trigger old = scheduler.getTrigger(tk);
//        if (old == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));
//
//        final String cron;
//        try { cron = CronPattern.from(req); }
//        catch (IllegalArgumentException ex) {
//            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
//        }
//        if (!CronExpression.isValidExpression(cron))
//            return ResponseEntity.badRequest().body(Map.of("error","Invalid cron generated", "cron", cron));
//
//        TimeZone tz = TimeZone.getTimeZone(BANGKOK);
//        TriggerBuilder<CronTrigger> tb = TriggerBuilder.newTrigger()
//                .withIdentity(tk)
//                .forJob(old.getJobKey())
//                .withSchedule(CronScheduleBuilder.cronSchedule(cron)
//                        .inTimeZone(tz)
//                        .withMisfireHandlingInstructionDoNothing());
//
//        if (req.getStartDateTime()!=null) tb.startAt(Date.from(req.getStartDateTime().atZone(BANGKOK).toInstant()));
//        if (req.getEndDateTime()!=null)   tb.endAt(Date.from(req.getEndDateTime().atZone(BANGKOK).toInstant()));
//
//        Date next = scheduler.rescheduleJob(tk, tb.build());
//
//        NotificationConfigEntity e = new NotificationConfigEntity();
//        e.setSendingType("Recurring");
//        e.setRecurringType(String.valueOf(req.getRecurrenceType()));
//        e.setEffectiveStartDate(req.getStartDateTime());
//        e.setEffectiveEndDate(req.getEndDateTime());
//        e.setRecurTime(req.getTime());
//        e.setRecurDay(req.getDayOfMonth());
//        e.setRecurDayOfWeek(req.getDaysOfWeek() == null ? null : req.getDaysOfWeek().stream()
//                .map(DayOfWeek::name)
//                .collect(Collectors.joining(",")));
//        e.setRecurMonth(req.getMonth() != null ? req.getMonth().getValue() : null);
//        e.setStatus("A");
//        e.setUpdatedBy("system");
//        e.setUpdatedDate(LocalDateTime.now(BANGKOK));
//        notifyRepo.save(e);
//
//        return ResponseEntity.ok(Map.of(
//                "message", "Rescheduled (recurring)",
//                "cron", cron,
//                "nextFireTime", next
//        ));
//    }
//
//    @PutMapping("/triggers/{group}/{name}/reschedule/schedule")
//    public ResponseEntity<?> rescheduleOnce(@PathVariable String group, @PathVariable String name,
//                                            @RequestBody RescheduleOnceRequest req) throws Exception {
//        if (req.getFireAt()==null) {
//            return ResponseEntity.badRequest().body(Map.of("error","fireAt is required"));
//        }
//        TriggerKey tk = TriggerKey.triggerKey(name, group);
//        Trigger old = scheduler.getTrigger(tk);
//        if (old == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));
//
//        JobDetail jd = scheduler.getJobDetail(old.getJobKey());
//        Trigger newTrig = TriggerBuilder.newTrigger()
//                .withIdentity(tk)
//                .forJob(jd)
//                .startAt(new Date(req.getFireAt().getTime()))
//                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
//                        .withMisfireHandlingInstructionFireNow())
//                .build();
//
//        Date next = scheduler.rescheduleJob(tk, newTrig);
//
//        NotificationConfigEntity e = new NotificationConfigEntity();
//        e.setSendingType("Schedule");
//        e.setSendTime(req.getFireAt().toInstant().atZone(BANGKOK).toLocalDateTime());
//        e.setStatus("A");
//        e.setUpdatedBy("system");
//        e.setUpdatedDate(LocalDateTime.now(BANGKOK));
//        notifyRepo.save(e);
//
//        return ResponseEntity.ok(Map.of(
//                "message","Rescheduled (once)",
//                "nextFireTime", next
//        ));
//    }
//
//    @DeleteMapping("/jobs/{group}/{name}")
//    public ResponseEntity<?> deleteJob(@PathVariable String group, @PathVariable String name) throws Exception {
//        JobKey jk = JobKey.jobKey(name, group);
//        boolean removed = scheduler.deleteJob(jk);
//        if (!removed) return ResponseEntity.status(404).body(Map.of("error","Job not found"));
//        return ResponseEntity.ok(Map.of("message","Deleted job", "job", name, "group", group));
//    }
//
//    @DeleteMapping("/triggers/{group}/{name}")
//    public ResponseEntity<?> deleteTrigger(@PathVariable String group, @PathVariable String name) throws Exception {
//        TriggerKey tk = TriggerKey.triggerKey(name, group);
//        boolean removed = scheduler.unscheduleJob(tk);
//        if (!removed) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));
//        return ResponseEntity.ok(Map.of("message","Unschedule trigger", "trigger", name, "group", group));
//    }
//
//    @GetMapping("/jobs")
//    public ResponseEntity<?> listJobs() throws SchedulerException {
//        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyGroup());
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        for (JobKey jk : jobKeys) {
//            JobDetail jd = scheduler.getJobDetail(jk);
//            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jk);
//
//            Map<String, Object> jv = new LinkedHashMap<>();
//            jv.put("name", jk.getName());
//            jv.put("group", jk.getGroup());
//            jv.put("jobData", new LinkedHashMap<>(jd.getJobDataMap()));
//
//            List<Map<String, Object>> trigViews = new ArrayList<>();
//            for (Trigger t : triggers) {
//                try {
//                    trigViews.add(triggerView(t, scheduler));
//                } catch (SchedulerException ignored) {}
//            }
//            jv.put("triggers", trigViews);
//
//            result.add(jv);
//        }
//
//        result.sort(Comparator
//                .comparing((Map<String, Object> m) -> (String) m.get("group"))
//                .thenComparing(m -> (String) m.get("name")));
//
//        return ResponseEntity.ok(result);
//    }
//
//    @GetMapping("/triggers")
//    public ResponseEntity<?> listTriggers(@RequestParam(required = false) String group,
//                                          @RequestParam(required = false) String name) throws SchedulerException {
//
//        if (notBlank(group) && notBlank(name)) {
//            TriggerKey tk = TriggerKey.triggerKey(name, group);
//            Trigger t = scheduler.getTrigger(tk);
//            if (t == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));
//            return ResponseEntity.ok(triggerView(t, scheduler));
//        }
//
//        Set<TriggerKey> keys;
//        if (notBlank(group)) {
//            keys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group));
//        } else {
//            keys = scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
//        }
//
//        List<Map<String, Object>> out = new ArrayList<>();
//        for (TriggerKey tk : keys) {
//            if (notBlank(name) && !tk.getName().equals(name)) continue; // กรองชื่อถ้ามี
//            Trigger t = scheduler.getTrigger(tk);
//            if (t == null) continue;
//            out.add(triggerView(t, scheduler));
//        }
//
//        out.sort(Comparator
//                .comparing((Map<String, Object> m) -> (String) m.get("group"))
//                .thenComparing(m -> (String) m.get("name")));
//
//        return ResponseEntity.ok(out);
//    }
//
//
//    private static boolean notBlank(String s) {
//        return s != null && !s.isBlank();
//    }
//
//    private static Map<String, Object> triggerView(Trigger t, Scheduler scheduler) throws SchedulerException {
//        Map<String, Object> m = new LinkedHashMap<>();
//        TriggerKey tk = t.getKey();
//
//        m.put("name", tk.getName());
//        m.put("group", tk.getGroup());
//        m.put("jobName", t.getJobKey().getName());
//        m.put("jobGroup", t.getJobKey().getGroup());
//        m.put("type", t.getClass().getSimpleName());
//        m.put("state", scheduler.getTriggerState(tk).name());
//        m.put("priority", t.getPriority());
//        m.put("startAt", t.getStartTime());
//        m.put("endAt", t.getEndTime());
//        m.put("prevFireTime", t.getPreviousFireTime());
//        m.put("nextFireTime", t.getNextFireTime());
//        m.put("misfireInstruction", t.getMisfireInstruction());
//        m.put("description", t.getDescription());
//
//        if (t instanceof CronTrigger ct) {
//            m.put("cronExpression", ct.getCronExpression());
//            m.put("timeZone", ct.getTimeZone() != null ? ct.getTimeZone().getID() : null);
//        } else if (t instanceof SimpleTrigger st) {
//            m.put("repeatIntervalMs", st.getRepeatInterval());
//            m.put("repeatCount", st.getRepeatCount());
//            m.put("timesTriggered", st.getTimesTriggered());
//        }
//        return m;
//    }
//
//    @GetMapping("/triggers/cron")
//    public ResponseEntity<?> listCronTriggers(@RequestParam(required = false) String group,
//                                              @RequestParam(required = false) String name) throws SchedulerException {
//        // ถ้าระบุทั้ง group และ name -> ตัวเดียว
//        if (notBlank(group) && notBlank(name)) {
//            TriggerKey tk = TriggerKey.triggerKey(name, group);
//            Trigger t = scheduler.getTrigger(tk);
//            if (t == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));
//            if (!(t instanceof CronTrigger ct)) {
//                return ResponseEntity.badRequest().body(Map.of("error","Not a CronTrigger", "actualType", t.getClass().getSimpleName()));
//            }
//            return ResponseEntity.ok(cronTriggerView(ct, scheduler));
//        }
//
//        // เลือก key ตาม group
//        Set<TriggerKey> keys = notBlank(group)
//                ? scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group))
//                : scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
//
//        List<Map<String,Object>> out = new ArrayList<>();
//        for (TriggerKey tk : keys) {
//            if (notBlank(name) && !tk.getName().equals(name)) continue;
//            Trigger t = scheduler.getTrigger(tk);
//            if (t instanceof CronTrigger ct) {
//                out.add(cronTriggerView(ct, scheduler));
//            }
//        }
//        out.sort(Comparator
//                .comparing((Map<String,Object> m) -> (String)m.get("group"))
//                .thenComparing(m -> (String)m.get("name")));
//        return ResponseEntity.ok(out);
//    }
//
//    @GetMapping("/triggers/simple")
//    public ResponseEntity<?> listSimpleTriggers(@RequestParam(required = false) String group,
//                                                @RequestParam(required = false) String name) throws SchedulerException {
//        if (notBlank(group) && notBlank(name)) {
//            TriggerKey tk = TriggerKey.triggerKey(name, group);
//            Trigger t = scheduler.getTrigger(tk);
//            if (t == null) return ResponseEntity.status(404).body(Map.of("error","Trigger not found"));
//            if (!(t instanceof SimpleTrigger st)) {
//                return ResponseEntity.badRequest().body(Map.of("error","Not a SimpleTrigger", "actualType", t.getClass().getSimpleName()));
//            }
//            return ResponseEntity.ok(simpleTriggerView(st, scheduler));
//        }
//
//        Set<TriggerKey> keys = notBlank(group)
//                ? scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group))
//                : scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
//
//        List<Map<String,Object>> out = new ArrayList<>();
//        for (TriggerKey tk : keys) {
//            if (notBlank(name) && !tk.getName().equals(name)) continue;
//            Trigger t = scheduler.getTrigger(tk);
//            if (t instanceof SimpleTrigger st) {
//                out.add(simpleTriggerView(st, scheduler));
//            }
//        }
//        out.sort(Comparator
//                .comparing((Map<String,Object> m) -> (String)m.get("group"))
//                .thenComparing(m -> (String)m.get("name")));
//        return ResponseEntity.ok(out);
//    }
//
//    private static Map<String,Object> cronTriggerView(CronTrigger ct, Scheduler scheduler) throws SchedulerException {
//        Map<String,Object> m = new LinkedHashMap<>();
//        TriggerKey tk = ct.getKey();
//        m.put("name", tk.getName());
//        m.put("group", tk.getGroup());
//        m.put("jobName", ct.getJobKey().getName());
//        m.put("jobGroup", ct.getJobKey().getGroup());
//        m.put("type", "CronTrigger");
//        m.put("state", scheduler.getTriggerState(tk).name());
//        m.put("priority", ct.getPriority());
//        m.put("cronExpression", ct.getCronExpression());
//        m.put("timeZone", ct.getTimeZone()!=null ? ct.getTimeZone().getID() : null);
//        m.put("startAt", ct.getStartTime());
//        m.put("endAt", ct.getEndTime());
//        m.put("prevFireTime", ct.getPreviousFireTime());
//        m.put("nextFireTime", ct.getNextFireTime());
//        m.put("misfireInstruction", ct.getMisfireInstruction());
//        m.put("description", ct.getDescription());
//        return m;
//    }
//
//    private static Map<String,Object> simpleTriggerView(SimpleTrigger st, Scheduler scheduler) throws SchedulerException {
//        Map<String,Object> m = new LinkedHashMap<>();
//        TriggerKey tk = st.getKey();
//        m.put("name", tk.getName());
//        m.put("group", tk.getGroup());
//        m.put("jobName", st.getJobKey().getName());
//        m.put("jobGroup", st.getJobKey().getGroup());
//        m.put("type", "SimpleTrigger");
//        m.put("state", scheduler.getTriggerState(tk).name());
//        m.put("priority", st.getPriority());
//        m.put("startAt", st.getStartTime());
//        m.put("endAt", st.getEndTime());
//        m.put("prevFireTime", st.getPreviousFireTime());
//        m.put("nextFireTime", st.getNextFireTime());
//        m.put("misfireInstruction", st.getMisfireInstruction());
//        m.put("repeatIntervalMs", st.getRepeatInterval());
//        m.put("repeatCount", st.getRepeatCount());
//        m.put("timesTriggered", st.getTimesTriggered());
//        m.put("description", st.getDescription());
//        return m;
//    }
//
//}
