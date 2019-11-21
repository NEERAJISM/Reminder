package com.markone.reminder.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.markone.reminder.Common;
import com.markone.reminder.LoginActivity;
import com.markone.reminder.R;
import com.markone.reminder.databinding.FragmentSettingsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.markone.reminder.Common.Frequency;
import static com.markone.reminder.Common.Frequency.Every_10_Min;
import static com.markone.reminder.Common.Frequency.Every_1_Min;
import static com.markone.reminder.Common.Frequency.Every_30_Min;
import static com.markone.reminder.Common.Frequency.Every_5_Min;
import static com.markone.reminder.Common.Frequency.getFrequency;
import static com.markone.reminder.Common.getGoogleSignInClient;

public class SettingsFragment extends Fragment {
    public static final String SETTING_FILE = "Settings";
    public static final String SNOOZE_SETTING = "snoozeSetting";

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

        snoozeFrequency = getFrequency(getActivity().getSharedPreferences(SETTING_FILE, Context.MODE_PRIVATE).getString(SNOOZE_SETTING, Every_1_Min.toString()));
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
                getGoogleSignInClient(getActivity()).signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Common.viewToast(getContext(), "Logged out successfully!!");
                            startActivity(new Intent(getContext(), LoginActivity.class));
                            Objects.requireNonNull(getActivity()).finish();
                        } else {
                            Common.viewToast(getContext(), "Unable to logout!!");
                        }
                    }
                });
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