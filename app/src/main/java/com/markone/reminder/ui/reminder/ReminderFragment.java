package com.markone.reminder.ui.reminder;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.markone.reminder.Common;
import com.markone.reminder.R;
import com.markone.reminder.databinding.FragmentReminderBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by Neeraj on 02-Nov-19
 */
public class ReminderFragment extends Fragment {
    private final Calendar myCalendar = Calendar.getInstance();
    CollectionReference reminderCollectionReference = FirebaseFirestore.getInstance()
            .collection(Common.REMINDER_DB)
            .document("Neeraj User ID")
            .collection("Reminders");
    private FragmentReminderBinding binding;
    private int hour, min, ampm, day, year, month;

    private List<String> frequency = new ArrayList<String>() {{
        add("Once");
        add("1 Minute");
        add("5 Minute");
        add("10 Minute");
        add("30 Minute");
        add("1 Hour");
        add("Every Day");
        add("Every Week");
        add("Every Weekend");
        add("Every Month");
        add("Every Year");
    }};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReminderBinding.inflate(inflater, container, false);
        binding.spinner.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.spinner_item, frequency));

        setDatePicker();
        setTimePicker();

        binding.btCreateReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReminder();
                (Objects.requireNonNull(getActivity())).onBackPressed();
            }
        });

        return binding.getRoot();
    }

    private void setDatePicker() {
        updateDatePicker();
        final DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDatePicker();
            }
        };

        binding.tvDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), listener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate((System.currentTimeMillis() - 1000));
                datePickerDialog.show();
            }
        });
    }

    private void updateDatePicker() {
        day = myCalendar.get(Calendar.DAY_OF_MONTH);
        month = myCalendar.get(Calendar.MONTH);
        year = myCalendar.get(Calendar.YEAR);
        String myFormat = "dd MMM yyyy";
        SimpleDateFormat format = new SimpleDateFormat(myFormat, Locale.getDefault());
        format.setCalendar(myCalendar);
        binding.tvDatePicker.setText(format.format(myCalendar.getTime()));
    }

    private void setTimePicker() {
        hour = myCalendar.get(Calendar.HOUR);
        min = myCalendar.get(Calendar.MINUTE);
        ampm = myCalendar.get(Calendar.AM_PM);

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
        String hourString = (hour < 10 ? "0" : "") + (hour > 12 ? hour - 12 : hour);
        String minString = (min < 10 ? "0" : "") + min;
        String ampmString = ampm == Calendar.AM ? "AM" : "PM";
        binding.tvTimePicker.setText(hourString + " : " + minString + " " + ampmString);
    }


    private void createReminder() {
        DocumentReference documentReference = reminderCollectionReference.document();

        Reminder reminder = new Reminder();
        reminder.setId(documentReference.getId());
        reminder.setName(binding.etName.getText().toString());
        reminder.setDetails(binding.etDetails.getText().toString());
        reminder.setStartDate_Hour(hour);
        reminder.setStartDate_Minute(min);
        reminder.setStartDate_Day(day);
        reminder.setStartDate_Month(month);
        reminder.setStartDate_Year(year);

        documentReference.set(reminder);
    }
}
