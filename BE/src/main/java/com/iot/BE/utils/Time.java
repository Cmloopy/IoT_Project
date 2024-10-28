package com.iot.BE.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Time {
    // shared class

    public static String getTimeLocalConvert() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentDateTime.format(formatter);
    }
    public static Date getTimeLocal() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Date date = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return date;
    }
}
