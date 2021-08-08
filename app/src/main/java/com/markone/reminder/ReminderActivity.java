package com.markone.reminder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Source;
import com.markone.reminder.databinding.ActivityReminderBinding;
import com.markone.reminder.service.NotificationService;
import com.markone.reminder.ui.reminder.Reminder;

import static com.markone.reminder.service.NotificationService.ACTION_COMPLETE_SERVICE;
import static com.markone.reminder.service.NotificationService.ACTION_SNOOZE_SERVICE;
import static com.markone.reminder.service.NotificationService.ACTION_STOP_SERVICE;
import static com.markone.reminder.service.NotificationService.NOTIFICATION_ID;

public class ReminderActivity extends AppCompatActivity {

    private ActivityReminderBinding binding;
    private CollectionReference reminderCollectionReference;
    private String reminderId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reminderCollectionReference = Common.getUserReminderCollection(getSharedPreferences(Common.USER_FILE, Context.MODE_PRIVATE).getString(Common.USER_ID, "UserId"));
        reminderId = getIntent().getStringExtra(NOTIFICATION_ID);
        getReminder(reminderId);
        String reminderName = getIntent().getAction();

        binding = ActivityReminderBinding.inflate(getLayoutInflater());
        binding.tvTitle.setText(reminderName);
        setButtons();
        setContentView(binding.getRoot());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String reminderName = intent.getAction();
        binding.tvTitle.setText(reminderName);
        reminderId = intent.getStringExtra(NOTIFICATION_ID);
        getReminder(reminderId);
        setButtons();
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String reminderName = getIntent().getAction();
        binding.tvTitle.setText(reminderName);
        reminderId = getIntent().getStringExtra(NOTIFICATION_ID);
        getReminder(reminderId);
        setButtons();
    }

    private void setButtons() {
        final Intent service = new Intent(this, NotificationService.class);
        service.putExtra(NOTIFICATION_ID, reminderId);

        binding.btSnooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.viewToast(getApplicationContext(), "Reminder snoozed");
                service.setAction(ACTION_SNOOZE_SERVICE);
                startService(service);
                finish();
            }
        });

        binding.btMarkComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.viewToast(getApplicationContext(), "Reminder Completed");
                service.setAction(ACTION_STOP_SERVICE);
                startService(service);
                finish();
            }
        });

        binding.btMarkCompleteForever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.viewToast(getApplicationContext(), "Reminder Marked as Done");
                service.setAction(ACTION_COMPLETE_SERVICE);
                startService(service);
                finish();
            }
        });

    }

    private void getReminder(final String reminderId) {
        reminderCollectionReference.document(reminderId).get(Source.CACHE).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Reminder reminder = task.getResult().toObject(Reminder.class);
                    if (reminder != null) {
                        String details = reminder.getDetails();
                        if (Common.isBlank(details)) {
                            binding.tvDetails.setVisibility(View.GONE);
                        } else {
                            binding.tvDetails.setText(reminder.getDetails());
                            binding.tvDetails.setVisibility(View.VISIBLE);
                        }

                        if (reminder.getFrequency() != Common.Frequency.Once) {
                            binding.btMarkCompleteForever.setVisibility(View.VISIBLE);
                            binding.btMarkComplete.setText("Stop");
                        } else {
                            binding.btMarkComplete.setText("Mark as Done");
                            binding.btMarkCompleteForever.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }
}
