package com.markone.reminder.ui.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.markone.reminder.Common;
import com.markone.reminder.LoginActivity;
import com.markone.reminder.R;
import com.markone.reminder.alarm.AlarmReceiver;
import com.markone.reminder.databinding.FragmentSettingsBinding;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static com.markone.reminder.Common.Frequency;
import static com.markone.reminder.Common.Frequency.Every_10_Min;
import static com.markone.reminder.Common.Frequency.Every_1_Min;
import static com.markone.reminder.Common.Frequency.Every_30_Min;
import static com.markone.reminder.Common.Frequency.Every_5_Min;
import static com.markone.reminder.Common.Frequency.Hourly;
import static com.markone.reminder.Common.Frequency.getFrequency;
import static com.markone.reminder.Common.SETTING_FILE;
import static com.markone.reminder.Common.SNOOZE_SETTING;

public class SettingsFragment extends Fragment {
    private AlarmManager alarmManager;
    private CollectionReference reminderCollectionReference;
    private SharedPreferences sharedPreferences;

    private FragmentSettingsBinding fragmentSettingsBinding;
    private List<String> snoozeListName = new ArrayList<>();
    private Frequency snoozeFrequency = Every_1_Min;

    private Boolean runsInBackground = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        snoozeListName.add(Every_1_Min.toString());
        snoozeListName.add(Every_5_Min.toString());
        snoozeListName.add(Every_10_Min.toString());
        snoozeListName.add(Every_30_Min.toString());
        snoozeListName.add(Hourly.toString());

        alarmManager = (AlarmManager) Objects.requireNonNull(getContext()).getSystemService(Context.ALARM_SERVICE);
        reminderCollectionReference = Common.getUserReminderCollection(Objects.requireNonNull(getActivity()).getSharedPreferences(Common.USER_FILE, MODE_PRIVATE).getString(Common.USER_ID, "UserId"));

        sharedPreferences = getActivity().getSharedPreferences(Common.SETTING_FILE, MODE_PRIVATE);
        snoozeFrequency = getFrequency(sharedPreferences.getString(SNOOZE_SETTING, Every_1_Min.toString()));
    }

    private void checkBackgroundActivity() {
        PowerManager powerManager = (PowerManager) Objects.requireNonNull(getContext()).getSystemService(Context.POWER_SERVICE);
        if (powerManager != null && Build.VERSION.SDK_INT >= 23) {
            runsInBackground = powerManager.isIgnoringBatteryOptimizations(getContext().getPackageName());
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentSettingsBinding == null) {
            fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false);
            fragmentSettingsBinding.spinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item, snoozeListName));
            fragmentSettingsBinding.spinner.setSelection(snoozeListName.indexOf(snoozeFrequency.toString()));
            setupOptimization();
            setupSignOut();
        }
        return fragmentSettingsBinding.getRoot();
    }

    @Override
    public void onResume() {
        setupOptimization();
        super.onResume();
    }

    private void setupOptimization() {
        checkBackgroundActivity();
        if (runsInBackground) {
            fragmentSettingsBinding.clBatteryInstruction.setVisibility(View.GONE);
            fragmentSettingsBinding.clBattery.setVisibility(View.GONE);
        } else {
            fragmentSettingsBinding.clBattery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        Intent i = new Intent();
                        i.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        getContext().startActivity(i);
                    }
                }
            });
        }
    }

    private void setupSignOut() {
        fragmentSettingsBinding.btSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllAlarms();
                FirebaseAuth.getInstance().signOut();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignIn.getClient(getContext(), gso).signOut();
                sharedPreferences.edit().putBoolean(Common.SIGNOUT, true).apply();
                Common.viewToast(getContext(), "Logged out successfully!!");
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
            }
        });
    }

    private void removeAllAlarms() {
        reminderCollectionReference.get(Source.CACHE).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    if (alarmManager == null)
                        return;

                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        Reminder reminder = documentSnapshot.toObject(Reminder.class);
                        if (reminder.getStatus() == Common.Status.Done) {
                            continue;
                        }

                        Intent intent = new Intent(getContext(), AlarmReceiver.class);
                        intent.setAction(reminder.getId());
                        intent.putExtra(Common.REMINDER_NAME, reminder.getName());
                        intent.putExtra(Common.REMINDER_FREQUENCY, reminder.getFrequency().toString());
                        alarmManager.cancel(PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        getActivity().getSharedPreferences(SETTING_FILE, Context.MODE_PRIVATE).edit()
                .putString(SNOOZE_SETTING, fragmentSettingsBinding.spinner.getSelectedItem()
                        .toString()).apply();
        super.onPause();
    }
}