package com.markone.reminder.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.markone.reminder.Common;
import com.markone.reminder.MainActivity;
import com.markone.reminder.R;

public class VibrateService extends Service {
    private static final String ACTION_STOP_SERVICE = "StopService";
    private static final String NOTIFICATION_ID = "NotificationId";

    private Vibrator vibrator;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        setNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            notificationManager.cancel(intent.getStringExtra(NOTIFICATION_ID), 0);
            vibrator.cancel();
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        String reminderId = intent.getAction();
        String reminderName = intent.getStringExtra(Common.REMINDER_NAME);

        // Open Main Activity
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pendingIntentActivity = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel Notification & update Reminder
        Intent cancel = new Intent(this, VibrateService.class);
        cancel.setAction(ACTION_STOP_SERVICE);
        cancel.putExtra("NotificationId", reminderId);
        PendingIntent pendingIntentDone = PendingIntent.getService(this, 0, cancel, PendingIntent.FLAG_CANCEL_CURRENT);

        //Send Notification
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, Common.REMINDER_CHANNEL);
        notification.setSmallIcon(R.drawable.ic_reminder)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_app_icon))
                .setContentTitle("Reminder")
                .setContentInfo(reminderName)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .addAction(R.drawable.ic_tick, "Mark Complete", pendingIntentDone)
                .addAction(R.drawable.ic_snooze, "Snooze", pendingIntentDone)
                .setContentIntent(pendingIntentActivity);

        notificationManager.notify(reminderId, 0, notification.build());
        startVibration();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Common.REMINDER_CHANNEL, Common.REMINDER_CHANNEL, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel to generate reminders for reminder app");
            //Todo make it configurable
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            channel.enableVibration(true);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startVibration() {
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(Common.VIBRATION_PATTERN, 0));
            } else {
                vibrator.vibrate(Common.VIBRATION_PATTERN, -1);
            }
        }
    }
}
