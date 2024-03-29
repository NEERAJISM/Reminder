package com.markone.reminder;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.markone.reminder.ui.reminder.Reminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Common {
    public static final String REMINDER_COLLECTION = "Reminders";
    public static final String REMINDER_CHANNEL = "ReminderChannel";
    public static final long[] VIBRATION_PATTERN = {0L, 2000L, 500L};
    public static final int MAX_REMINDERS = 50;
    public static final int MAX_REMINDERS_PRO = 100;

    public static final String REMINDER_DB = "Reminder DB";
    public static final String REMINDER_NAME = "ReminderName";
    public static final String REMINDER_FREQUENCY = "ReminderFrequency";
    private static final Calendar myCalendar = Calendar.getInstance();

    // Intent Actions
    public static final String ACTION_NOT_STARTUP = "ActionNotStartup";

    // Shared preferences
    public static final String USER_FILE = "User";
    public static final String USER_NAME = "UserName";
    public static final String USER_MAIL = "UserMail";
    public static final String USER_ID = "UserId";
    public static final String USER_URI = "UserUri";

    public static final String SETTING_FILE = "Settings";
    public static final String SNOOZE_SETTING = "snoozeSetting";
    public static final String IS_FIRST_STARTUP = "isFirstStartup";
    public static final String SIGNOUT = "signout";
    public static final String CURRENT_REMINDER_SIZE = "currentReminderSize";

    public static CollectionReference getUserReminderCollection(String userId) {
        return FirebaseFirestore.getInstance()
                .collection(Common.REMINDER_DB)
                .document(userId)
                .collection(Common.REMINDER_COLLECTION);
    }

    public static final Comparator<Reminder> reminderComparator = new Comparator<Reminder>() {
        @Override
        public int compare(Reminder o1, Reminder o2) {

            if (o1.getStartDate_Year() < o2.getStartDate_Year()) {
                return -1;
            } else if (o1.getStartDate_Year() > o2.getStartDate_Year()) {
                return 1;
            }

            if (o1.getStartDate_Month() < o2.getStartDate_Month()) {
                return -1;
            } else if (o1.getStartDate_Month() > o2.getStartDate_Month()) {
                return 1;
            }

            if (o1.getStartDate_Day() < o2.getStartDate_Day()) {
                return -1;
            } else if (o1.getStartDate_Day() > o2.getStartDate_Day()) {
                return 1;
            }

            if (o1.getStartDate_Hour() < o2.getStartDate_Hour()) {
                return -1;
            } else if (o1.getStartDate_Hour() > o2.getStartDate_Hour()) {
                return 1;
            }

            if (o1.getStartDate_Minute() < o2.getStartDate_Minute()) {
                return -1;
            } else if (o1.getStartDate_Minute() > o2.getStartDate_Minute()) {
                return 1;
            }

            return 1;
        }
    };

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
        return hourString + " : " + minString + " " + ampmString;
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
        Low, Med, High, Done
    }

    public static void updateCalendar(Calendar calendar, Frequency frequency) {
        int dayOfWeek;
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
            case Two_Hour:
                calendar.add(Calendar.HOUR_OF_DAY, 2);
                break;
            case Three_Hour:
                calendar.add(Calendar.HOUR_OF_DAY, 3);
                break;
            case Twelve_Hour:
                calendar.add(Calendar.HOUR_OF_DAY, 12);
                break;
            case Daily:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case WorkingWeek:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                while (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                }
                break;
            case AlternateDay:
                calendar.add(Calendar.DAY_OF_MONTH, 2);
                break;
            case Weekends:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                while (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                }
                break;
            case Weekly:
                calendar.add(Calendar.WEEK_OF_MONTH, 1);
                break;
            case BiWeekly:
                calendar.add(Calendar.WEEK_OF_MONTH, 2);
                break;
            case Monthly:
                calendar.add(Calendar.MONTH, 1);
                break;
            case Quarterly:
                calendar.add(Calendar.MONTH, 3);
                break;
            case Yearly:
                calendar.add(Calendar.YEAR, 1);
                break;
        }
    }

    public enum Frequency {
        Once("Once"),
        Every_1_Min("1 Min"),
        Every_5_Min("5 Min"),
        Every_10_Min("10 Min"),
        Every_30_Min("30 Min"),
        Hourly("Hourly"),
        Two_Hour("Every 2 Hour"),
        Three_Hour("Every 3 Hour"),
        Twelve_Hour("12 Hour"),
        Daily("Daily"),
        WorkingWeek("Mon - Fri"),
        AlternateDay("Alternate Day"),
        Weekends("Weekends"),
        Weekly("Weekly"),
        BiWeekly("Bi-Weekly"),
        Monthly("Monthly"),
        Quarterly("Quarterly"),
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

    public static boolean isBlank(String s) {
        return (s == null || "".equals(s.trim()));
    }

    public static void updateCalendarFromReminder(Calendar calendar, Reminder reminder) {
        calendar.set(Calendar.HOUR_OF_DAY, reminder.getStartDate_Hour());
        calendar.set(Calendar.MINUTE, reminder.getStartDate_Minute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, reminder.getStartDate_Day());
        calendar.set(Calendar.MONTH, reminder.getStartDate_Month());
        calendar.set(Calendar.YEAR, reminder.getStartDate_Year());
    }

    public static GoogleSignInClient getGoogleSignInClient(Activity activity, String clientId) {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        return GoogleSignIn.getClient(activity, gso);
    }
}