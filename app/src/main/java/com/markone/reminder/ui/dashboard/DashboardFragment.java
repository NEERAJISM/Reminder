package com.markone.reminder.ui.dashboard;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.markone.reminder.Common;
import com.markone.reminder.R;
import com.markone.reminder.alarm.AlarmReceiver;
import com.markone.reminder.databinding.FragmentDashboardBinding;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Neeraj on 02-Nov-19
 */
public class DashboardFragment extends Fragment {
    private AlarmManager alarmManager;

    private CollectionReference reminderCollectionReference;

    private FragmentDashboardBinding fragmentDashboardBinding;
    private NavController navController;

    private RecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Reminder> reminders = new ArrayList<>();
    private List<Reminder> upcomingReminders = new ArrayList<>();

    private FloatingActionButton floatingActionButton;

    private List<Reminder> doneReminders = new ArrayList<>();
    private DoneDashboardFragment doneDashboardFragment;

    private boolean signout = true;
    private boolean isProUser = false;
    private SharedPreferences sharedPreferences;

    DashboardFragment(DoneDashboardFragment doneDashboardFragment) {
        this.doneDashboardFragment = doneDashboardFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        layoutManager = new LinearLayoutManager(getContext());
        mAdapter = new RecyclerViewAdapter(getActivity(), reminders);

        navController = NavHostFragment.findNavController(this);

        reminderCollectionReference = Common.getUserReminderCollection(Objects.requireNonNull(getActivity()).getSharedPreferences(Common.USER_FILE, MODE_PRIVATE).getString(Common.USER_ID, "UserId"));

        sharedPreferences = getActivity().getSharedPreferences(Common.SETTING_FILE, MODE_PRIVATE);
        signout = sharedPreferences.getBoolean(Common.SIGNOUT, true);
        if (signout) {
            sharedPreferences.edit().putBoolean(Common.SIGNOUT, false).apply();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (fragmentDashboardBinding == null) {
            fragmentDashboardBinding = FragmentDashboardBinding.inflate(inflater, container, false);
            floatingActionButton = fragmentDashboardBinding.fab;

            RecyclerView recyclerView = fragmentDashboardBinding.rvReminders;
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(mAdapter);
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

    void getReminders() {
        fragmentDashboardBinding.swipeFreshLayout.setRefreshing(true);
        reminderCollectionReference.get((signout) ? Source.DEFAULT : Source.CACHE).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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

                    mAdapter.clearReminders();
                    mAdapter.updateReminders(reminders, "Today");
                    mAdapter.updateReminders(upcomingReminders, "Upcoming");
                    mAdapter.notifyDataSetChanged();
                    doneDashboardFragment.updateReminders(doneReminders);
                    if (signout) {
                        setRemindersOnFirstLogin(reminders);
                        setRemindersOnFirstLogin(upcomingReminders);
                    }
                } else {
                    Common.viewToast(getContext(), "Error while getting reminders");
                }

                fragmentDashboardBinding.tvNoReminders.setVisibility((reminders.size() == 0 && upcomingReminders.size() == 0) ? View.VISIBLE : View.GONE);
                fragmentDashboardBinding.swipeFreshLayout.setRefreshing(false);
            }
        });
    }

    private void setRemindersOnFirstLogin(List<Reminder> reminders) {
        Calendar calendar = Calendar.getInstance();
        for (Reminder reminder : reminders) {
            calendar.set(Calendar.HOUR_OF_DAY, reminder.getStartDate_Hour());
            calendar.set(Calendar.MINUTE, reminder.getStartDate_Minute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.DAY_OF_MONTH, reminder.getStartDate_Day());
            calendar.set(Calendar.MONTH, reminder.getStartDate_Month());
            calendar.set(Calendar.YEAR, reminder.getStartDate_Year());
            PendingIntent pendingIntent = getPendingIntent(reminder);
            alarmManager.set(AlarmManager.RTC_WAKEUP, Math.max(System.currentTimeMillis(), calendar.getTimeInMillis()), pendingIntent);
        }
    }

    private PendingIntent getPendingIntent(Reminder reminder) {
        // chk if already set or update
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        intent.setAction(reminder.getId());
        intent.putExtra(Common.REMINDER_NAME, reminder.getName());
        intent.putExtra(Common.REMINDER_FREQUENCY, reminder.getFrequency().toString());
        return PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
