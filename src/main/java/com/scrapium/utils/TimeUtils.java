package com.scrapium.utils;

import java.sql.Timestamp;
import java.util.Calendar;

public class TimeUtils {

    public static String timeToString(int seconds) {
        if (seconds < 60) {
            return "in " + seconds + " seconds";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            return "in " + minutes + " minute" + (minutes == 1 ? "" : "s");
        } else if (seconds < 86400) {
            int hours = seconds / 3600;
            return "in " + hours + " hour" + (hours == 1 ? "" : "s");
        } else {
            int days = seconds / 86400;
            return "in " + days + " day" + (days == 1 ? "" : "s");
        }
    }

    public static Timestamp nowPlusMinutes(int minutes){
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

        // Create a Calendar instance and set the time to the current timestamp
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTimestamp);

        // Add 15 minutes to the calendar
        calendar.add(Calendar.MINUTE, minutes);

        // Get the new timestamp with the updated time
        Timestamp newTimeStamp = new Timestamp(calendar.getTimeInMillis());

        return newTimeStamp;
    }


}