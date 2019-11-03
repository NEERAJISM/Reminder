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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.markone.reminder.Common;
import com.markone.reminder.MainActivity;
import com.markone.reminder.R;
import com.markone.reminder.databinding.FragmentDashboardBinding;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Neeraj on 02-Nov-19
 */
public class DashboardFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private FloatingActionButton floatingActionButton;

    private List<Reminder> reminderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getReminders();

        FragmentDashboardBinding fragmentDashboardBinding = FragmentDashboardBinding.inflate(inflater, container, false);
        recyclerView = fragmentDashboardBinding.rvReminders;
        floatingActionButton = fragmentDashboardBinding.fab;
        setFloatingButtonAction();

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecyclerViewAdapter(reminderList);
        recyclerView.setAdapter(mAdapter);

        return fragmentDashboardBinding.getRoot();
    }

    private void setFloatingButtonAction() {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.generateSnackBar(view, "Replace with your own action");
                ((MainActivity) Objects.requireNonNull(getActivity())).getNavController().navigate(R.id.nav_reminder);
            }
        });
    }

    private void getReminders() {
        Reminder r1 = new Reminder();
        r1.setName("Reminder 1");
        r1.setFrequency(Common.Frequency.Daily);
        r1.setStartDate_Day(02);
        r1.setStartDate_Month(12);
        r1.setStartDate_Year(1994);
        r1.setStartDate_Hour(9);
        r1.setStartDate_Minute(55);

        Reminder r2 = new Reminder();
        r2.setName("Reminder 2");
        r2.setFrequency(Common.Frequency.None);
        r2.setStartDate_Day(02);
        r2.setStartDate_Month(12);
        r2.setStartDate_Year(1994);
        r2.setStartDate_Hour(9);
        r2.setStartDate_Minute(55);

        reminderList.add(r1);
        reminderList.add(r2);
        reminderList.add(r2);
        reminderList.add(r2);
        reminderList.add(r2);
        reminderList.add(r2);
        reminderList.add(r2);
        reminderList.add(r2);
        reminderList.add(r2);
        reminderList.add(r2);
        reminderList.add(r2);
        reminderList.add(r2);
    }
}
