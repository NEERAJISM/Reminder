package com.markone.reminder;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Common {
    public static final String REMINDER_COLLECTION = "Reminders";

    public static final String REMINDER_DB = "Reminder DB";
    //Todo remove this with actual id
    public static final String USER_ID = "Neeraj User ID";
    private static final Calendar myCalendar = Calendar.getInstance();

    public static void generateSnackBar(View view, String message) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).show();
    }

    public static void viewToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String getFormattedTime(int hour, int min, int ampm) {
        String hourString = (hour < 10 ? "0" : "") + (hour > 12 ? hour - 12 : hour);
        String minString = (min < 10 ? "0" : "") + min;
        String ampmString = ampm == Calendar.AM ? "AM" : "PM";
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
        None, Custom, Daily, Weekly, Monthly, Yearly
    }

}
