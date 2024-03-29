package com.markone.reminder.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Source;
import com.markone.reminder.Common;
import com.markone.reminder.R;
import com.markone.reminder.ReminderActivity;
import com.markone.reminder.alarm.AlarmReceiver;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static com.markone.reminder.Common.Frequency.Every_1_Min;
import static com.markone.reminder.Common.Frequency.getFrequency;

public class NotificationService extends Service {
    public static final String ACTION_SNOOZE_SERVICE = "SnoozeService";
    public static final String ACTION_STOP_SERVICE = "StopService";
    public static final String ACTION_COMPLETE_SERVICE = "CompleteService";
    public static final String NOTIFICATION_ID = "NotificationId";

    private Notification foregroundNotification;
    private Map<String, Notification> idNotificationMap = new HashMap<>();
    private Map<Notification, String> notificationIdMap = new HashMap<>();
    private LinkedBlockingQueue<Notification> backgroundNotifications = new LinkedBlockingQueue<>();

    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private NotificationManager notificationManager;
    private CollectionReference reminderCollectionReference;
    private AlarmManager alarmManager;

    @Override
    public void onCreate() {
        reminderCollectionReference = Common.getUserReminderCollection(getSharedPreferences(Common.USER_FILE, Context.MODE_PRIVATE).getString(Common.USER_ID, "UserId"));
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.default_ringtone);
        mediaPlayer.setLooping(true);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        setNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String reminderId = intent.getAction();
        int uniqueId = (int) System.currentTimeMillis();

        if (ACTION_STOP_SERVICE.equals(reminderId) || ACTION_SNOOZE_SERVICE.equals(reminderId) || ACTION_COMPLETE_SERVICE.equals(reminderId)) {
            String nId = intent.getStringExtra(NOTIFICATION_ID);
            updateReminder(nId, reminderId);
            handleNotification(nId, uniqueId);
            return super.onStartCommand(intent, flags, startId);
        }

        String reminderName = intent.getStringExtra(Common.REMINDER_NAME);
        boolean isRepeating = Common.Frequency.Once != Common.Frequency.getFrequency(intent.getStringExtra(Common.REMINDER_FREQUENCY));

        // Open Main Activity
        Intent open = new Intent(this, ReminderActivity.class);
        open.setAction(reminderName);
        open.putExtra(NOTIFICATION_ID, reminderId);
        PendingIntent pendingIntentActivity = PendingIntent.getActivity(this, uniqueId, open, PendingIntent.FLAG_CANCEL_CURRENT);

        // Snooze
        Intent snooze = new Intent(this, NotificationService.class);
        snooze.setAction(ACTION_SNOOZE_SERVICE);
        snooze.putExtra(NOTIFICATION_ID, reminderId);
        PendingIntent pendingIntentSnooze = PendingIntent.getService(this, uniqueId, snooze, PendingIntent.FLAG_CANCEL_CURRENT);

        // Done
        Intent done = new Intent(this, NotificationService.class);
        done.setAction(ACTION_STOP_SERVICE);
        done.putExtra(NOTIFICATION_ID, reminderId);
        PendingIntent pendingIntentDone = PendingIntent.getService(this, uniqueId, done, PendingIntent.FLAG_CANCEL_CURRENT);

        //Send Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Common.REMINDER_CHANNEL)
                .setSmallIcon(R.drawable.ic_reminder)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_app_icon))
                .setContentTitle("Reminder")
                .setContentText(reminderName)
                .setWhen(System.currentTimeMillis())
                .setFullScreenIntent(pendingIntentActivity, true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                //Todo make it configurable
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .addAction(0, "Snooze", pendingIntentSnooze)
                .addAction(0, isRepeating ? "Complete Once" : "Mark as Done", pendingIntentDone)
                .setContentIntent(pendingIntentActivity);

        // Complete
//        if (isRepeating) {
//            Intent complete = new Intent(this, NotificationService.class);
//            complete.setAction(ACTION_COMPLETE_SERVICE);
//            complete.putExtra(NOTIFICATION_ID, reminderId);
//            notificationBuilder.addAction(0, "Mark as Done", PendingIntent.getService(this, uniqueId, complete, PendingIntent.FLAG_CANCEL_CURRENT));
//        }

        Notification notification = notificationBuilder.build();
        idNotificationMap.put(reminderId, notification);
        notificationIdMap.put(notification, reminderId);

        try {
            if (foregroundNotification == null) {
                foregroundNotification = notification;
                startForeground(uniqueId, foregroundNotification);
                startVibration();
                mediaPlayer.start();
                scheduleStop();
            } else {
                backgroundNotifications.put(notification);
                notificationManager.notify(reminderId, 0, notification);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void scheduleStop() {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();                vibrator.cancel();
            }
        }, 20000);
    }

    private void handleNotification(String nId, int uniqueId) {
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
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                vibrator.cancel();
            }
            stopForeground(true);
            stopSelf();
        }
    }

    private void updateReminder(String nId, final String action) {
        final Common.Frequency snoozeFrequency = getFrequency(getSharedPreferences(Common.SETTING_FILE, Context.MODE_PRIVATE)
                .getString(Common.SNOOZE_SETTING, Every_1_Min.toString()));

        //Decide next snooze / reminder time whichever is less (chk if no next reminder time)
        reminderCollectionReference.document(nId).get(Source.CACHE).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Reminder reminder = task.getResult().toObject(Reminder.class);

                    if (reminder != null) {
                        if (ACTION_COMPLETE_SERVICE.equals(action)) {
                            reminder.setStatus(Common.Status.Done);
                            reminderCollectionReference.document(reminder.getId()).set(reminder);
                            return;
                        }

                        boolean isRepeating = (Common.Frequency.Once != reminder.getFrequency());
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, reminder.getStartDate_Hour());
                        calendar.set(Calendar.MINUTE, reminder.getStartDate_Minute());
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, reminder.getStartDate_Day());
                        calendar.set(Calendar.MONTH, reminder.getStartDate_Month());
                        calendar.set(Calendar.YEAR, reminder.getStartDate_Year());

                        // Set Next start date
                        if (isRepeating && calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                            while (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                                Common.updateCalendar(calendar, reminder.getFrequency());
                            }
                            reminder.setStartDate_Hour(calendar.get(Calendar.HOUR_OF_DAY));
                            reminder.setStartDate_Minute(calendar.get(Calendar.MINUTE));
                            reminder.setStartDate_Day(calendar.get(Calendar.DAY_OF_MONTH));
                            reminder.setStartDate_Month(calendar.get(Calendar.MONTH));
                            reminder.setStartDate_Year(calendar.get(Calendar.YEAR));
                        }

                        boolean isSnooze = ACTION_SNOOZE_SERVICE.equals(action);
                        Calendar snoozeCalendar = Calendar.getInstance();
                        if (isSnooze) {
                            Common.updateCalendar(snoozeCalendar, snoozeFrequency);
                            snoozeCalendar.set(Calendar.SECOND, 0);
                            snoozeCalendar.set(Calendar.MILLISECOND, 0);
                        }

                        if (isSnooze && (!isRepeating || snoozeCalendar.getTimeInMillis() < calendar.getTimeInMillis())) {
                            setAlarm(snoozeCalendar, reminder.getId(), reminder.getName(), reminder.getFrequency());
                        } else if (isRepeating) {
                            setAlarm(calendar, reminder.getId(), reminder.getName(), reminder.getFrequency());
                        }

                        // Mark as Done
                        if (!isSnooze && !isRepeating) {
                            reminder.setStatus(Common.Status.Done);
                        }
                        reminderCollectionReference.document(reminder.getId()).set(reminder);
                    }
                }
            }
        });
    }

    private void setAlarm(Calendar calendar, String reminderId, String reminderName, Common.Frequency frequency) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(reminderId);
        intent.putExtra(Common.REMINDER_NAME, reminderName);
        intent.putExtra(Common.REMINDER_FREQUENCY, frequency.toString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
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
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            channel.enableVibration(true);
            channel.setSound(null, null);
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
