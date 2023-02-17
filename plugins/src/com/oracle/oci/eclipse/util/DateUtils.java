package com.oracle.oci.eclipse.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

import org.eclipse.swt.widgets.DateTime;

public class DateUtils {

    public static void apply(LocalDateTime date, DateTime dateWidget) {
        dateWidget.setDate(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
    }

    public static LocalDateTime convert(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    public static Instant convert(LocalDateTime dateTime) {
        long timeInSeconds = dateTime.toEpochSecond(ZoneOffset.UTC);
        return Instant.ofEpochSecond(timeInSeconds);
    }
}
