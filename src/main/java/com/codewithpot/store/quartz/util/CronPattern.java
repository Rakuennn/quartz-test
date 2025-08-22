package com.codewithpot.store.quartz.util;
import com.codewithpot.store.quartz.Entity.NotificationConfigEntity;
import com.codewithpot.store.quartz.dto.RecurringRequest;


import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CronPattern {
    private CronPattern() {}

    public static String from(RecurringRequest req) {
        Objects.requireNonNull(req.getRecurrenceType(), "recurrenceType is required");

        LocalTime t = (req.getTime() != null) ? req.getTime() : LocalTime.of(9, 0);
        int s = t.getSecond();
        int m = t.getMinute();
        int h = t.getHour();

        String typ = req.getRecurrenceType().name().toUpperCase(Locale.ROOT);
        return switch (typ) {
            case "DAILY" -> daily(s, m, h);
            case "WEEKLY" -> weekly(s, m, h, requireDaysOfWeek(req.getDaysOfWeek()));
            case "MONTHLY" -> monthly(s, m, h, requireDayOfMonth(req.getDayOfMonth()));
            case "YEARLY" -> yearly(s, m, h, requireDayOfMonth(req.getDayOfMonth()), requireMonth(req.getMonth()));
            default -> throw new IllegalArgumentException("Unsupported recurrenceType: " + typ);
        };
    }

    public static String from(NotificationConfigEntity n) {
        String type = nz(n.getRecurringType()).toUpperCase(Locale.ROOT);

        LocalTime t = (n.getRecurTime() != null) ? n.getRecurTime() : LocalTime.of(9, 0);
        int s = t.getSecond();
        int m = t.getMinute();
        int h = t.getHour();

        return switch (type) {
            case "DAILY" -> daily(s, m, h);

            case "WEEKLY" -> {
                List<DayOfWeek> dows = parseDaysOfWeekCsv(n.getRecurDayOfWeek());
                if (dows.isEmpty()) {
                    throw new IllegalArgumentException("recurDayOfWeek is required for WEEKLY");
                }
                yield weekly(s, m, h, dows);
            }

            case "MONTHLY" -> {
                Integer dom = n.getRecurDay();
                yield monthly(s, m, h, requireDayOfMonth(dom));
            }

            case "YEARLY" -> {
                Integer dom = n.getRecurDay();
                Integer mon = n.getRecurMonth();
                yield yearly(s, m, h, requireDayOfMonth(dom), requireMonth(mon));
            }

            default -> throw new IllegalArgumentException("Unsupported recuringType: " + type);
        };
    }

    private static String daily(int s, int m, int h) {
        return String.format("%d %d %d ? * *", s, m, h);
    }

    private static String weekly(int s, int m, int h, List<DayOfWeek> dows) {
        String list = dows.stream()
                .map(CronPattern::toQuartzDowText)
                .collect(Collectors.joining(","));
        return String.format("%d %d %d ? * %s", s, m, h, list);
    }

    private static String monthly(int s, int m, int h, int dom) {
        return String.format("%d %d %d %d * ?", s, m, h, dom);
    }

    private static String yearly(int s, int m, int h, int dom, Month month) {
        return String.format("%d %d %d %d %d ?", s, m, h, dom, month.getValue());
    }

    private static String yearly(int s, int m, int h, int dom, int month1to12) {
        return String.format("%d %d %d %d %d ?", s, m, h, dom, month1to12);
    }

    private static List<DayOfWeek> requireDaysOfWeek(List<DayOfWeek> dows) {
        if (dows == null || dows.isEmpty()) {
            throw new IllegalArgumentException("daysOfWeek is required for WEEKLY");
        }
        return dows;
    }

    private static int requireDayOfMonth(Integer dom) {
        if (dom == null) throw new IllegalArgumentException("dayOfMonth is required");
        if (dom < 1 || dom > 31) throw new IllegalArgumentException("dayOfMonth must be 1..31");
        return dom;
    }

    private static Month requireMonth(Month m) {
        if (m == null) throw new IllegalArgumentException("month is required for YEARLY");
        return m;
    }

    private static Month requireMonth(Integer month1to12) {
        if (month1to12 == null) throw new IllegalArgumentException("recurMonth is required for YEARLY");
        if (month1to12 < 1 || month1to12 > 12) throw new IllegalArgumentException("recurMonth must be 1..12");
        return Month.of(month1to12);
    }

    private static String toQuartzDowText(DayOfWeek dow) {
        return dow.name().substring(0, 3).toUpperCase(Locale.ROOT);
    }

    private static List<DayOfWeek> parseDaysOfWeekCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        String cleaned = csv.replace("[", "").replace("]", "").trim();
        if (cleaned.isBlank()) return List.of();
        String[] tokens = cleaned.split("[,\\s]+");

        List<DayOfWeek> out = new ArrayList<>();
        for (String t : tokens) {
            if (t.isBlank()) continue;
            String u = t.trim().toUpperCase(Locale.ROOT);
            DayOfWeek dow = switch (u) {
                case "MON" -> DayOfWeek.MONDAY;
                case "TUE" -> DayOfWeek.TUESDAY;
                case "WED" -> DayOfWeek.WEDNESDAY;
                case "THU" -> DayOfWeek.THURSDAY;
                case "FRI" -> DayOfWeek.FRIDAY;
                case "SAT" -> DayOfWeek.SATURDAY;
                case "SUN" -> DayOfWeek.SUNDAY;
                default -> DayOfWeek.valueOf(u);
            };
            out.add(dow);
        }
        return out;
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
