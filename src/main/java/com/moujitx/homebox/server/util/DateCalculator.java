package com.moujitx.homebox.server.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateCalculator {

    public record DateTriplet(LocalDate startDate, LocalDate endDate, Integer durationDays) {}

    public static DateTriplet calculate(LocalDate startDate, LocalDate endDate, Integer durationDays) {
        int providedCount = 0;
        if (startDate != null) providedCount++;
        if (endDate != null) providedCount++;
        if (durationDays != null) providedCount++;

        if (providedCount < 2) {
            throw new IllegalArgumentException("At least 2 of (startDate, endDate, durationDays) must be provided");
        }

        if (startDate != null && endDate != null) {
            int days = (int) ChronoUnit.DAYS.between(startDate, endDate);
            return new DateTriplet(startDate, endDate, days);
        } else if (startDate != null && durationDays != null) {
            LocalDate computed = startDate.plusDays(durationDays);
            return new DateTriplet(startDate, computed, durationDays);
        } else {
            LocalDate computed = endDate.minusDays(durationDays);
            return new DateTriplet(computed, endDate, durationDays);
        }
    }

    private DateCalculator() {}
}
