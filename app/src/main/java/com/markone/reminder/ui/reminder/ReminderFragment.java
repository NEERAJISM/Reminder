package com.markone.reminder.ui.reminder;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.markone.reminder.Common;
import com.markone.reminder.R;
import com.markone.reminder.alarm.AlarmReceiver;
import com.markone.reminder.databinding.FragmentReminderBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * Created by Neeraj on 02-Nov-19
 */
public class ReminderFragment extends Fragment {
    private final Calendar myCalendar = Calendar.getInstance();
    private Reminder reminder;
    private FragmentReminderBinding binding;
    private List<String> frequency;
    private int hour, min, ampm, day, year, month;
    private AlarmManager alarmManager;

    private final CollectionReference reminderCollectionReference = FirebaseFirestore.getInstance()
            .collection(Common.REMINDER_DB)
            .document(Common.USER_ID)
            .collection(Common.REMINDER_COLLECTION);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmManager = (AlarmManager) Objects.requireNonNull(getContext()).getSystemService(Context.ALARM_SERVICE);
        frequency = new ArrayList<String>();
        for (Common.Frequency value : Common.Frequency.values()) {
            frequency.add(value.toString());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentReminderBinding.inflate(inflater, container, false);
            binding.spinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item, frequency));
            setCreateButton();
        }

        if (getArguments() != null) {
            reminder = (Reminder) getArguments().getSerializable("Reminder");
            updateUI();
        } else {
            reminder = new Reminder();
            day = myCalendar.get(Calendar.DAY_OF_MONTH);
            month = myCalendar.get(Calendar.MONTH);
            year = myCalendar.get(Calendar.YEAR);
            hour = myCalendar.get(Calendar.HOUR_OF_DAY);
            min = myCalendar.get(Calendar.MINUTE);
            ampm = myCalendar.get(Calendar.AM_PM);
        }

        setDatePicker();
        setTimePicker();
        return binding.getRoot();
    }

    private void updateUI() {
        binding.etName.setText(reminder.getName());
        binding.etDetails.setText(reminder.getDetails());
        binding.spinner.setSelection(reminder.getFrequency().ordinal());
        day = reminder.getStartDate_Day();
        month = reminder.getStartDate_Month();
        year = reminder.getStartDate_Year();
        hour = reminder.getStartDate_Hour();
        min = reminder.getStartDate_Minute();
        ampm = reminder.getStartDate_ampm();
    }

    private void setCreateButton() {
        binding.btCreateReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReminder();
            }
        });
    }

    private void setDatePicker() {
        updateDatePicker();

        final DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int yearSet, int monthSet, int daySet) {
                day = daySet;
                month = monthSet;
                year = yearSet;
                updateDatePicker();
            }
        };

        binding.tvDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), listener,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate((System.currentTimeMillis() - 1000));
                datePickerDialog.show();
            }
        });
    }

    private void updateDatePicker() {
        binding.tvDatePicker.setText(Common.getFormattedDate(day, month, year));
    }

    private void setTimePicker() {
        updateTimePicker();

        binding.tvTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                hour = hourOfDay;
                                min = minute;
                                ampm = hourOfDay >= 12 ? Calendar.PM : Calendar.AM;
                                updateTimePicker();
                            }
                        }, hour, min, false).show();
            }
        });
    }

    private void updateTimePicker() {
        binding.tvTimePicker.setText(Common.getFormattedTime(hour, min, ampm));
    }

    private void createReminder() {
        String id = reminder.getId();
        DocumentReference documentReference = id.isEmpty() ? reminderCollectionReference.document() : reminderCollectionReference.document(id);

        reminder.setId(documentReference.getId());
        reminder.setName(binding.etName.getText().toString());
        reminder.setDetails(binding.etDetails.getText().toString());
        reminder.setStartDate_Hour(hour);
        reminder.setStartDate_Minute(min);
        reminder.setStartDate_ampm(ampm);
        reminder.setStartDate_Day(day);
        reminder.setStartDate_Month(month);
        reminder.setStartDate_Year(year);
        reminder.setFrequency(Common.Frequency.getFrequency(binding.spinner.getSelectedItem().toString()));

        documentReference.set(reminder).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                } else {
                    //Todo failure
                }
            }
        });
        setAlarm();
        (Objects.requireNonNull(getActivity())).onBackPressed();
    }

    private void setAlarm() {

        // chk if already set or update
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        intent.setAction(reminder.getId());
        intent.putExtra(Common.REMINDER_NAME, reminder.getName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);

        if (reminder.getFrequency() == Common.Frequency.Once) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), getFrequencyInMillis(), pendingIntent);
        }
    }

    private long getFrequencyInMillis() {
        switch (reminder.getFrequency()) {
            case Every_1_Min:
                return 60 * 1000;
            case Every_5_Min:
                return 5 * 60 * 1000;
            case Every_10_Min:
                return 10 * 60 * 1000;
            case Every_30_Min:
                return AlarmManager.INTERVAL_HALF_HOUR;
            case Hourly:
                return AlarmManager.INTERVAL_HOUR;
            case Daily:
                return AlarmManager.INTERVAL_DAY;
            case Weekly:
                return 7 * AlarmManager.INTERVAL_DAY;
            //Todo set these
            case Weekend:
            case Monthly:
            case Yearly:
                return 0;
        }
        return 0;
    }
}