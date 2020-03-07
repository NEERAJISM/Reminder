package com.markone.reminder.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.markone.reminder.service.BootService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent service = new Intent(context, BootService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(service);
            } else {
                context.startService(service);
            }
        }
    }
}
