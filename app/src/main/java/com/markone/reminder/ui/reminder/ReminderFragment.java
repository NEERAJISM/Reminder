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

    private final CollectionReference reminderCollectionReference = FirebaseFirestore.getInstance()
            .collection(Common.REMINDER_DB)
            .document(Common.USER_ID)
            .collection(Common.REMINDER_COLLECTION);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        documentReference.set(reminder);
        (Objects.requireNonNull(getActivity())).onBackPressed();
    }
}
