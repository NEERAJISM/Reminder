package com.markone.reminder.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.markone.reminder.Common;
import com.markone.reminder.databinding.FragmentDoneDashboardBinding;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class DoneDashboardFragment extends Fragment {

    private FragmentDoneDashboardBinding fragmentDashboardBinding;

    private RecyclerView doneRecyclerView;
    private RecyclerView.LayoutManager doneLayoutManager;
    private RecyclerViewAdapter doneAdapter;
    private List<Reminder> doneReminders = new ArrayList<>();

    private RecyclerView earlierRecyclerView;
    private RecyclerView.LayoutManager earlierLayoutManager;
    private RecyclerViewAdapter earlierAdapter;
    private List<Reminder> earlierReminders = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doneLayoutManager = new LinearLayoutManager(getContext());
        doneAdapter = new RecyclerViewAdapter(getActivity(), doneReminders);

        earlierLayoutManager = new LinearLayoutManager(getContext());
        earlierAdapter = new RecyclerViewAdapter(getActivity(), earlierReminders);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (fragmentDashboardBinding == null) {
            fragmentDashboardBinding = FragmentDoneDashboardBinding.inflate(inflater, container, false);

            doneRecyclerView = fragmentDashboardBinding.rvReminders;
            doneRecyclerView.setLayoutManager(doneLayoutManager);
            doneRecyclerView.setAdapter(doneAdapter);

            earlierRecyclerView = fragmentDashboardBinding.rvEarlierReminders;
            earlierRecyclerView.setLayoutManager(earlierLayoutManager);
            earlierRecyclerView.setAdapter(earlierAdapter);
        }
        return fragmentDashboardBinding.getRoot();
    }

    public void updateReminders(List<Reminder> remindersDone) {
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

        doneAdapter.updateReminders(doneReminders);
        doneAdapter.notifyDataSetChanged();

        earlierAdapter.updateReminders(earlierReminders);
        earlierAdapter.notifyDataSetChanged();

        fragmentDashboardBinding.tvNoReminders.setVisibility((doneReminders.size() == 0 && earlierReminders.size() == 0) ? View.VISIBLE : View.INVISIBLE);
        fragmentDashboardBinding.tvToday.setVisibility((doneReminders.size() != 0) ? View.VISIBLE : View.GONE);
        fragmentDashboardBinding.tvEarlier.setVisibility((earlierReminders.size() != 0) ? View.VISIBLE : View.GONE);
    }
}