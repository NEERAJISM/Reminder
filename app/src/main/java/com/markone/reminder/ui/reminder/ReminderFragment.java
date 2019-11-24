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
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

/**
 * Created by Neeraj on 02-Nov-19
 */
public class ReminderFragment extends Fragment {
    private final Calendar myCalendar = Calendar.getInstance();
    private Reminder reminder;
    private FragmentReminderBinding binding;
    private List<String> frequency;
    private int hour, min, day, year, month;
    private AlarmManager alarmManager;

    private  CollectionReference reminderCollectionReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        frequency = new ArrayList<String>();
        for (Common.Frequency value : Common.Frequency.values()) {
            frequency.add(value.toString());
        }
        reminderCollectionReference = FirebaseFirestore.getInstance()
                .collection(Common.REMINDER_DB)
                .document(getActivity().getSharedPreferences(Common.USER_FILE, Context.MODE_PRIVATE).getString(Common.USER_ID, "UserId"))
                .collection(Common.REMINDER_COLLECTION);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentReminderBinding.inflate(inflater, container, false);
            binding.spinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item, frequency));
            setCreateButton();
            setRadioButtons();
        }

        if (getArguments() != null) {
            binding.btCreateReminder.setText("Update Reminder");
            binding.btDeleteReminder.setVisibility(View.VISIBLE);
            reminder = (Reminder) getArguments().getSerializable("Reminder");
            updateUI();
            setDeleteButton();
        } else {
            binding.btCreateReminder.setText("Set Reminder");
            binding.btDeleteReminder.setVisibility(View.INVISIBLE);
            reminder = new Reminder();
            day = myCalendar.get(Calendar.DAY_OF_MONTH);
            month = myCalendar.get(Calendar.MONTH);
            year = myCalendar.get(Calendar.YEAR);
            hour = myCalendar.get(Calendar.HOUR_OF_DAY);
            min = myCalendar.get(Calendar.MINUTE);
        }

        setDatePicker();
        setTimePicker();
        updatePriority();
        //Todo update this - error while second time
//        requestKeyboard();
        return binding.getRoot();
    }

    private void requestKeyboard() {
        binding.etName.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void updatePriority() {
        if (reminder.getStatus() == Common.Status.High) {
            binding.rbPriorityHigh.setChecked(true);
        } else if (reminder.getStatus() == Common.Status.Med) {
            binding.rbPriorityMed.setChecked(true);
        } else {
            binding.rbPriorityLow.setChecked(true);
        }
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
    }

    private void setCreateButton() {
        binding.btCreateReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReminder();
            }
        });
    }

    private void setDeleteButton() {
        binding.btDeleteReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteReminder();
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
                                updateTimePicker();
                            }
                        }, hour, min, false).show();
            }
        });
    }

    private void updateTimePicker() {
        binding.tvTimePicker.setText(Common.getFormattedTime(hour, min));
    }

    private boolean isValidReminder(Calendar calendar) {
        if ("".equals(binding.etName.getText().toString().trim())) {
            Common.viewToast(getContext(), "Invalid Reminder Name");
            return false;
        }

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Common.viewToast(getContext(), "Set reminder time in future");
            return false;
        }
        return true;
    }

    private void createReminder() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);

        if (isValidReminder(calendar)) {
            String id = reminder.getId();
            DocumentReference documentReference = id.isEmpty() ? reminderCollectionReference.document() : reminderCollectionReference.document(id);

            reminder.setId(documentReference.getId());
            reminder.setName(binding.etName.getText().toString());
            reminder.setDetails(binding.etDetails.getText().toString());
            reminder.setStartDate_Hour(hour);
            reminder.setStartDate_Minute(min);
            reminder.setStartDate_Day(day);
            reminder.setStartDate_Month(month);
            reminder.setStartDate_Year(year);
            reminder.setFrequency(Common.Frequency.getFrequency(binding.spinner.getSelectedItem().toString()));
            reminder.setStatus(getPriority());

            documentReference.set(reminder);
            setAlarm(calendar);
            getActivity().onBackPressed();
        }
    }

    private Common.Status getPriority() {
        if (binding.rbPriorityMed.isChecked()) {
            return Common.Status.Med;
        }
        if (binding.rbPriorityHigh.isChecked()) {
            return Common.Status.High;
        }
        return Common.Status.Low;
    }

    private void setAlarm(Calendar calendar) {
        PendingIntent pendingIntent = getPendingIntent();
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void deleteReminder() {
        //Todo Delete notification for already running

        //Delete alarm
        PendingIntent pendingIntent = getPendingIntent();
        alarmManager.cancel(pendingIntent);

        //Delete entry
        reminderCollectionReference.document(reminder.getId()).delete();

        Common.viewToast(getContext(), "Reminder Deleted successfully!!");
        getActivity().onBackPressed();
    }

    private PendingIntent getPendingIntent() {
        // chk if already set or update
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        intent.setAction(reminder.getId());
        intent.putExtra(Common.REMINDER_NAME, reminder.getName());
        intent.putExtra(Common.REMINDER_FREQUENCY, reminder.getFrequency().toString());
        return PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void setRadioButtons() {

        binding.rbPriorityLow.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.rbPriorityMed.setChecked(false);
                    binding.rbPriorityHigh.setChecked(false);
                }
            }
        });

        binding.rbPriorityMed.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.rbPriorityLow.setChecked(false);
                    binding.rbPriorityHigh.setChecked(false);
                }
            }
        });

        binding.rbPriorityHigh.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.rbPriorityLow.setChecked(false);
                    binding.rbPriorityMed.setChecked(false);
                }
            }
        });
    }
}