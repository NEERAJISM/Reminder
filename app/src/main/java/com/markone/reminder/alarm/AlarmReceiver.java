package com.markone.reminder.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.markone.reminder.Common;
import com.markone.reminder.MainActivity;
import com.markone.reminder.service.NotificationService;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.Calendar;

//Todo rename this
public class AlarmReceiver extends BroadcastReceiver {

    private final CollectionReference reminderCollectionReference = FirebaseFirestore.getInstance()
            .collection(Common.REMINDER_DB)
            .document(Common.USER_ID)
            .collection(Common.REMINDER_COLLECTION);

    @Override
    public void onReceive(final Context context, Intent intent) {
        String reminderName = intent.getStringExtra(Common.REMINDER_NAME);

        if (!Common.isBlank(reminderName)) {
            Intent service = new Intent(context, NotificationService.class);
            service.setAction(intent.getAction());
            service.putExtra(Common.REMINDER_NAME, intent.getStringExtra(Common.REMINDER_NAME));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(service);
            } else {
                context.startService(service);
            }
        }


        if (Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            reminderCollectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Calendar calendar = Calendar.getInstance();
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                        if (alarmManager == null)
                            return;

                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Reminder reminder = documentSnapshot.toObject(Reminder.class);

                            if (reminder.getStatus() == Common.Status.Done || reminder.getStatus() == Common.Status.Missed) {
                                continue;
                            }

                            calendar.set(Calendar.HOUR_OF_DAY, reminder.getStartDate_Hour());
                            calendar.set(Calendar.MINUTE, reminder.getStartDate_Minute());
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            calendar.set(Calendar.DAY_OF_MONTH, reminder.getStartDate_Day());
                            calendar.set(Calendar.MONTH, reminder.getStartDate_Month());
                            calendar.set(Calendar.YEAR, reminder.getStartDate_Year());

                            if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
                                Intent intent = new Intent(context, AlarmReceiver.class);
                                intent.setAction(reminder.getId());
                                intent.putExtra(Common.REMINDER_NAME, reminder.getName());
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            }
                        }
                        context.startActivity(new Intent(context, MainActivity.class));
                    }
                }
            });
        }
    }
}
