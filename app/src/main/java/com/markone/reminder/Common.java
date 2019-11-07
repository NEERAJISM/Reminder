package com.markone.reminder;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class Common {

    public static final String REMINDER_DB = "Reminder DB";

    public static void generateSnackBar(View view, String message) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).show();
    }

    public enum Status {
        None, Low, Med, High, Done, Missed
    }

    public enum Frequency {
        None, Custom, Daily, Weekly, Monthly, Yearly
    }

}
