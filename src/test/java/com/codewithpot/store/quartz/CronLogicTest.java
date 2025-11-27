package com.codewithpot.store.quartz;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;
import static org.junit.jupiter.api.Assertions.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CronLogicTest {
    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Test
    void last_day_of_month_non_leap_year_2025() throws Exception {
        String cron = "0 0 9 L * ?";
        assertTrue(CronExpression.isValidExpression(cron));
        CronExpression expr = new CronExpression(cron);

//        Date cursor = fmt.parse("2025-01-01 00:00");
        List<String> got = new ArrayList<>();
//        for (int i = 0; i < 6; i++) {
//            got.add(fmt.format("2025-04-01 00:00"));
//            got.add(fmt.format("2025-02-01 00:00"));
//
//            Date next = expr.getNextValidTimeAfter(cursor);
//            got.add(fmt.format(next));
//            cursor = new Date(next.getTime() + 1);
//        }

        Date date1 = fmt.parse("2025-02-01 01:00");
        Date next1 =expr.getNextValidTimeAfter(date1);
        got.add(fmt.format(next1));

        Date date2 = fmt.parse("2025-04-01 01:00");
        Date next2 = expr.getNextValidTimeAfter(date2);
        got.add(fmt.format(next2));

        Date date3 = fmt.parse("2024-02-01 01:00");
        Date next3 = expr.getNextValidTimeAfter(date3);
        got.add(fmt.format(next3));

        System.out.print(got);
        Assertions.assertThat(got).containsExactly(
                "2025-02-28 09:00",
                "2025-04-30 09:00",
                "2024-02-29 09:00"
        );
    }

    @Test
    void day31_skips_months_without_31st() throws Exception {
        String cron = "0 0 9 31 * ?";
        assertTrue(CronExpression.isValidExpression(cron));

        CronExpression expr = new CronExpression(cron);

        Date start = fmt.parse("2025-02-01 00:00");
        List<String> got = new ArrayList<>();

        //getNextValidTimeAfter ข้ามไปตามเงื่อนไข
        // output: 2025-03-31 09:00
        Date next = expr.getNextValidTimeAfter(start);

        assertEquals("2025-03-31 09:00", fmt.format(next));
    }

    @Test
    void last_day_of_month_leap_year_feb_2024_should_be_29() throws Exception {
        String cron = "0 0 9 L * ?";
        assertTrue(CronExpression.isValidExpression(cron));
        CronExpression expr = new CronExpression(cron);

        Date from = fmt.parse("2024-02-01 00:00");
        Date next = expr.getNextValidTimeAfter(from);

        Assertions.assertThat(fmt.format(next)).isEqualTo("2024-02-29 09:00");
    }
}
