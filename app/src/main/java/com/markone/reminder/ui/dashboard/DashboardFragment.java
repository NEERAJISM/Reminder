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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.markone.reminder.Common;
import com.markone.reminder.MainActivity;
import com.markone.reminder.R;
import com.markone.reminder.databinding.FragmentDashboardBinding;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by Neeraj on 02-Nov-19
 */
public class DashboardFragment extends Fragment {
    private final CollectionReference reminderCollectionReference = FirebaseFirestore.getInstance()
            .collection(Common.REMINDER_DB)
            .document(Common.USER_ID)
            .collection(Common.REMINDER_COLLECTION);

    private FragmentDashboardBinding fragmentDashboardBinding;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerViewAdapter mAdapter;
    private List<Reminder> reminders = new ArrayList<>();

    private RecyclerView upcomingRecyclerView;
    private RecyclerView.LayoutManager upcomingLayoutManager;
    private RecyclerViewAdapter upcomingAdapter;
    private List<Reminder> upcomingReminders = new ArrayList<>();

    private FloatingActionButton floatingActionButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutManager = new LinearLayoutManager(getContext());
        mAdapter = new RecyclerViewAdapter(getActivity(), reminders);

        upcomingLayoutManager = new LinearLayoutManager(getContext());
        upcomingAdapter = new RecyclerViewAdapter(getActivity(), upcomingReminders);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (fragmentDashboardBinding == null) {
            fragmentDashboardBinding = FragmentDashboardBinding.inflate(inflater, container, false);
            floatingActionButton = fragmentDashboardBinding.fab;
            setFloatingButtonAction();

            recyclerView = fragmentDashboardBinding.rvReminders;
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(mAdapter);

            upcomingRecyclerView = fragmentDashboardBinding.rvUpcomingReminders;
            upcomingRecyclerView.setLayoutManager(upcomingLayoutManager);
            upcomingRecyclerView.setAdapter(upcomingAdapter);
        }
        getReminders();
        return fragmentDashboardBinding.getRoot();
    }

    private void setFloatingButtonAction() {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) Objects.requireNonNull(getActivity())).getNavController().navigate(R.id.nav_reminder);
            }
        });
    }

    private void getReminders() {
        reminderCollectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    reminders.clear();
                    upcomingReminders.clear();

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);

                    long today = calendar.getTimeInMillis();

                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        Reminder reminder = documentSnapshot.toObject(Reminder.class);
                        Common.updateCalendarFromReminder(calendar, reminder);

                        if (today > calendar.getTimeInMillis()) {
                            reminders.add(reminder);
                        } else {
                            upcomingReminders.add(reminder);
                        }

                    }

                    Collections.sort(reminders, Common.reminderComparator);
                    Collections.sort(upcomingReminders, Common.reminderComparator);

                    mAdapter.updateReminders(reminders);
                    mAdapter.notifyDataSetChanged();

                    upcomingAdapter.updateReminders(upcomingReminders);
                    upcomingAdapter.notifyDataSetChanged();
                } else {
                    Common.viewToast(getContext(), "Error while getting reminders");
                }

                fragmentDashboardBinding.tvNoReminders.setVisibility((reminders.size() == 0 && upcomingReminders.size() == 0) ? View.VISIBLE : View.INVISIBLE);
                fragmentDashboardBinding.tvToday.setVisibility((reminders.size() != 0) ? View.VISIBLE : View.INVISIBLE);
                fragmentDashboardBinding.tvUpcoming.setVisibility((upcomingReminders.size() != 0) ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }
}
