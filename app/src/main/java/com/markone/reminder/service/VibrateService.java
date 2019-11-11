package com.markone.reminder.service;

import android.app.Notification;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class VibrateService extends Service {
    private static final String ACTION_STOP_SERVICE = "StopService";
    private static final String NOTIFICATION_ID = "NotificationId";

    private Notification foregroundNotification;
    private Map<String, Notification> idNotificationMap = new HashMap<>();
    private Map<Notification, String> notificationIdMap = new HashMap<>();
    private LinkedBlockingQueue<Notification> backgroundNotifications = new LinkedBlockingQueue<>();

    private Vibrator vibrator;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        setNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int uniqueId = (int) System.currentTimeMillis();
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            String nId = intent.getStringExtra(NOTIFICATION_ID);

            if (nId.equals(notificationIdMap.get(foregroundNotification))) {
                if (!backgroundNotifications.isEmpty()) {
                    try {
                        foregroundNotification = backgroundNotifications.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    notificationManager.cancel(notificationIdMap.get(foregroundNotification), 0);
                    startForeground(uniqueId, foregroundNotification);
                } else {
                    foregroundNotification = null;
                }
            } else {
                notificationManager.cancel(nId, 0);
                backgroundNotifications.remove(idNotificationMap.get(nId));
            }

            notificationIdMap.remove(idNotificationMap.get(nId));
            idNotificationMap.remove(nId);

            // All empty
            if (idNotificationMap.isEmpty()) {
                vibrator.cancel();
                stopForeground(true);
                stopSelf();
            }
            return super.onStartCommand(intent, flags, startId);
        }

        String reminderId = intent.getAction();
        String reminderName = intent.getStringExtra(Common.REMINDER_NAME);

        // Open Main Activity
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pendingIntentActivity = PendingIntent.getActivity(this, uniqueId, open, PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel Notification & update Reminder
        Intent cancel = new Intent(this, VibrateService.class);
        cancel.setAction(ACTION_STOP_SERVICE);
        cancel.putExtra(NOTIFICATION_ID, reminderId);
        PendingIntent pendingIntentDone = PendingIntent.getService(this, uniqueId, cancel, PendingIntent.FLAG_CANCEL_CURRENT);

        //Send Notification
        Notification notification = new NotificationCompat.Builder(this, Common.REMINDER_CHANNEL)
                .setSmallIcon(R.drawable.ic_reminder)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_app_icon))
                .setContentTitle("Reminder")
                .setContentText(reminderName)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .addAction(R.drawable.ic_tick, "Mark Complete", pendingIntentDone)
                .addAction(R.drawable.ic_snooze, "Snooze", pendingIntentDone)
                .setContentIntent(pendingIntentActivity).build();

        idNotificationMap.put(reminderId, notification);
        notificationIdMap.put(notification, reminderId);

        try {
            if (foregroundNotification == null) {
                foregroundNotification = notification;
                startForeground(uniqueId, foregroundNotification);
                startVibration();
            } else {
                backgroundNotifications.put(notification);
                notificationManager.notify(reminderId, 0, notification);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                vibrator.vibrate(VibrationEffect.createWaveform(Common.VIBRATION_PATTERN, 1));
            } else {
                vibrator.vibrate(Common.VIBRATION_PATTERN, 1);
            }
        }
    }
}
