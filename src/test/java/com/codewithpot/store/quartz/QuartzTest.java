package com.codewithpot.store.quartz;

import org.junit.jupiter.api.Test;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.TriggerUtils;
import org.quartz.Calendar; // quartz.Calendar
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class QuartzTest {
    @Test
    void compute_between_shows_no_feb_fire() throws Exception {
        CronTriggerImpl ct = new CronTriggerImpl();
        ct.setName("t");
        ct.setGroup("CUSTOM");
        ct.setCronExpression("0 0 9 31 * ?");
        ct.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
        ct.setStartTime(new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-01"));

        Date from = new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-01");
        Date to   = new SimpleDateFormat("yyyy-MM-dd").parse("2025-04-01");

        List<Date> fires = TriggerUtils.computeFireTimesBetween(ct, (Calendar) null, from, to);

        // ควรได้ 31 ม.ค. และ 31 มี.ค. (ไม่มี ก.พ.)
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<String> asText = fires.stream().map(fmt::format).toList();

        assertThat(asText).contains("2025-01-31 09:00", "2025-03-31 09:00");
        assertThat(asText).doesNotContain("2025-02-28 09:00", "2025-02-29 09:00");
    }
}