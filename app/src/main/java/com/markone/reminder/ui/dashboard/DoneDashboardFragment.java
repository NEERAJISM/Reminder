package com.markone.reminder.ui.dashboard;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.markone.reminder.Common;
import com.markone.reminder.databinding.FragmentDoneDashboardBinding;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class DoneDashboardFragment extends Fragment {

    private FragmentDoneDashboardBinding fragmentDashboardBinding;
    private FloatingActionButton floatingActionButtonDelete;
    private AlertDialog.Builder alertDialog;

    private RecyclerView.LayoutManager doneLayoutManager;
    private RecyclerViewAdapter doneAdapter;
    private List<Reminder> doneReminders = new ArrayList<>();

    private RecyclerView.LayoutManager earlierLayoutManager;
    private RecyclerViewAdapter earlierAdapter;
    private List<Reminder> earlierReminders = new ArrayList<>();

    private CollectionReference reminderCollectionReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doneLayoutManager = new LinearLayoutManager(getContext());
        doneAdapter = new RecyclerViewAdapter(getActivity(), doneReminders);

        earlierLayoutManager = new LinearLayoutManager(getContext());
        earlierAdapter = new RecyclerViewAdapter(getActivity(), earlierReminders);

        createAlertDialog();
        reminderCollectionReference = Common.getUserReminderCollection(Objects.requireNonNull(getActivity()).getSharedPreferences(Common.USER_FILE, MODE_PRIVATE).getString(Common.USER_ID, "UserId"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (fragmentDashboardBinding == null) {
            fragmentDashboardBinding = FragmentDoneDashboardBinding.inflate(inflater, container, false);
            floatingActionButtonDelete = fragmentDashboardBinding.fabDelete;

            RecyclerView doneRecyclerView = fragmentDashboardBinding.rvReminders;
            doneRecyclerView.setLayoutManager(doneLayoutManager);
            doneRecyclerView.setAdapter(doneAdapter);

            RecyclerView earlierRecyclerView = fragmentDashboardBinding.rvEarlierReminders;
            earlierRecyclerView.setLayoutManager(earlierLayoutManager);
            earlierRecyclerView.setAdapter(earlierAdapter);

            setFloatingButtonAction();
        }
        return fragmentDashboardBinding.getRoot();
    }

    private void createAlertDialog() {
        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Delete All Completed Reminders ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        deleteAllCompletedReminders();
                        updateView();
                        Common.viewToast(getContext(), "Removed Successfully!!");
                    }
                })
                .setNegativeButton(android.R.string.no, null);
    }

    private void setFloatingButtonAction() {
        floatingActionButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (doneReminders.size() > 0 || earlierReminders.size() > 0) {
                    alertDialog.show();
                } else {
                    Common.viewToast(getContext(), "Nothing to Remove!!");
                }
            }
        });
    }

    private void deleteAllCompletedReminders() {
        for (Reminder reminder : doneReminders) {
            reminderCollectionReference.document(reminder.getId()).delete();
        }
        for (Reminder reminder : earlierReminders) {
            reminderCollectionReference.document(reminder.getId()).delete();
        }
        doneReminders.clear();
        earlierReminders.clear();
    }

    void updateReminders(List<Reminder> remindersDone) {
        doneReminders.clear();
        earlierReminders.clear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long today = calendar.getTimeInMillis();

        for (Reminder reminder : remindersDone) {
            Common.updateCalendarFromReminder(calendar, reminder);

            if (calendar.getTimeInMillis() < today) {
                earlierReminders.add(reminder);
            } else {
                doneReminders.add(reminder);
            }
        }

        if (doneReminders.size() > 1) {
            Collections.sort(doneReminders, Common.reminderComparator);
            Collections.reverse(doneReminders);
        }
        if (earlierReminders.size() > 1) {
            Collections.sort(earlierReminders, Common.reminderComparator);
            Collections.reverse(earlierReminders);
        }
        updateView();
    }

    private void updateView() {
        doneAdapter.updateReminders(doneReminders);
        doneAdapter.notifyDataSetChanged();
        earlierAdapter.updateReminders(earlierReminders);
        earlierAdapter.notifyDataSetChanged();

        fragmentDashboardBinding.tvNoReminders.setVisibility((doneReminders.size() == 0 && earlierReminders.size() == 0) ? View.VISIBLE : View.GONE);
        fragmentDashboardBinding.tvToday.setVisibility((doneReminders.size() != 0) ? View.VISIBLE : View.GONE);
        fragmentDashboardBinding.tvEarlier.setVisibility((earlierReminders.size() != 0) ? View.VISIBLE : View.GONE);
    }
}