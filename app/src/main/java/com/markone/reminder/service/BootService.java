package com.markone.reminder.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.markone.reminder.Common;
import com.markone.reminder.alarm.AlarmReceiver;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.Calendar;

public class BootService extends Service {
    private AlarmManager alarmManager;
    private CollectionReference reminderCollectionReference;

    @Override
    public void onCreate() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        reminderCollectionReference = FirebaseFirestore.getInstance()
                .collection(Common.REMINDER_DB)
                .document(getSharedPreferences(Common.USER_FILE, Context.MODE_PRIVATE).getString(Common.USER_ID, "UserId"))
                .collection(Common.REMINDER_COLLECTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Context context = this;

        reminderCollectionReference.get(Source.CACHE).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Calendar calendar = Calendar.getInstance();

                    if (alarmManager == null)
                        return;

                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        Reminder reminder = documentSnapshot.toObject(Reminder.class);

                        if (reminder.getStatus() == Common.Status.Done) {
                            continue;
                        }

                        //Todo Add logs like this
                        Log.v("Reminder", "Firestore reminder" + reminder.getName() + " " + reminder.getId());

                        calendar.set(Calendar.HOUR_OF_DAY, reminder.getStartDate_Hour());
                        calendar.set(Calendar.MINUTE, reminder.getStartDate_Minute());
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, reminder.getStartDate_Day());
                        calendar.set(Calendar.MONTH, reminder.getStartDate_Month());
                        calendar.set(Calendar.YEAR, reminder.getStartDate_Year());

                        Intent intent = new Intent(context, AlarmReceiver.class);
                        intent.setAction(reminder.getId());
                        intent.putExtra(Common.REMINDER_NAME, reminder.getName());
                        intent.putExtra(Common.REMINDER_FREQUENCY, reminder.getFrequency().toString());
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        alarmManager.set(AlarmManager.RTC_WAKEUP, Math.max(System.currentTimeMillis(), calendar.getTimeInMillis()), pendingIntent);
                    }
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
