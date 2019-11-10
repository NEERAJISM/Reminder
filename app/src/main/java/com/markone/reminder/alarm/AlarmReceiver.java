package com.markone.reminder.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.markone.reminder.Common;
import com.markone.reminder.service.VibrateService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, VibrateService.class);
        service.setAction(intent.getAction());
        service.putExtra(Common.REMINDER_NAME, intent.getStringExtra(Common.REMINDER_NAME));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
    }
}
