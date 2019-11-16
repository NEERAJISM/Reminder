package com.markone.reminder;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Common {
    public static final String REMINDER_COLLECTION = "Reminders";
    public static final String REMINDER_CHANNEL = "ReminderChannel";
    public static final long[] VIBRATION_PATTERN = {0L, 2000L, 500L};

    public static final String REMINDER_DB = "Reminder DB";
    //Todo remove this with actual id
    public static final String USER_ID = "Neeraj User ID";
    public static final String REMINDER_NAME = "ReminderName";
    private static final Calendar myCalendar = Calendar.getInstance();

    public static Map<String, Frequency> frequencyMap = new HashMap<>();

    public static void generateSnackBar(View view, String message) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).show();
    }

    public static void viewToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String getFormattedTime(int hour, int min) {
        String ampmString = hour < 12 ? "AM" : "PM";
        hour = (hour > 12 ? hour - 12 : hour);
        String hourString = (hour == 0) ? "12" : ((hour < 10 ? "0" : "") + hour);
        String minString = (min < 10 ? "0" : "") + min;
        return new StringBuilder(hourString).append(" : ").append(minString).append(" ").append(ampmString).toString();
    }

    public static String getFormattedDate(int day, int month, int year) {
        myCalendar.set(Calendar.DAY_OF_MONTH, day);
        myCalendar.set(Calendar.MONTH, month);
        myCalendar.set(Calendar.YEAR, year);
        String myFormat = "dd MMM yyyy";
        SimpleDateFormat format = new SimpleDateFormat(myFormat, Locale.getDefault());
        format.setCalendar(myCalendar);
        return format.format(myCalendar.getTime());
    }

    public enum Status {
        None, Low, Med, High, Done, Missed
    }

    public enum Frequency {
        Once("Once"),
        Every_1_Min("1 Min"),
        Every_5_Min("5 Min"),
        Every_10_Min("10 Min"),
        Every_30_Min("30 Min"),
        Hourly("Hourly"),
        Daily("Daily"),
        Weekly("Weekly"),
        Monthly("Monthly"),
        Yearly("Yearly");

        private static final Map<String, Frequency> ENUM_MAP;

        static {
            Map<String, Frequency> map = new ConcurrentHashMap<>();
            for (Frequency instance : Frequency.values()) {
                map.put(instance.toString(), instance);
            }
            ENUM_MAP = Collections.unmodifiableMap(map);
        }

        private String frequency;


        Frequency(String s) {
            frequency = s;
        }

        public static Frequency getFrequency(String name) {
            return ENUM_MAP.get(name);
        }

        @NonNull
        @Override
        public String toString() {
            return frequency;
        }
    }

    public static void updateCalendar(Calendar calendar, Frequency frequency) {
        switch (frequency) {
            case Every_1_Min:
                calendar.add(Calendar.MINUTE, 1);
                break;
            case Every_5_Min:
                calendar.add(Calendar.MINUTE, 5);
                break;
            case Every_10_Min:
                calendar.add(Calendar.MINUTE, 10);
                break;
            case Every_30_Min:
                calendar.add(Calendar.MINUTE, 30);
                break;
            case Hourly:
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                break;
            case Daily:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case Weekly:
                calendar.add(Calendar.WEEK_OF_MONTH, 1);
                break;
            case Monthly:
                calendar.add(Calendar.MONTH, 1);
                break;
            case Yearly:
                calendar.add(Calendar.YEAR, 1);
                break;
        }
    }

    public static boolean isBlank(String s) {
        return (s == null || "".equals(s.trim()));
    }

}