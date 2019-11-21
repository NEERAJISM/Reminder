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
import android.os.Build;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.markone.reminder.Common;
import com.markone.reminder.MainActivity;
import com.markone.reminder.R;
import com.markone.reminder.alarm.AlarmReceiver;
import com.markone.reminder.ui.reminder.Reminder;
import com.markone.reminder.ui.settings.SettingsFragment;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static com.markone.reminder.Common.Frequency.Every_1_Min;
import static com.markone.reminder.Common.Frequency.getFrequency;

public class NotificationService extends Service {
    private static final String ACTION_STOP_SERVICE = "StopService";
    private static final String ACTION_SNOOZE_SERVICE = "SnoozeService";
    private static final String NOTIFICATION_ID = "NotificationId";

    private Notification foregroundNotification;
    private Map<String, Notification> idNotificationMap = new HashMap<>();
    private Map<Notification, String> notificationIdMap = new HashMap<>();
    private LinkedBlockingQueue<Notification> backgroundNotifications = new LinkedBlockingQueue<>();

    private Vibrator vibrator;
    private NotificationManager notificationManager;
    private CollectionReference reminderCollectionReference;
    private AlarmManager alarmManager;

    @Override
    public void onCreate() {
        reminderCollectionReference = FirebaseFirestore.getInstance()
                .collection(Common.REMINDER_DB)
                .document(getSharedPreferences(Common.USER_FILE, Context.MODE_PRIVATE).getString(Common.USER_ID, "UserId"))
                .collection(Common.REMINDER_COLLECTION);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        setNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String reminderId = intent.getAction();
        int uniqueId = (int) System.currentTimeMillis();

        if (ACTION_STOP_SERVICE.equals(reminderId) || ACTION_SNOOZE_SERVICE.equals(reminderId)) {
            String nId = intent.getStringExtra(NOTIFICATION_ID);
            updateReminder(nId, ACTION_SNOOZE_SERVICE.equals(reminderId));
            handleNotification(nId, uniqueId);
            return super.onStartCommand(intent, flags, startId);
        }

        String reminderName = intent.getStringExtra(Common.REMINDER_NAME);

        // Open Main Activity
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pendingIntentActivity = PendingIntent.getActivity(this, uniqueId, open, PendingIntent.FLAG_CANCEL_CURRENT);

        // Done
        Intent done = new Intent(this, NotificationService.class);
        done.setAction(ACTION_STOP_SERVICE);
        done.putExtra(NOTIFICATION_ID, reminderId);
        PendingIntent pendingIntentDone = PendingIntent.getService(this, uniqueId, done, PendingIntent.FLAG_CANCEL_CURRENT);

        // Snooze
        Intent snooze = new Intent(this, NotificationService.class);
        snooze.setAction(ACTION_SNOOZE_SERVICE);
        snooze.putExtra(NOTIFICATION_ID, reminderId);
        PendingIntent pendingIntentSnooze = PendingIntent.getService(this, uniqueId, snooze, PendingIntent.FLAG_CANCEL_CURRENT);

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
                //Todo icon not visible
                .addAction(R.drawable.ic_tick, "Mark Complete", pendingIntentDone)
                .addAction(R.drawable.ic_snooze, "Snooze", pendingIntentSnooze)
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
            vibrator.cancel();
            stopForeground(true);
            stopSelf();
        }
    }

    private void updateReminder(String nId, final boolean isSnooze) {
        //ToDo get snooze time
        final Common.Frequency snoozeFrequency = getFrequency(getSharedPreferences(SettingsFragment.SETTING_FILE, Context.MODE_PRIVATE)
                .getString(SettingsFragment.SNOOZE_SETTING, Every_1_Min.toString()));

        //Decide next snooze / reminder time whichever is less (chk if no next reminder time)
        reminderCollectionReference.document(nId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Reminder reminder = task.getResult().toObject(Reminder.class);

                    if (reminder != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, reminder.getStartDate_Hour());
                        calendar.set(Calendar.MINUTE, reminder.getStartDate_Minute());
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, reminder.getStartDate_Day());
                        calendar.set(Calendar.MONTH, reminder.getStartDate_Month());
                        calendar.set(Calendar.YEAR, reminder.getStartDate_Year());

                        // Set Next start date
                        if (reminder.getSnoozeDate_Year() == 0 && Common.Frequency.Once != reminder.getFrequency()) {
                            Common.updateCalendar(calendar, reminder.getFrequency());
                            reminder.setStartDate_Hour(calendar.get(Calendar.HOUR_OF_DAY));
                            reminder.setStartDate_Minute(calendar.get(Calendar.MINUTE));
                            reminder.setStartDate_Day(calendar.get(Calendar.DAY_OF_MONTH));
                            reminder.setStartDate_Month(calendar.get(Calendar.MONTH));
                            reminder.setStartDate_Year(calendar.get(Calendar.YEAR));
                        }

                        Calendar snoozeCalendar = (Calendar) calendar.clone();
                        if (isSnooze) {
                            if (reminder.getSnoozeDate_Year() != 0) {
                                snoozeCalendar.set(Calendar.HOUR_OF_DAY, reminder.getSnoozeDate_Hour());
                                snoozeCalendar.set(Calendar.MINUTE, reminder.getSnoozeDate_Minute());
                                snoozeCalendar.set(Calendar.DAY_OF_MONTH, reminder.getSnoozeDate_Day());
                                snoozeCalendar.set(Calendar.MONTH, reminder.getSnoozeDate_Month());
                                snoozeCalendar.set(Calendar.YEAR, reminder.getSnoozeDate_Year());
                            }
                            Common.updateCalendar(snoozeCalendar, snoozeFrequency);
                        }

                        if (isSnooze && (Common.Frequency.Once == reminder.getFrequency() || snoozeCalendar.getTimeInMillis() < calendar.getTimeInMillis())) {
                            setAlarm(snoozeCalendar, reminder.getId(), reminder.getName());

                            reminder.setSnoozeDate_Hour(snoozeCalendar.get(Calendar.HOUR_OF_DAY));
                            reminder.setSnoozeDate_Minute(snoozeCalendar.get(Calendar.MINUTE));
                            reminder.setSnoozeDate_Day(snoozeCalendar.get(Calendar.DAY_OF_MONTH));
                            reminder.setSnoozeDate_Month(snoozeCalendar.get(Calendar.MONTH));
                            reminder.setSnoozeDate_Year(snoozeCalendar.get(Calendar.YEAR));
                        } else if (Common.Frequency.Once != reminder.getFrequency()) {
                            reminder.setSnoozeDate_Year(0);
                            setAlarm(calendar, reminder.getId(), reminder.getName());
                        }

                        // Mark as Done
                        if (!isSnooze && Common.Frequency.Once == reminder.getFrequency()) {
                            reminder.setStatus(Common.Status.Done);
                        }

                        reminderCollectionReference.document(reminder.getId()).set(reminder);
                    }
                }
            }
        });
    }

    private void setAlarm(Calendar calendar, String reminderId, String reminderName) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(reminderId);
        intent.putExtra(Common.REMINDER_NAME, reminderName);
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
