package com.markone.reminder.ui.dashboard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.markone.reminder.Common;
import com.markone.reminder.R;
import com.markone.reminder.databinding.FragmentDashboardBinding;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Neeraj on 02-Nov-19
 */
public class DashboardFragment extends Fragment {
    private CollectionReference reminderCollectionReference;

    private FragmentDashboardBinding fragmentDashboardBinding;
    private NavController navController;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerViewAdapter mAdapter;
    private List<Reminder> reminders = new ArrayList<>();

    private RecyclerView upcomingRecyclerView;
    private RecyclerView.LayoutManager upcomingLayoutManager;
    private RecyclerViewAdapter upcomingAdapter;
    private List<Reminder> upcomingReminders = new ArrayList<>();

    private List<Reminder> doneReminders = new ArrayList<>();

    private FloatingActionButton floatingActionButton;

    private DoneDashboardFragment doneDashboardFragment;

    private boolean isFirstLogin = true;
    private boolean isProUser = false;
    private SharedPreferences sharedPreferences;

    DashboardFragment(DoneDashboardFragment doneDashboardFragment) {
        this.doneDashboardFragment = doneDashboardFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutManager = new LinearLayoutManager(getContext());
        mAdapter = new RecyclerViewAdapter(getActivity(), reminders);

        upcomingLayoutManager = new LinearLayoutManager(getContext());
        upcomingAdapter = new RecyclerViewAdapter(getActivity(), upcomingReminders);
        navController = NavHostFragment.findNavController(this);

        reminderCollectionReference = FirebaseFirestore.getInstance()
                .collection(Common.REMINDER_DB)
                .document(getActivity().getSharedPreferences(Common.USER_FILE, MODE_PRIVATE).getString(Common.USER_ID, "UserId"))
                .collection(Common.REMINDER_COLLECTION);

        sharedPreferences = getActivity().getSharedPreferences(Common.SETTING_FILE, MODE_PRIVATE);
        isFirstLogin = sharedPreferences.getBoolean(Common.IS_FIRST_LOGIN, true);

        if (isFirstLogin) {
            sharedPreferences.edit().putBoolean(Common.SETTING_FILE, false).apply();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (fragmentDashboardBinding == null) {
            fragmentDashboardBinding = FragmentDashboardBinding.inflate(inflater, container, false);
            floatingActionButton = fragmentDashboardBinding.fab;

            recyclerView = fragmentDashboardBinding.rvReminders;
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(mAdapter);

            upcomingRecyclerView = fragmentDashboardBinding.rvUpcomingReminders;
            upcomingRecyclerView.setLayoutManager(upcomingLayoutManager);
            upcomingRecyclerView.setAdapter(upcomingAdapter);
            setSwipeFreshLayout();
        }
        getReminders();
        setFloatingButtonAction();
        return fragmentDashboardBinding.getRoot();
    }

    private void setSwipeFreshLayout() {
        fragmentDashboardBinding.swipeFreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getReminders();
            }
        });
    }

    private void setFloatingButtonAction() {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentReminderSize = sharedPreferences.getInt(Common.CURRENT_REMINDER_SIZE, 0);
                if ((isProUser ? Common.MAX_REMINDERS_PRO : Common.MAX_REMINDERS) <= currentReminderSize) {
                    Common.viewToast(getContext(), "Max Reminder Limit Reached");
                } else {
                    navController.navigate(R.id.nav_reminder);
                }
            }
        });
    }

    public void getReminders() {
        fragmentDashboardBinding.swipeFreshLayout.setRefreshing(true);
        reminderCollectionReference.get(isFirstLogin ? Source.DEFAULT : Source.CACHE).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    reminders.clear();
                    upcomingReminders.clear();
                    doneReminders.clear();

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);

                    long today = calendar.getTimeInMillis();
                    sharedPreferences.edit().putInt(Common.CURRENT_REMINDER_SIZE, task.getResult().size()).apply();

                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        Reminder reminder = documentSnapshot.toObject(Reminder.class);
                        if (reminder.getStatus() == Common.Status.Done) {
                            doneReminders.add(reminder);
                            continue;
                        }

                        Common.updateCalendarFromReminder(calendar, reminder);
                        if (today > calendar.getTimeInMillis()) {
                            reminders.add(reminder);
                        } else {
                            upcomingReminders.add(reminder);
                        }
                    }

                    if (reminders.size() > 1) {
                        Collections.sort(reminders, Common.reminderComparator);
                    }
                    if (upcomingReminders.size() > 1) {
                        Collections.sort(upcomingReminders, Common.reminderComparator);
                    }

                    mAdapter.updateReminders(reminders);
                    mAdapter.notifyDataSetChanged();

                    upcomingAdapter.updateReminders(upcomingReminders);
                    upcomingAdapter.notifyDataSetChanged();
                    doneDashboardFragment.updateReminders(doneReminders);
                } else {
                    Common.viewToast(getContext(), "Error while getting reminders");
                }

                fragmentDashboardBinding.tvNoReminders.setVisibility((reminders.size() == 0 && upcomingReminders.size() == 0) ? View.VISIBLE : View.GONE);
                fragmentDashboardBinding.tvToday.setVisibility((reminders.size() != 0) ? View.VISIBLE : View.GONE);
                fragmentDashboardBinding.tvUpcoming.setVisibility((upcomingReminders.size() != 0) ? View.VISIBLE : View.GONE);
                fragmentDashboardBinding.swipeFreshLayout.setRefreshing(false);
            }
        });
    }
}
