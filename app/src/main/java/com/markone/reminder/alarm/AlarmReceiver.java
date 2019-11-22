package com.markone.reminder.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.markone.reminder.Common;
import com.markone.reminder.service.NotificationService;

//Todo rename this
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        String reminderName = intent.getStringExtra(Common.REMINDER_NAME);
        if (!Common.isBlank(reminderName)) {
            Intent service = new Intent(context, NotificationService.class);
            service.setAction(intent.getAction());
            service.putExtra(Common.REMINDER_NAME, intent.getStringExtra(Common.REMINDER_NAME));
            service.putExtra(Common.REMINDER_FREQUENCY, intent.getStringExtra(Common.REMINDER_FREQUENCY));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(service);
            } else {
                context.startService(service);
            }
        }
    }
}
