package com.markone.reminder.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.markone.reminder.Common;
import com.markone.reminder.LoginActivity;
import com.markone.reminder.R;
import com.markone.reminder.databinding.FragmentSettingsBinding;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.markone.reminder.Common.Frequency;
import static com.markone.reminder.Common.Frequency.Every_10_Min;
import static com.markone.reminder.Common.Frequency.Every_1_Min;
import static com.markone.reminder.Common.Frequency.Every_30_Min;
import static com.markone.reminder.Common.Frequency.Every_5_Min;
import static com.markone.reminder.Common.Frequency.getFrequency;
import static com.markone.reminder.Common.SETTING_FILE;
import static com.markone.reminder.Common.SNOOZE_SETTING;

public class SettingsFragment extends Fragment {
    private SharedPreferences sharedPreferences;

    private FragmentSettingsBinding fragmentSettingsBinding;
    private List<String> snoozeListName = new ArrayList<>();
    private Frequency snoozeFrequency = Every_1_Min;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        snoozeListName.add(Every_1_Min.toString());
        snoozeListName.add(Every_5_Min.toString());
        snoozeListName.add(Every_10_Min.toString());
        snoozeListName.add(Every_30_Min.toString());

        sharedPreferences = getActivity().getSharedPreferences(Common.SETTING_FILE, MODE_PRIVATE);
        snoozeFrequency = getFrequency(sharedPreferences.getString(SNOOZE_SETTING, Every_1_Min.toString()));
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentSettingsBinding == null) {
            fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false);
            fragmentSettingsBinding.spinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item, snoozeListName));
            fragmentSettingsBinding.spinner.setSelection(snoozeListName.indexOf(snoozeFrequency.toString()));
            setupSignOut();
        }
        return fragmentSettingsBinding.getRoot();
    }

    private void setupSignOut() {
        fragmentSettingsBinding.btSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToDo Remove all reminders
                FirebaseAuth.getInstance().signOut();
                sharedPreferences.edit().putBoolean(Common.IS_FIRST_LOGIN, false).apply();
                Common.viewToast(getContext(), "Logged out successfully!!");
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
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